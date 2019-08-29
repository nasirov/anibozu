package nasirov.yv.service;

import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedAndOngoing;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;
import static nasirov.yv.util.AnimediaUtils.isTitleUpdated;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.AnimediaUtils;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class ReferencesManager {

	private Map<String, Map<String, String>> animediaRequestParameters;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	private UrlsNames urlsNames;

	private ResourcesNames resourcesNames;

	@Autowired
	public ReferencesManager(HttpCaller httpCaller, @Qualifier("animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			AnimediaHTMLParser animediaHTMLParser, UrlsNames urlsNames, ResourcesNames resourcesNames) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.urlsNames = urlsNames;
		this.resourcesNames = resourcesNames;
	}

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
	}

	/**
	 * Creates container with the anime references
	 *
	 * @return the references
	 */
	public Set<AnimediaMALTitleReferences> getMultiSeasonsReferences() {
		HttpResponse response = httpCaller
				.call(urlsNames.getGitHubUrls().getRawGithubusercontentComReferences(), HttpMethod.GET, animediaRequestParameters);
		return WrappedObjectMapper.unmarshal(response.getContent(), AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}

	/**
	 * Updates multiseasons anime references minConcretizedEpisodeOnAnimedia,maxConcretizedEpisodeOnAnimedia,first episode,current max
	 *
	 * @param references the references
	 */
	public void updateReferences(Set<AnimediaMALTitleReferences> references) {
		String onlineAnimediaTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
		String onlineAnimediaAnimeEpisodesList = urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesList();
		String onlineAnimediaAnimeEpisodesPostfix = urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesPostfix();
		Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache = new HashMap<>();
		for (AnimediaMALTitleReferences reference : references) {
			if (isTitleUpdated(reference) || isTitleNotFoundOnMAL(reference)) {
				continue;
			}
			String url = onlineAnimediaTv + reference.getUrl();
			Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap = seasonsAndEpisodesCache.get(url);
			if (animeIdDataListsAndMaxEpisodesMap == null) {
				animeIdDataListsAndMaxEpisodesMap = getTitleHtmlAndPutInCache(url, seasonsAndEpisodesCache);
			}
			String animeId = AnimediaUtils.getAnimeId(animeIdDataListsAndMaxEpisodesMap);
			Map<String, String> dataListsAndMaxEpisodesMap = AnimediaUtils.getDataListsAndMaxEpisodesMap(animeIdDataListsAndMaxEpisodesMap);
			Stream.of(dataListsAndMaxEpisodesMap).flatMap(map -> map.entrySet().stream())
					.filter(dataListsAndMaxEpisodesMapEntry -> dataListsAndMaxEpisodesMapEntry.getKey().equals(reference.getDataList()))
					.forEach(x -> {
						HttpResponse responseHtmlWithEpisodesInConcretizedDataList = httpCaller
								.call(onlineAnimediaAnimeEpisodesList + animeId + "/" + reference.getDataList() + onlineAnimediaAnimeEpisodesPostfix,
										HttpMethod.GET,
										animediaRequestParameters);
						Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange(responseHtmlWithEpisodesInConcretizedDataList);
						Stream.of(episodesRange).flatMap(episodesRangeMap -> episodesRangeMap.entrySet().stream()).forEach(episodesRangeMapEntry -> {
							List<String> episodesList = episodesRangeMapEntry.getValue();
							String maxEpisodes = episodesRangeMapEntry.getKey();
							if (isTitleConcretizedAndOngoing(reference)) {
								enrichConcretizedAndOngoingReference(reference, episodesList);
							} else {
								enrichRegularReference(reference, maxEpisodes, episodesList);
							}
						});
					});
		}
	}

	private void enrichRegularReference(AnimediaMALTitleReferences reference, String maxEpisodes, List<String> episodesList) {
		Integer intMaxEpisodes = null;
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		if (!isMaxEpisodeUndefined(maxEpisodes)) {
			intMaxEpisodes = Integer.parseInt(maxEpisodes);
		}
		reference.setFirstEpisode(correctFirstEpisodeAndMin);
		reference.setMinConcretizedEpisodeOnAnimedia(correctFirstEpisodeAndMin);
		reference.setMaxConcretizedEpisodeOnAnimedia(getCorrectMaxConcretizedEpisodeOnAnimedia(intMaxEpisodes, correctFirstEpisodeAndMin, maxEpisodes));
		reference.setEpisodesRange(episodesList);
		reference.setCurrentMax(correctCurrentMax);
	}

	private void enrichConcretizedAndOngoingReference(AnimediaMALTitleReferences reference, List<String> episodesList) {
		String currentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		reference.setCurrentMax(currentMax);
	}

	private String getCorrectMaxConcretizedEpisodeOnAnimedia(Integer intMaxEpisodes, String firstEpisodeAndMin, String maxEpisodes) {
		int intFirstEpisodeAndMin = Integer.parseInt(firstEpisodeAndMin);
		return intMaxEpisodes != null && intMaxEpisodes < intFirstEpisodeAndMin ? Integer.toString(intFirstEpisodeAndMin + intMaxEpisodes) : maxEpisodes;
	}

	private Map<String, Map<String, String>> getTitleHtmlAndPutInCache(String url,
			Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache) {
		HttpResponse response = httpCaller.call(url, HttpMethod.GET, animediaRequestParameters);
		Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(response);
		seasonsAndEpisodesCache.put(url, animeIdDataListsAndMaxEpisodesMap);
		return animeIdDataListsAndMaxEpisodesMap;
	}

	/**
	 * Compare multiseasons references and user watching titles and Creates container with matched anime Set poster url from MAL
	 *
	 * @param references the  multiseasons references
	 * @param watchingTitles the user watching titles
	 * @return the matched user references
	 */
	public Set<AnimediaMALTitleReferences> getMatchedReferences(Set<AnimediaMALTitleReferences> references, Set<UserMALTitleInfo> watchingTitles) {
		Set<AnimediaMALTitleReferences> tempReferences = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			references.stream().filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle())).forEach(set -> {
				set.setPosterUrl(userMALTitleInfo.getPosterUrl());
				tempReferences.add(new AnimediaMALTitleReferences(set));
			});
		}
		return tempReferences;
	}

	/**
	 * Compare multi seasons titles from animedia search list with multi seasons references from resources
	 *
	 * @param multiSeasonsAnime multi seasons titles from animedia
	 * @param allReferences all multi seasons references from resources
	 * @return true if multi seasons references from resources are full, if false then we must add the new reference to the raw mapping
	 */
	public boolean isReferencesAreFull(Set<Anime> multiSeasonsAnime, Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> readFromRaw = convertReferencesSetToMap(allReferences);
		return compareMaps(multiSeasonsAnime, readFromRaw);
	}

	/**
	 * Updates currentMax matched reference and set titleOnMal for currentlyUpdatedTitle
	 *
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param currentlyUpdatedTitle the currently updated title on animedia
	 */
	public void updateCurrentMax(Set<AnimediaMALTitleReferences> matchedAnimeFromCache, AnimediaMALTitleReferences currentlyUpdatedTitle) {
		matchedAnimeFromCache.stream()
				.filter(set -> set.getUrl().equals(currentlyUpdatedTitle.getUrl()) && set.getDataList().equals(currentlyUpdatedTitle.getDataList()))
				.forEach(set -> {
					set.setCurrentMax(getCorrectCurrentMax(currentlyUpdatedTitle.getCurrentMax()));
					currentlyUpdatedTitle.setTitleOnMAL(set.getTitleOnMAL());
				});
	}

	private Map<String, String> convertReferencesSetToMap(Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> urlTitle = new HashMap<>();
		String onlineAnimediaTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
		allReferences.forEach(set -> urlTitle
				.put(onlineAnimediaTv + set.getUrl() + "/" + set.getDataList() + "/" + set.getFirstEpisode(),
						set.getTitleOnMAL()));
		return urlTitle;
	}

	private boolean compareMaps(Set<Anime> multi, Map<String, String> raw) {
		boolean fullMatch = true;
		Set<Anime> missingReferences = new LinkedHashSet<>();
		for (Anime anime : multi) {
			String fullUrl = anime.getFullUrl();
			if (!raw.containsKey(fullUrl)) {
				fullMatch = false;
				missingReferences.add(anime);
			}
		}
		if (!missingReferences.isEmpty()) {
			RoutinesIO.marshalToFileInTheFolder(resourcesNames.getTempFolder(), resourcesNames.getTempRawReferences(), missingReferences);
		}
		return fullMatch;
	}
}
