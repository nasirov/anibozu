package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class AnimediaService {

	private static final String POSTER_URL_LOW_QUALITY_QUERY_PARAMETER = "h=70&q=50";

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";

	@Value("${resources.tempFolder.name}")
	private String tempFolderName;

	@Value("${resources.tempNewTitlesInAnimediaSearchList.name}")
	private String tempNewTitlesInAnimediaSearchList;

	@Value("${resources.tempRemovedTitlesFromAnimediaSearchList.name}")
	private String tempRemovedTitlesFromAnimediaSearchList;

	@Value("${resources.tempDuplicatedUrlsInAnimediaSearchList.name}")
	private String tempDuplicatedUrlsInAnimediaSearchList;

	@Value("${resources.tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList.name}")
	private String tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList;

	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;

	@Value("${cache.sortedAnimediaSearchList.name}")
	private String sortedAnimediaSearchListCacheName;

	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;

	@Value("${urls.online.animedia.anime.list}")
	private String animediaAnimeList;

	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;

	@Value("${urls.online.animedia.anime.episodes.postfix}")
	private String animediaEpisodesListPostfix;

	@Value("${urls.raw.githubusercontent.com.animediaSearchList}")
	private String animediaSearchListFromGitHubUrl;

	@Value("classpath:${resources.multiSeasonsAnimeUrls.name}")
	private Resource resourceMultiSeasonsAnimeUrls;

	@Value("classpath:${resources.singleSeasonsAnimeUrls.name}")
	private Resource resourceSingleSeasonsAnimeUrls;

	@Value("classpath:${resources.announcements.name}")
	private Resource resourceAnnouncementsUrls;

	private Map<String, Map<String, String>> animediaRequestParameters;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	private CacheManager cacheManager;

	@Autowired
	public AnimediaService(HttpCaller httpCaller,
			@Qualifier(value = "animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder, AnimediaHTMLParser
			animediaHTMLParser,
			CacheManager cacheManager) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.cacheManager = cacheManager;
	}

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
	}

	/**
	 * Searches for fresh animedia search list from animedia
	 *
	 * @return the list with title search info on animedia
	 */
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromAnimedia() {
		HttpResponse animediaResponse = httpCaller.call(animediaAnimeList, HttpMethod.GET, animediaRequestParameters);
		Set<AnimediaTitleSearchInfo> animediaSearchList = WrappedObjectMapper
				.unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		animediaSearchList.forEach(title -> {
			title.setUrl(UriUtils.encodePath(title.getUrl().replaceAll(animediaOnlineTv, ""), StandardCharsets.UTF_8));
			title.setPosterUrl(title.getPosterUrl().replace(POSTER_URL_LOW_QUALITY_QUERY_PARAMETER, POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER));
		});
		return animediaSearchList;
	}

	/**
	 * Searches for fresh animedia search list from github
	 *
	 * @return the list with title search info on animedia
	 */
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromGitHub() {
		HttpResponse animediaResponse = httpCaller.call(animediaSearchListFromGitHubUrl, HttpMethod.GET, animediaRequestParameters);
		return WrappedObjectMapper.unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}

	/**
	 * Searches for currently updated titles on animedia
	 *
	 * @return list of currently updated titles
	 */
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles() {
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		HttpResponse animediaResponse = httpCaller.call(animediaOnlineTv, HttpMethod.GET, animediaRequestParameters);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaHTMLParser.getCurrentlyUpdatedTitlesList(animediaResponse);
		currentlyUpdatedTitlesCache.putIfAbsent(currentlyUpdatedTitlesCacheName, currentlyUpdatedTitles);
		return currentlyUpdatedTitles;
	}

	/**
	 * Sort the anime search info for single season,multi seasons, announcements
	 *
	 * @param animediaSearchListInput the anime info for search on animedia
	 * @return list[0] - singleSeason anime, list[1] - multiSeason anime,list[2] - announcements
	 */
	public Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByType(@NotEmpty Set<AnimediaTitleSearchInfo> animediaSearchListInput) {
		int multiSeasonCount = 1;
		int singleSeasonCount = 1;
		int announcementCount = 1;
		Set<Anime> multi = new LinkedHashSet<>();
		Set<Anime> single = new LinkedHashSet<>();
		Set<Anime> announcement = new LinkedHashSet<>();
		EnumMap<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = new EnumMap<>(AnimeTypeOnAnimedia.class);
		for (AnimediaTitleSearchInfo animediaSearchList : animediaSearchListInput) {
			String rootUrl = animediaSearchList.getUrl();
			String url = animediaOnlineTv + rootUrl;
			//get a html page with an anime
			HttpResponse response = httpCaller.call(url, HttpMethod.GET, animediaRequestParameters);
			if (isAnnouncement(response.getContent())) {
				announcement.add(new Anime(String.valueOf(announcementCount), url, rootUrl));
				announcementCount++;
				continue;
			}
			Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
			for (Map.Entry<String, Map<String, String>> animeIdSeasonsAndEpisodesEntry : animeIdSeasonsAndEpisodesMap.entrySet()) {
				int dataListCount = 1;
				Map<String, String> seasonsAndEpisodesMap = animeIdSeasonsAndEpisodesEntry.getValue();
				for (Map.Entry<String, String> seasonsAndEpisodesEntry : seasonsAndEpisodesMap.entrySet()) {
					String dataList = seasonsAndEpisodesEntry.getKey();
					if (seasonsAndEpisodesMap.size() > 1) {
						String animeId = animeIdSeasonsAndEpisodesEntry.getKey();
						handleMultiSeasonsAnime(animeId, dataList, animediaRequestParameters, multiSeasonCount, dataListCount, multi, url, rootUrl);
						dataListCount++;
					} else {
						String maxEpisodeInDataList = seasonsAndEpisodesEntry.getValue();
						handleSingleSeasonAnime(url, dataList, maxEpisodeInDataList, single, singleSeasonCount, rootUrl);
						singleSeasonCount++;
					}
				}
				if (seasonsAndEpisodesMap.size() > 1) {
					multiSeasonCount++;
				}
			}
		}
		allSeasons.put(SINGLESEASON, single);
		allSeasons.put(MULTISEASONS, multi);
		allSeasons.put(ANNOUNCEMENT, announcement);
		addSortedAnimeToTempResources(single, multi, announcement);
		putSortedAnimeToCache(single, multi, announcement);
		return allSeasons;
	}

	/**
	 * Deserialize single, multi, announcements from resources
	 * priority:
	 * 1.resources from temp/ means that resources from classpath are not updated
	 * 2.resources from classpath
	 *
	 * @return map with singleseason anime, multiseasons anime and announcements
	 */
	public Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByTypeFromResources() {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		Set<Anime> singleSeasonAnime = sortedAnimediaSearchListCache.get(SINGLESEASON.getDescription(), LinkedHashSet.class);
		Set<Anime> multiSeasonsAnime = sortedAnimediaSearchListCache.get(MULTISEASONS.getDescription(), LinkedHashSet.class);
		Set<Anime> announcements = sortedAnimediaSearchListCache.get(ANNOUNCEMENT.getDescription(), LinkedHashSet.class);
		EnumMap<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = new EnumMap<>(AnimeTypeOnAnimedia.class);
		if (singleSeasonAnime != null && multiSeasonsAnime != null && announcements != null) {
			log.info("LOADING SORTED ANIME FROM CACHE ...");
			allSeasons.put(SINGLESEASON, singleSeasonAnime);
			allSeasons.put(MULTISEASONS, multiSeasonsAnime);
			allSeasons.put(ANNOUNCEMENT, announcements);
			log.info("SORTED ANIME ARE SUCCESSFULLY LOADED FROM CACHE.");
		} else {
			log.info("SORTED ANIME ARE NOT FOUND IN CACHE!");
			return allSeasons;
		}
		return allSeasons;
	}

	/**
	 * Searches for new titles from animedia search list in containers from resources
	 *
	 * @param singleSeasonAnime single season anime from resources
	 * @param multiSeasonsAnime multi seasons anime from resources
	 * @param announcements announcements anime from resources
	 * @param animediaSearchList animedia search list
	 * @return set of not found titles from animedia search list
	 */
	public Set<AnimediaTitleSearchInfo> checkSortedAnime(@NotNull Set<Anime> singleSeasonAnime, @NotNull Set<Anime> multiSeasonsAnime,
			@NotNull Set<Anime> announcements, @NotNull Set<AnimediaTitleSearchInfo> animediaSearchList) {
		Set<AnimediaTitleSearchInfo> notFound = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo animediaTitleSearchInfo : animediaSearchList) {
			long singleCount = singleSeasonAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long multiCount = multiSeasonsAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long announcementCount = announcements.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			if (singleCount == 0 && multiCount == 0 && announcementCount == 0) {
				log.warn("NOT FOUND IN ANY SORTED ANIME LISTS {}", animediaOnlineTv + animediaTitleSearchInfo.getUrl());
				notFound.add(animediaTitleSearchInfo);
			}
		}
		return notFound;
	}

	/**
	 * Compare cached currently updated titles and fresh
	 * refresh cache with difference
	 *
	 * @param fresh fresh updated titles
	 * @param fromCache updated titles from cache
	 * @return list of differences between fresh and cached
	 */
	public List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles(@NotNull List<AnimediaMALTitleReferences> fresh,
			@NotNull List<AnimediaMALTitleReferences> fromCache) {
		List<AnimediaMALTitleReferences> list = new ArrayList<>();
		if (!fromCache.isEmpty() && !fresh.isEmpty()) {
			AnimediaMALTitleReferences animediaMALTitleReferencesFromCache = fromCache.get(0);
			if (fresh.size() != fromCache.size() || !fresh.get(0).equals(animediaMALTitleReferencesFromCache)) {
				for (AnimediaMALTitleReferences temp : fresh) {
					if (temp.equals(animediaMALTitleReferencesFromCache)) {
						break;
					}
					list.add(temp);
				}
			}
		} else if (fromCache.isEmpty() && !fresh.isEmpty()) {
			list.addAll(fresh);
		} else if (!fromCache.isEmpty() && fresh.isEmpty()) {
			return list;
		}
		cacheManager.getCache(currentlyUpdatedTitlesCacheName).put(currentlyUpdatedTitlesCacheName, list);
		return list;
	}

	/**
	 * Compare animedia search lists
	 *
	 * @param fromResources animedia search list from resources
	 * @param fresh new animedia search list
	 * @return true if missing titles not available
	 */
	public boolean isAnimediaSearchListUpToDate(Set<AnimediaTitleSearchInfo> fromResources, Set<AnimediaTitleSearchInfo> fresh) {
		boolean fullMatch = true;
		List<AnimediaTitleSearchInfo> removedTitlesFromSearchList = new ArrayList<>();
		List<AnimediaTitleSearchInfo> newTitlesInSearchList = new ArrayList<>();
		Set<AnimediaTitleSearchInfo> duplicates = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo fromResource : fromResources) {
			AnimediaTitleSearchInfo matchedTitle = fresh.stream().filter(set -> set.getUrl().equals(fromResource.getUrl())).findAny().orElse(null);
			if (matchedTitle == null) {
				removedTitlesFromSearchList.add(fromResource);
				fullMatch = false;
				log.warn("TITLE {} REMOVED FROM FRESH ANIMEDIA SEARCH LIST! PLEASE, REMOVE IT FROM THE RESOURCES!", fromResource);
			}
			long duplicatedTitlesCount = fromResources.stream().filter(set -> set.getUrl().equals(fromResource.getUrl())).count();
			if (duplicatedTitlesCount > 1) {
				duplicates.add(fromResource);
				fullMatch = false;
				log.warn("DUPLICATED TITLE IN ANIMEDIA SEARCH LIST FROM RESOURCES {}", fromResource);
			}
		}
		for (AnimediaTitleSearchInfo freshTitle : fresh) {
			AnimediaTitleSearchInfo matchedTitle = fromResources.stream().filter(set -> set.getUrl().equals(freshTitle.getUrl())).findAny().orElse(null);
			if (matchedTitle == null) {
				newTitlesInSearchList.add(freshTitle);
				fullMatch = false;
				log.warn("NEW TITLE AVAILABLE IN ANIMEDIA SEARCH LIST {} ! PLEASE, ADD IT TO THE RESOURCES!", freshTitle);
			}
		}
		if (!newTitlesInSearchList.isEmpty() || !removedTitlesFromSearchList.isEmpty() || !duplicates.isEmpty()) {
			String prefix = tempFolderName + File.separator;
			try {
				if (!RoutinesIO.isDirectoryExists(tempFolderName)) {
					RoutinesIO.mkDir(tempFolderName);
				}
				if (!newTitlesInSearchList.isEmpty()) {
					RoutinesIO.marshalToFile(prefix + tempNewTitlesInAnimediaSearchList, newTitlesInSearchList);
				}
				if (!removedTitlesFromSearchList.isEmpty()) {
					RoutinesIO.marshalToFile(prefix + tempRemovedTitlesFromAnimediaSearchList, removedTitlesFromSearchList);
				}
				if (!duplicates.isEmpty()) {
					RoutinesIO.marshalToFile(prefix + tempDuplicatedUrlsInAnimediaSearchList, duplicates);
				}
			} catch (NotDirectoryException e) {
				log.error(e.getMessage());
			}
		}
		return fullMatch;
	}

	/**
	 * Checks single season titles keywords for a concretized title name from MAL
	 * if title keywords contain cyrillic characters that means the title not handled
	 *
	 * @param singleSeasonAnime all single season anime
	 * @param animediaSearchListFromResources the animedia search list from resources
	 * @return true if keywords not contain cyrillic characters, else false
	 */
	public boolean isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(Set<Anime> singleSeasonAnime,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromResources) {
		Pattern pattern = Pattern.compile("[а-яА-Я]");
		Set<AnimediaTitleSearchInfo> matched = new LinkedHashSet<>();
		for (Anime x : singleSeasonAnime) {
			animediaSearchListFromResources.stream().filter(y -> {
				Matcher matcher = pattern.matcher(y.getKeywords());
				return y.getUrl().equals(x.getRootUrl()) && matcher.find();
			}).forEach(matched::add);
		}
		if (!matched.isEmpty()) {
			try {
				if (!RoutinesIO.isDirectoryExists(tempFolderName)) {
					RoutinesIO.mkDir(tempFolderName);
				}
				String prefix = tempFolderName + File.separator;
				log.warn("CONCRETIZE THIS TITLES FROM ANIMEDIA SEARCH LIST {}", matched);
				RoutinesIO.marshalToFile(prefix + tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList, matched);
			} catch (NotDirectoryException e) {
				log.error(e.getMessage());
			}
		}
		return matched.isEmpty();
	}

	private void addSortedAnimeToTempResources(Set<Anime> single, Set<Anime> multi, Set<Anime> announcement) {
		String prefix = tempFolderName + File.separator;
		RoutinesIO.mkDir(tempFolderName);
		RoutinesIO.marshalToFile(prefix + resourceSingleSeasonsAnimeUrls.getFilename(), single);
		RoutinesIO.marshalToFile(prefix + resourceMultiSeasonsAnimeUrls.getFilename(), multi);
		RoutinesIO.marshalToFile(prefix + resourceAnnouncementsUrls.getFilename(), announcement);
	}

	private void putSortedAnimeToCache(Set<Anime> single, Set<Anime> multi, Set<Anime> announcement) {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		sortedAnimediaSearchListCache.put(SINGLESEASON.getDescription(), single);
		sortedAnimediaSearchListCache.put(MULTISEASONS.getDescription(), multi);
		sortedAnimediaSearchListCache.put(ANNOUNCEMENT.getDescription(), announcement);
	}

	private void handleSingleSeasonAnime(String url, String dataList, String maxEpisodeInDataList, Set<Anime> single, int singleSeasonCount,
			String rootUrl) {
		String targetUrl = URLBuilder.build(url, dataList, null, maxEpisodeInDataList);
		single.add(new Anime(String.valueOf(singleSeasonCount), targetUrl, rootUrl));
	}

	private void handleMultiSeasonsAnime(String animeId, String dataList, Map<String, Map<String, String>> animediaRequestParameters,
			int multiSeasonCount, int dataListCount, Set<Anime> multi, String url, String rootUrl) {
		HttpResponse resp = httpCaller
				.call(animediaEpisodesList + animeId + "/" + dataList + animediaEpisodesListPostfix, HttpMethod.GET, animediaRequestParameters);
		String count = String.valueOf(multiSeasonCount) + "." + dataListCount;
		String targetUrl = URLBuilder.build(url, dataList, animediaHTMLParser.getFirstEpisodeInSeason(resp), null);
		multi.add(new Anime(count, targetUrl, rootUrl));
	}
}
