package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

	private static final Pattern CYRILLIC_CHARACTERS_PATTERN = Pattern.compile("[а-яА-Я]");

	private Map<String, Map<String, String>> animediaRequestParameters;

	private Cache currentlyUpdatedTitlesCache;

	private Cache sortedAnimediaSearchListCache;

	private String tempFolder;

	private String onlineAnimediaTv;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	private CacheManager cacheManager;

	private UrlsNames urlsNames;

	private ResourcesNames resourcesNames;

	@Autowired
	public AnimediaService(HttpCaller httpCaller,
			@Qualifier(value = "animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			AnimediaHTMLParser animediaHTMLParser, CacheManager cacheManager, UrlsNames urlsNames, ResourcesNames resourcesNames) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.cacheManager = cacheManager;
		this.urlsNames = urlsNames;
		this.resourcesNames = resourcesNames;
	}

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
		currentlyUpdatedTitlesCache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		tempFolder = resourcesNames.getTempFolder();
		onlineAnimediaTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
	}

	/**
	 * Searches for fresh animedia search list from animedia
	 *
	 * @return the list with title search info on animedia
	 */
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromAnimedia() {
		HttpResponse animediaResponse = httpCaller
				.call(urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeList(), HttpMethod.GET, animediaRequestParameters);
		Set<AnimediaTitleSearchInfo> animediaSearchList = WrappedObjectMapper
				.unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		animediaSearchList.forEach(title -> {
			title.setUrl(UriUtils.encodePath(title.getUrl().replaceAll(onlineAnimediaTv, ""), StandardCharsets.UTF_8));
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
		HttpResponse animediaResponse = httpCaller
				.call(urlsNames.getGitHubUrls().getRawGithubusercontentComAnimediaSearchList(), HttpMethod.GET, animediaRequestParameters);
		return WrappedObjectMapper.unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}

	/**
	 * Searches for currently updated titles on animedia
	 *
	 * @return list of currently updated titles
	 */
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles() {
		HttpResponse animediaResponse = httpCaller.call(onlineAnimediaTv, HttpMethod.GET, animediaRequestParameters);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaHTMLParser.getCurrentlyUpdatedTitlesList(animediaResponse);
		currentlyUpdatedTitlesCache.putIfAbsent(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, currentlyUpdatedTitles);
		return currentlyUpdatedTitles;
	}

	/**
	 * Sort the anime search info for single season,multi seasons, announcements
	 *
	 * @param animediaSearchListInput the anime info for search on animedia
	 * @return list[0] - singleSeason anime, list[1] - multiSeason anime,list[2] - announcements
	 */
	public Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByType(Set<AnimediaTitleSearchInfo> animediaSearchListInput) {
		int multiSeasonCount = 1;
		int singleSeasonCount = 1;
		int announcementCount = 1;
		Set<Anime> multi = new LinkedHashSet<>();
		Set<Anime> single = new LinkedHashSet<>();
		Set<Anime> announcement = new LinkedHashSet<>();
		EnumMap<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = new EnumMap<>(AnimeTypeOnAnimedia.class);
		for (AnimediaTitleSearchInfo animediaSearchList : animediaSearchListInput) {
			String rootUrl = animediaSearchList.getUrl();
			String url = onlineAnimediaTv + rootUrl;
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
						handleMultiSeasonsAnime(animeId, dataList, multiSeasonCount, dataListCount, multi, url, rootUrl);
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
	public Set<AnimediaTitleSearchInfo> checkSortedAnime(Set<Anime> singleSeasonAnime, Set<Anime> multiSeasonsAnime, Set<Anime> announcements,
			Set<AnimediaTitleSearchInfo> animediaSearchList) {
		Set<AnimediaTitleSearchInfo> notFound = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo animediaTitleSearchInfo : animediaSearchList) {
			long singleCount = singleSeasonAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long multiCount = multiSeasonsAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long announcementCount = announcements.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			if (singleCount == 0 && multiCount == 0 && announcementCount == 0) {
				log.warn("NOT FOUND IN ANY SORTED ANIME LISTS {}", onlineAnimediaTv + animediaTitleSearchInfo.getUrl());
				notFound.add(animediaTitleSearchInfo);
			}
		}
		return notFound;
	}

	/**
	 * Compare cached currently updated titles and fresh refresh cache with difference
	 *
	 * @param fresh fresh updated titles
	 * @param fromCache updated titles from cache
	 * @return list of differences between fresh and cached
	 */
	public List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles(List<AnimediaMALTitleReferences> fresh,
			List<AnimediaMALTitleReferences> fromCache) {
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
		currentlyUpdatedTitlesCache.put(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, list);
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
		if (!newTitlesInSearchList.isEmpty()) {
			marshallToTempFolder(resourcesNames.getTempNewTitlesInAnimediaSearchList(), newTitlesInSearchList);
		}
		if (!removedTitlesFromSearchList.isEmpty()) {
			marshallToTempFolder(resourcesNames.getTempRemovedTitlesFromAnimediaSearchList(), removedTitlesFromSearchList);
		}
		if (!duplicates.isEmpty()) {
			marshallToTempFolder(resourcesNames.getTempDuplicatedUrlsInAnimediaSearchList(), duplicates);
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
		Set<AnimediaTitleSearchInfo> matched = new LinkedHashSet<>();
		for (Anime x : singleSeasonAnime) {
			animediaSearchListFromResources.stream().filter(y -> {
				Matcher matcher = CYRILLIC_CHARACTERS_PATTERN.matcher(y.getKeywords());
				return y.getUrl().equals(x.getRootUrl()) && matcher.find();
			}).forEach(matched::add);
		}
		if (!matched.isEmpty()) {
			marshallToTempFolder(resourcesNames.getTempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList(), matched);
			log.warn("CONCRETIZE THIS TITLES FROM ANIMEDIA SEARCH LIST {}", matched);
		}
		return matched.isEmpty();
	}

	private void addSortedAnimeToTempResources(Set<Anime> single, Set<Anime> multi, Set<Anime> announcement) {
		RoutinesIO.marshalToFileInTheFolder(tempFolder, resourcesNames.getSingleSeasonsAnimeUrls(), single);
		RoutinesIO.marshalToFileInTheFolder(tempFolder, resourcesNames.getMultiSeasonsAnimeUrls(), multi);
		RoutinesIO.marshalToFileInTheFolder(tempFolder, resourcesNames.getAnnouncementsUrls(), announcement);
	}

	private void putSortedAnimeToCache(Set<Anime> single, Set<Anime> multi, Set<Anime> announcement) {
		sortedAnimediaSearchListCache.put(SINGLESEASON.getDescription(), single);
		sortedAnimediaSearchListCache.put(MULTISEASONS.getDescription(), multi);
		sortedAnimediaSearchListCache.put(ANNOUNCEMENT.getDescription(), announcement);
	}

	private void handleSingleSeasonAnime(String url, String dataList, String maxEpisodeInDataList, Set<Anime> single, int singleSeasonCount,
			String rootUrl) {
		String targetUrl = URLBuilder.build(url, dataList, null, maxEpisodeInDataList);
		single.add(new Anime(String.valueOf(singleSeasonCount), targetUrl, rootUrl));
	}

	private void handleMultiSeasonsAnime(String animeId, String dataList, int multiSeasonCount, int dataListCount, Set<Anime> multi, String url,
			String rootUrl) {
		HttpResponse resp = httpCaller.call(
				urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesList() + animeId + "/" + dataList + urlsNames.getAnimediaUrls()
						.getOnlineAnimediaAnimeEpisodesPostfix(), HttpMethod.GET, animediaRequestParameters);
		String count = multiSeasonCount + "." + dataListCount;
		String targetUrl = URLBuilder.build(url, dataList, animediaHTMLParser.getFirstEpisodeInSeason(resp), null);
		multi.add(new Anime(count, targetUrl, rootUrl));
	}

	private void marshallToTempFolder(String tempFileName, Collection<?> content) {
		RoutinesIO.marshalToFileInTheFolder(tempFolder, tempFileName, content);
	}
}
