package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ALL_TYPES;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.util.AnimediaUtils.getAnimeId;
import static nasirov.yv.util.AnimediaUtils.getDataListsAndMaxEpisodesMap;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.RoutinesIO.marshalToFileInTheFolder;
import static nasirov.yv.util.URLBuilder.build;
import static org.springframework.http.HttpMethod.GET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class ResourcesService implements ResourcesServiceI {

	private static final Pattern CYRILLIC_CHARACTERS_PATTERN = Pattern.compile("[а-яА-Я]");

	private Map<String, Map<String, String>> animediaRequestParameters;

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
	public ResourcesService(HttpCaller httpCaller,
			@Qualifier(value = "animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			AnimediaHTMLParser animediaHTMLParser,
			CacheManager cacheManager, UrlsNames urlsNames, ResourcesNames resourcesNames) {
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
		sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		tempFolder = resourcesNames.getTempFolder();
		onlineAnimediaTv = urlsNames.getAnimediaUrls()
				.getOnlineAnimediaTv();
	}

	/**
	 * Sort the anime search info for single season,multi seasons, announcements
	 *
	 * @param animediaSearchListInput the anime info for search on animedia
	 * @return map with sets of single, multi and announcements
	 */
	@Override
	@CachePut(value = "sortedAnimediaSearchListCache", key = "'allTypes'", condition = "#root.caches[0].get('allTypes') == null")
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
			String fullUrl = onlineAnimediaTv + rootUrl;
			HttpResponse animediaResponseWithAnimeHtml = httpCaller.call(fullUrl, GET, animediaRequestParameters);
			if (isAnnouncement(animediaResponseWithAnimeHtml.getContent())) {
				announcement.add(new Anime(String.valueOf(announcementCount), fullUrl, rootUrl));
				announcementCount++;
			} else {
				Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(
						animediaResponseWithAnimeHtml);
				String animeId = getAnimeId(animeIdDataListsAndMaxEpisodesMap);
				Map<String, String> dataListsAndMaxEpisodes = getDataListsAndMaxEpisodesMap(animeIdDataListsAndMaxEpisodesMap);
				if (dataListsAndMaxEpisodes.size() > 1) {
					handleMultiSeasonsAnime(dataListsAndMaxEpisodes, animeId, multiSeasonCount, multi, fullUrl, rootUrl);
					multiSeasonCount++;
				} else {
					handleSingleSeasonAnime(dataListsAndMaxEpisodes, fullUrl, single, singleSeasonCount, rootUrl);
					singleSeasonCount++;
				}
			}
		}
		allSeasons.put(SINGLESEASON, single);
		allSeasons.put(MULTISEASONS, multi);
		allSeasons.put(ANNOUNCEMENT, announcement);
		marshalSortedAnimeToTempResources(single, multi, announcement);
		return allSeasons;
	}

	/**
	 * Returns sorted anime from cache if present
	 *
	 * @return map with sets of single, multi and announcements
	 */
	@Override
	public Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByTypeFromCache() {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = sortedAnimediaSearchListCache.get(ALL_TYPES.getDescription(), EnumMap.class);
		if (allTypes == null) {
			log.info("SORTED ANIME ARE NOT FOUND IN CACHE!");
			allTypes = new HashMap<>();
		} else {
			log.info("SORTED ANIME ARE SUCCESSFULLY LOADED FROM CACHE.");
		}
		return allTypes;
	}

	/**
	 * Searches for new titles from animedia search list in containers from resources
	 *
	 * @param allTypes           singleSeasonAnime, multiSeasonsAnime, announcements
	 * @param animediaSearchList animedia search list
	 * @return set of not found titles from animedia search list
	 */
	@Override
	public Set<AnimediaTitleSearchInfo> checkSortedAnime(Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes,
			Set<AnimediaTitleSearchInfo> animediaSearchList) {
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> announcements = allTypes.get(ANNOUNCEMENT);
		Set<AnimediaTitleSearchInfo> notFound = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo animediaTitleSearchInfo : animediaSearchList) {
			long singleCount = singleSeasonAnime.stream()
					.filter(set -> set.getRootUrl()
							.equals(animediaTitleSearchInfo.getUrl()))
					.count();
			long multiCount = multiSeasonsAnime.stream()
					.filter(set -> set.getRootUrl()
							.equals(animediaTitleSearchInfo.getUrl()))
					.count();
			long announcementCount = announcements.stream()
					.filter(set -> set.getRootUrl()
							.equals(animediaTitleSearchInfo.getUrl()))
					.count();
			if (singleCount == 0 && multiCount == 0 && announcementCount == 0) {
				log.warn("NOT FOUND IN ANY SORTED ANIME LISTS {}", onlineAnimediaTv + animediaTitleSearchInfo.getUrl());
				notFound.add(animediaTitleSearchInfo);
			}
		}
		return notFound;
	}

	/**
	 * Compare animedia search lists
	 *
	 * @param fromResources animedia search list from resources
	 * @param fresh         new animedia search list
	 * @return true if missing titles not available
	 */
	@Override
	public boolean isAnimediaSearchListFromGitHubUpToDate(Set<AnimediaTitleSearchInfo> fromResources, Set<AnimediaTitleSearchInfo> fresh) {
		boolean fullMatch = true;
		List<AnimediaTitleSearchInfo> removedTitlesFromSearchList = new ArrayList<>();
		List<AnimediaTitleSearchInfo> newTitlesInSearchList = new ArrayList<>();
		Set<AnimediaTitleSearchInfo> duplicates = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo fromResource : fromResources) {
			AnimediaTitleSearchInfo matchedTitle = fresh.stream()
					.filter(set -> set.getUrl()
							.equals(fromResource.getUrl()))
					.findAny()
					.orElse(null);
			if (matchedTitle == null) {
				removedTitlesFromSearchList.add(fromResource);
				fullMatch = false;
				log.warn("TITLE {} REMOVED FROM FRESH ANIMEDIA SEARCH LIST! PLEASE, REMOVE IT FROM THE RESOURCES!", fromResource);
			}
			long duplicatedTitlesCount = fromResources.stream()
					.filter(set -> set.getUrl()
							.equals(fromResource.getUrl()))
					.count();
			if (duplicatedTitlesCount > 1) {
				duplicates.add(fromResource);
				fullMatch = false;
				log.warn("DUPLICATED TITLE IN ANIMEDIA SEARCH LIST FROM RESOURCES {}", fromResource);
			}
		}
		for (AnimediaTitleSearchInfo freshTitle : fresh) {
			AnimediaTitleSearchInfo matchedTitle = fromResources.stream()
					.filter(set -> set.getUrl()
							.equals(freshTitle.getUrl()))
					.findAny()
					.orElse(null);
			if (matchedTitle == null) {
				newTitlesInSearchList.add(freshTitle);
				fullMatch = false;
				log.warn("NEW TITLE AVAILABLE IN ANIMEDIA SEARCH LIST {} ! PLEASE, ADD IT TO THE RESOURCES!", freshTitle);
			}
		}
		marshallToTempFolder(resourcesNames.getTempNewTitlesInAnimediaSearchList(), newTitlesInSearchList);
		marshallToTempFolder(resourcesNames.getTempRemovedTitlesFromAnimediaSearchList(), removedTitlesFromSearchList);
		marshallToTempFolder(resourcesNames.getTempDuplicatedUrlsInAnimediaSearchList(), duplicates);
		return fullMatch;
	}


	/**
	 * Checks single season titles keywords for a concretized title name from MAL if title keywords contain cyrillic characters that means the title
	 * not
	 * handled
	 *
	 * @param singleSeasonAnime               all single season anime
	 * @param animediaSearchListFromResources the animedia search list from resources
	 * @return true if keywords not contain cyrillic characters, else false
	 */
	@Override
	public boolean isAllSingleSeasonAnimeHasConcretizedMALTitleName(Set<Anime> singleSeasonAnime,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromResources) {
		Set<AnimediaTitleSearchInfo> matched = new LinkedHashSet<>();
		for (Anime x : singleSeasonAnime) {
			animediaSearchListFromResources.stream()
					.filter(y -> {
						Matcher matcher = CYRILLIC_CHARACTERS_PATTERN.matcher(y.getKeywords());
						return y.getUrl()
								.equals(x.getRootUrl()) && matcher.find();
					})
					.forEach(matched::add);
		}
		marshallToTempFolder(resourcesNames.getTempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList(), matched);
		return matched.isEmpty();
	}

	/**
	 * Compare multi seasons titles from animedia search list with multi seasons references from resources
	 *
	 * @param multiSeasonsAnime multi seasons titles from animedia
	 * @param allReferences     all multi seasons references from resources
	 * @return true if multi seasons references from resources are full, if false then we must add the new reference to the raw mapping
	 */
	@Override
	public boolean isReferencesAreFull(Set<Anime> multiSeasonsAnime, Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> readFromRaw = convertReferencesSetToMap(allReferences);
		return compareMaps(multiSeasonsAnime, readFromRaw);
	}

	private Map<String, String> convertReferencesSetToMap(Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> urlTitle = new HashMap<>();
		allReferences.forEach(set -> urlTitle.put(onlineAnimediaTv + set.getUrl() + "/" + set.getDataList() + "/" + set.getFirstEpisode(),
				set.getTitleOnMAL()));
		return urlTitle;
	}

	private boolean compareMaps(Set<Anime> multi, Map<String, String> raw) {
		Set<Anime> missingReferences = multi.stream()
				.filter(x -> !raw.containsKey(x.getFullUrl()))
				.collect(Collectors.toSet());
		marshallToTempFolder(resourcesNames.getTempRawReferences(), missingReferences);
		return missingReferences.isEmpty();
	}

	private void handleSingleSeasonAnime(Map<String, String> seasonsAndEpisodesMap, String url, Set<Anime> single, int singleSeasonCount,
			String rootUrl) {
		String dataList = Stream.of(seasonsAndEpisodesMap)
				.flatMap(map -> map.entrySet()
						.stream())
				.map(Entry::getKey)
				.findFirst()
				.orElse(null);
		String maxEpisodeInDataList = Stream.of(seasonsAndEpisodesMap)
				.flatMap(map -> map.entrySet()
						.stream())
				.map(Entry::getValue)
				.findFirst()
				.orElse(null);
		String targetUrl = build(url, dataList, null, maxEpisodeInDataList);
		single.add(new Anime(String.valueOf(singleSeasonCount), targetUrl, rootUrl));
	}

	private void handleMultiSeasonsAnime(Map<String, String> dataListsAndMaxEpisodesMap, String animeId, int multiSeasonCount, Set<Anime> multi,
			String url, String rootUrl) {
		int dataListCount = 1;
		for (String dataList : dataListsAndMaxEpisodesMap.keySet()) {
			HttpResponse resp = httpCaller.call(urlsNames.getAnimediaUrls()
					.getOnlineAnimediaAnimeEpisodesList() + animeId + "/" + dataList + urlsNames.getAnimediaUrls()
					.getOnlineAnimediaAnimeEpisodesPostfix(), GET, animediaRequestParameters);
			String count = multiSeasonCount + "." + dataListCount;
			String targetUrl = build(url, dataList, animediaHTMLParser.getFirstEpisodeInSeason(resp), null);
			multi.add(new Anime(count, targetUrl, rootUrl));
			dataListCount++;
		}
	}

	private void marshalSortedAnimeToTempResources(Set<Anime> single, Set<Anime> multi, Set<Anime> announcement) {
		marshallToTempFolder(resourcesNames.getSingleSeasonsAnimeUrls(), single);
		marshallToTempFolder(resourcesNames.getMultiSeasonsAnimeUrls(), multi);
		marshallToTempFolder(resourcesNames.getAnnouncementsUrls(), announcement);
	}

	private void marshallToTempFolder(String tempFileName, Collection<?> content) {
		if (!content.isEmpty()) {
			marshalToFileInTheFolder(tempFolder, tempFileName, content);
		}
	}
}
