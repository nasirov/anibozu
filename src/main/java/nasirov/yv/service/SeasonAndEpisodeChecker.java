package nasirov.yv.service;

import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class SeasonAndEpisodeChecker {

	private static final Map<String, String> EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH = new HashMap<>(1);

	private static final String JOINED_EPISODE_REGEXP = "\\d{1,3}-\\d{1,3}";

	static {
		EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH
				.put(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private String animediaOnlineTv;

	private Map<String, Map<String, String>> animediaRequestParameters;

	private Cache userMatchedAnimeCache;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	private CacheManager cacheManager;

	private ReferencesManager referencesManager;

	private UrlsNames urlsNames;

	@Autowired
	public SeasonAndEpisodeChecker(HttpCaller httpCaller,
			@Qualifier("animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder, AnimediaHTMLParser animediaHTMLParser,
			NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository, CacheManager cacheManager, ReferencesManager referencesManager,
			UrlsNames urlsNames) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.notFoundAnimeOnAnimediaRepository = notFoundAnimeOnAnimediaRepository;
		this.cacheManager = cacheManager;
		this.referencesManager = referencesManager;
		this.urlsNames = urlsNames;
	}

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
		userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		animediaOnlineTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
	}

	/**
	 * Creates a container with matched user titles
	 *
	 * @param watchingTitles     user watching titles
	 * @param references         multi seasons references
	 * @param animediaSearchList animedia search list
	 * @param username           the MAL username
	 * @return matched user titles(multi seasons and single season)
	 */
	public Set<AnimediaMALTitleReferences> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<AnimediaMALTitleReferences> references,
			Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		log.info("RESULT FOR {}:", username);
		Set<AnimediaMALTitleReferences> finalMatchedReferences = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences = references.stream()
					.filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle())).collect(Collectors.toSet());
			if (!matchedMultiSeasonsReferences.isEmpty()) {
				updateNewMatchedReferences(matchedMultiSeasonsReferences, userMALTitleInfo);
			}
			switch (matchedMultiSeasonsReferences.size()) {
				case 0:
					handleSingleSeasonTitle(animediaSearchList, userMALTitleInfo, finalMatchedReferences);
					break;
				case 1:
					handleOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, userMALTitleInfo, finalMatchedReferences);
					break;
				default:
					if (isMatchedReferencesOnSameDataList(matchedMultiSeasonsReferences)) {
						handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataList(matchedMultiSeasonsReferences,
								finalMatchedReferences,
								userMALTitleInfo);
					} else {
						handleMoreThanOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, finalMatchedReferences, userMALTitleInfo);
					}
					break;
			}
		}
		setMALPosterUrl(finalMatchedReferences, watchingTitles);
		userMatchedAnimeCache.putIfAbsent(username, finalMatchedReferences);
		return finalMatchedReferences;
	}

	/**
	 * Sets poster url from mal for final user matched references
	 *
	 * @param finalMatchedReferences the container for user matched references
	 * @param watchingTitles         user watching titles
	 */
	private void setMALPosterUrl(Set<AnimediaMALTitleReferences> finalMatchedReferences, Set<UserMALTitleInfo> watchingTitles) {
		for (UserMALTitleInfo title : watchingTitles) {
			finalMatchedReferences.stream().filter(matched -> matched.getTitleOnMAL().equals(title.getTitle()))
					.forEach(matched -> matched.setPosterUrl(title.getPosterUrl()));
		}
	}

	/**
	 * Handles single season titles from animedia search list
	 *
	 * @param animediaSearchList     animedia search list
	 * @param userMALTitleInfo       user watching title
	 * @param finalMatchedReferences the container for user matched references
	 */
	private void handleSingleSeasonTitle(Set<AnimediaTitleSearchInfo> animediaSearchList, UserMALTitleInfo userMALTitleInfo,
			Set<AnimediaMALTitleReferences> finalMatchedReferences) {
		int matchedCountOfSingleSeasonAnime = (int) animediaSearchList.stream().filter(list -> list.getKeywords().equals(userMALTitleInfo.getTitle()))
				.count();
		switch (matchedCountOfSingleSeasonAnime) {
			case 0:
				handleZeroMatchedResultInAnimediaSearchList(userMALTitleInfo);
				break;
			case 1:
				handleOneMatchedResultInAnimediaSearchList(animediaSearchList, userMALTitleInfo, finalMatchedReferences);
				break;
			default:
				handleMoreThanOneMatchedResultInAnimediaSearchList(userMALTitleInfo);
				break;
		}
	}

	/**
	 * Handles one matched result in the multi seasons references
	 *
	 * @param matchedMultiSeasonsReferences the matched multi seasons reference
	 * @param userMALTitleInfo              the user title info
	 * @param finalMatchedReferences        the container for user matched references
	 */
	private void handleOneMatchedResultInMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
			UserMALTitleInfo userMALTitleInfo, Set<AnimediaMALTitleReferences> finalMatchedReferences) {
		String episodeNumber = null;
		for (AnimediaMALTitleReferences reference : matchedMultiSeasonsReferences) {
			int episodeNumberForWatch;
			int firstEpisode = Integer.parseInt(reference.getFirstEpisode());
			if (isTitleConcretizedOnMAL(reference)) {
				episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedReferences(reference, userMALTitleInfo);
				episodeNumber = String.valueOf(userMALTitleInfo.getNumWatchedEpisodes() + 1);
			} else {
				episodeNumberForWatch = firstEpisode + userMALTitleInfo.getNumWatchedEpisodes();
			}
			String finalEpisodeNumber = episodeNumber;
			Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, episodeNumberForWatch);
			Stream.of(nextEpisodeForWatchFinalUrl).flatMap(map -> map.entrySet().stream()).forEach(entry -> {
				reference.setEpisodeNumberForWatch(
						!entry.getKey().equals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) && finalEpisodeNumber != null
								? finalEpisodeNumber : entry.getKey());
				reference.setFinalUrl(entry.getValue());
			});
			finalMatchedReferences.add(new AnimediaMALTitleReferences(reference));
		}
	}

	/**
	 * Get episode number for watch for titles with concretized episodes on MAL Used for references with concretized episodes on MAL for example, 1-2
	 * https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param animediaMALTitleReferences title reference
	 * @param userMALTitleInfo           mal title
	 * @return correct episode number for watch
	 */
	private int getEpisodeNumberForWatchForConcretizedReferences(AnimediaMALTitleReferences animediaMALTitleReferences,
			UserMALTitleInfo userMALTitleInfo) {
		int episodeNumberForWatch;
		int nextEpisodeNumber = userMALTitleInfo.getNumWatchedEpisodes() + 1;
		int intMinConcretizedEpisodeOnMAL = Integer.parseInt(animediaMALTitleReferences.getMinConcretizedEpisodeOnMAL());
		int intMaxConcretizedEpisodeOnMAL = Integer.parseInt(animediaMALTitleReferences.getMaxConcretizedEpisodeOnMAL());
		int min = Integer.parseInt(animediaMALTitleReferences.getMinConcretizedEpisodeOnAnimedia());
		int max = Integer.parseInt(animediaMALTitleReferences.getMaxConcretizedEpisodeOnAnimedia());
		int firstEpisode = Integer.parseInt(animediaMALTitleReferences.getFirstEpisode());
		if (nextEpisodeNumber >= intMinConcretizedEpisodeOnMAL && nextEpisodeNumber <= intMaxConcretizedEpisodeOnMAL) {
			if (min != max) {
				int stepFromFirstEpisode = nextEpisodeNumber - intMinConcretizedEpisodeOnMAL;
				episodeNumberForWatch = firstEpisode + stepFromFirstEpisode;
			} else {
				episodeNumberForWatch = firstEpisode;
			}
		} else {
			episodeNumberForWatch = Integer.parseInt(animediaMALTitleReferences.getCurrentMax()) + 1;
		}
		return episodeNumberForWatch;
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when one season separated on several tabs for example,
	 * http://online.animedia.tv/anime/one-piece-van-pis-tv/
	 *
	 * @param matchedMultiSeasonsReferences the matched multi seasons references
	 * @param finalMatchedReferences        the container for user matched references
	 */
	private void handleMoreThanOneMatchedResultInMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
			Set<AnimediaMALTitleReferences> finalMatchedReferences, UserMALTitleInfo userMALTitleInfo) {
		//Increment to next episode for watch
		int nextNumberOfEpisodeForWatch = userMALTitleInfo.getNumWatchedEpisodes() + 1;
		int undefinedMax = 0;
		for (AnimediaMALTitleReferences animediaMALTitleReferences : matchedMultiSeasonsReferences) {
			String maxEpisodesInDataListOnAnimedia = animediaMALTitleReferences.getMaxConcretizedEpisodeOnAnimedia();
			Integer intMax = isMaxEpisodeUndefined(maxEpisodesInDataListOnAnimedia) ? undefinedMax : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
			if (nextNumberOfEpisodeForWatch >= Integer.parseInt(animediaMALTitleReferences.getFirstEpisode()) && (nextNumberOfEpisodeForWatch <= intMax
					|| intMax.equals(undefinedMax))) {
				Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(animediaMALTitleReferences, nextNumberOfEpisodeForWatch);
				Stream.of(nextEpisodeForWatchFinalUrl).flatMap(map -> map.entrySet().stream()).forEach(entry -> {
					animediaMALTitleReferences.setEpisodeNumberForWatch(entry.getKey());
					animediaMALTitleReferences.setFinalUrl(entry.getValue());
				});
				finalMatchedReferences.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
				break;
			}
		}
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when data list contain several titles for example, 1-2
	 * https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param matchedMultiSeasonsReferences the references with equals titles and data lists
	 * @param finalMatchedReferences        the container for user matched references
	 * @param userMALTitleInfo              user watching title
	 */
	private void handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataList(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
			Set<AnimediaMALTitleReferences> finalMatchedReferences, UserMALTitleInfo userMALTitleInfo) {
		int nextNumberOfEpisodeForWatch;
		AnimediaMALTitleReferences animediaMALTitleReferences;
		int nextEpisodeNumber = userMALTitleInfo.getNumWatchedEpisodes() + 1;
		long matched = matchedMultiSeasonsReferences.stream().filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinConcretizedEpisodeOnMAL()))
				.count();
		if (matched == 1) {
			animediaMALTitleReferences = matchedMultiSeasonsReferences.stream()
					.filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinConcretizedEpisodeOnMAL())).findFirst().get();
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(animediaMALTitleReferences, userMALTitleInfo);
		} else {
			animediaMALTitleReferences = matchedMultiSeasonsReferences.stream()
					.sorted((x, y) -> -x.getMinConcretizedEpisodeOnMAL().compareTo(y.getMinConcretizedEpisodeOnMAL())).findFirst().get();
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(animediaMALTitleReferences, userMALTitleInfo);
		}
		Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(animediaMALTitleReferences, nextNumberOfEpisodeForWatch);
		Stream.of(nextEpisodeForWatchFinalUrl).flatMap(map -> map.entrySet().stream()).forEach(entry -> {
			animediaMALTitleReferences.setEpisodeNumberForWatch(
					entry.getKey().equals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) ? entry.getKey()
							: String.valueOf(nextEpisodeNumber));
			animediaMALTitleReferences.setFinalUrl(entry.getValue());
		});
		finalMatchedReferences.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
	}

	/**
	 * Handles one matched result in the animedia search list
	 *
	 * @param animediaSearchList     the animedia search list
	 * @param userMALTitleInfo       the user title info
	 * @param finalMatchedReferences the container for user matched references
	 */
	private void handleOneMatchedResultInAnimediaSearchList(Set<AnimediaTitleSearchInfo> animediaSearchList, UserMALTitleInfo userMALTitleInfo,
			Set<AnimediaMALTitleReferences> finalMatchedReferences) {
		//Increment to next episode for watch
		int nextNumberOfEpisodeForWatch = userMALTitleInfo.getNumWatchedEpisodes() + 1;
		AnimediaTitleSearchInfo matchedOfSingleSeasonAnime = animediaSearchList.stream()
				.filter(list -> list.getKeywords().equals(userMALTitleInfo.getTitle())).findFirst().orElse(null);
		if (matchedOfSingleSeasonAnime != null) {
			String url = matchedOfSingleSeasonAnime.getUrl();
			HttpResponse response = httpCaller.call(animediaOnlineTv + url, HttpMethod.GET, animediaRequestParameters);
			if (isAnnouncement(response.getContent())) {
				handleAnnouncements(url, userMALTitleInfo.getTitle(), matchedOfSingleSeasonAnime.getPosterUrl(), finalMatchedReferences);
				return;
			}
			Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
			for (Map.Entry<String, Map<String, String>> animeIdDataListsAndEpisodes : animeIdSeasonsAndEpisodesMap.entrySet()) {
				for (Map.Entry<String, String> dataListsAndEpisodes : animeIdDataListsAndEpisodes.getValue().entrySet()) {
					String dataList = dataListsAndEpisodes.getKey();
					HttpResponse resp = httpCaller.call(
							urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesList() + animeIdDataListsAndEpisodes.getKey() + "/" + dataList + urlsNames
									.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesPostfix(), HttpMethod.GET, animediaRequestParameters);
					Map<String, List<String>> maxEpisodesAndEpisodesRange = animediaHTMLParser.getEpisodesRange(resp);
					for (Map.Entry<String, List<String>> maxEpisodesAndEpisodesRangeEntry : maxEpisodesAndEpisodesRange.entrySet()) {
						List<String> episodesRange = maxEpisodesAndEpisodesRangeEntry.getValue();
						String firstEpisode = getFirstEpisode(episodesRange);
						String lastEpisode = getLastEpisode(episodesRange);
						String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(firstEpisode);
						String correctCurrentMax = getCorrectCurrentMax(lastEpisode);
						AnimediaMALTitleReferences temp =
								AnimediaMALTitleReferences.builder().url(url).dataList(dataList).firstEpisode(correctFirstEpisodeAndMin)
								.titleOnMAL(userMALTitleInfo.getTitle()).minConcretizedEpisodeOnAnimedia(correctFirstEpisodeAndMin)
								.maxConcretizedEpisodeOnAnimedia(maxEpisodesAndEpisodesRangeEntry.getKey()).currentMax(correctCurrentMax)
								.posterUrl(matchedOfSingleSeasonAnime.getPosterUrl()).episodesRange(episodesRange).build();
						Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(temp, nextNumberOfEpisodeForWatch);
						Stream.of(nextEpisodeForWatchFinalUrl).flatMap(map -> map.entrySet().stream()).forEach(entry -> {
							temp.setFinalUrl(entry.getValue());
							temp.setEpisodeNumberForWatch(entry.getKey());
							finalMatchedReferences.add(temp);
						});
					}
				}
			}
		}
	}

	/**
	 * Handles zero matched result in the animedia search list
	 *
	 * @param userMALTitleInfo the user title info
	 */
	private void handleZeroMatchedResultInAnimediaSearchList(UserMALTitleInfo userMALTitleInfo) {
		log.info("ANIME {}({}) IS NOT FOUND ON ANIMEDIA!", userMALTitleInfo.getTitle(), userMALTitleInfo.getAnimeUrl());
		if (!notFoundAnimeOnAnimediaRepository.exitsByTitle(userMALTitleInfo.getTitle())) {
			notFoundAnimeOnAnimediaRepository.saveAndFlush(userMALTitleInfo);
		}
	}

	/**
	 * Handles more than one matched result in the animedia search list
	 *
	 * @param userMALTitleInfo the user title info
	 */
	private void handleMoreThanOneMatchedResultInAnimediaSearchList(UserMALTitleInfo userMALTitleInfo) {
		log.error("WATCHING TITLE NAME {} DUPLICATED IN ANIMEDIA SEARCH LIST KEYWORDS. CHECK ANIMEDIA SEARCH LIST RESOURCE!", userMALTitleInfo);
	}


	private void handleAnnouncements(String url, String titleOnMal, String posterUrl, Set<AnimediaMALTitleReferences> finalMatchedReferences) {
		AnimediaMALTitleReferences temp = AnimediaMALTitleReferences.builder().url(url).dataList(BaseConstants.FIRST_DATA_LIST)
				.firstEpisode(BaseConstants.FIRST_EPISODE).titleOnMAL(titleOnMal).minConcretizedEpisodeOnAnimedia(BaseConstants.ZERO_EPISODE)
				.maxConcretizedEpisodeOnAnimedia(BaseConstants.ZERO_EPISODE).currentMax(BaseConstants.ZERO_EPISODE).posterUrl(posterUrl)
				.finalUrl(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE).build();
		finalMatchedReferences.add(temp);
		String finalUrl = animediaOnlineTv + temp.getUrl();
		log.info("NEW EPISODE FOR {} IS NOT AVAILABLE BECAUSE IT'S ANNOUNCEMENT", finalUrl);
	}

	private boolean isMatchedReferencesOnSameDataList(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences) {
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> ref.getDataList().equals(matchedMultiSeasonsReferences.stream().findFirst().get().getDataList())).count() > 1;
	}

	private void updateNewMatchedReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences, UserMALTitleInfo userMALTitleInfo) {
		Set<AnimediaMALTitleReferences> tempReferences = new LinkedHashSet<>();
		matchedMultiSeasonsReferences.stream().filter(set -> set.getCurrentMax() == null || set.getMinConcretizedEpisodeOnAnimedia() == null
				|| set.getMaxConcretizedEpisodeOnAnimedia() == null).forEach(ref -> {
			ref.setPosterUrl(userMALTitleInfo.getPosterUrl());
			tempReferences.add(ref);
		});
		if (!tempReferences.isEmpty()) {
			referencesManager.updateReferences(tempReferences);
		}
	}

	/**
	 * Updates the matched references (episode number for watch, final url for front)
	 *
	 * @param watchingTitles        the user watching titles
	 * @param references            the currently updated title on animedia
	 * @param matchedAnimeFromCache the matched user anime from cache
	 */
	public void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> watchingTitles, AnimediaMALTitleReferences references,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache) {
		log.info("UPDATING MATCHED REFERENCES...");
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			//Increment to next episode for watch
			Integer episodeNumberForWatch;
			Map<String, String> nextEpisodeForWatchAndFinalUrl;
			AnimediaMALTitleReferences referenceForUpdate = null;
			if (references.getTitleOnMAL().equals(userMALTitleInfo.getTitle())) {
				referenceForUpdate = matchedAnimeFromCache.stream()
						.filter(title -> title.getTitleOnMAL().equals(references.getTitleOnMAL()) && title.getDataList().equals(references.getDataList()))
						.findFirst().orElse(null);
			}
			if (referenceForUpdate != null) {
				int firstEpisode = Integer.parseInt(referenceForUpdate.getFirstEpisode());
				if (firstEpisode == 0 || firstEpisode == 1) {
					episodeNumberForWatch = firstEpisode + userMALTitleInfo.getNumWatchedEpisodes();
				} else {
					episodeNumberForWatch = userMALTitleInfo.getNumWatchedEpisodes() + 1;
				}
				nextEpisodeForWatchAndFinalUrl = getNextEpisodeForWatchAndFinalUrl(referenceForUpdate, episodeNumberForWatch);
				for (Map.Entry<String, String> nextEpisodeForWatchFinalUrl : nextEpisodeForWatchAndFinalUrl.entrySet()) {
					referenceForUpdate.setEpisodeNumberForWatch(nextEpisodeForWatchFinalUrl.getKey());
					referenceForUpdate.setFinalUrl(nextEpisodeForWatchFinalUrl.getValue());
				}
			}
		}
	}

	/**
	 * Updates the matched references (episode number for watch, final url for front)
	 *
	 * @param updatedWatchingTitles the user watching with updated number of watched episodes
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param animediaSearchList    animedia search list
	 * @param username              the MAL username
	 */
	public void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> updatedWatchingTitles,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		Integer episodeNumberForWatch;
		Map<String, String> nextEpisodeForWatchAndFinalUrl;
		Set<AnimediaMALTitleReferences> allMultiSeasonsReferences = referencesManager.getMultiSeasonsReferences();
		log.info("UPDATING MATCHED REFERENCES...");
		for (UserMALTitleInfo updatedTitle : updatedWatchingTitles) {
			AnimediaMALTitleReferences reference = matchedAnimeFromCache.stream().filter(ref -> ref.getTitleOnMAL().equals(updatedTitle.getTitle()))
					.findFirst().orElse(null);
			if (reference != null) {
				long countOfMatchedMultiSeasonsReferencesWithEqualsTitleName = allMultiSeasonsReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(updatedTitle.getTitle())).count();
				if (countOfMatchedMultiSeasonsReferencesWithEqualsTitleName > 1) {
					Set<UserMALTitleInfo> tempWatchingTitles = new LinkedHashSet<>();
					tempWatchingTitles.add(updatedTitle);
					Set<AnimediaMALTitleReferences> tempReferences = allMultiSeasonsReferences.stream()
							.filter(ref -> ref.getTitleOnMAL().equals(updatedTitle.getTitle())).collect(Collectors.toSet());
					Set<AnimediaMALTitleReferences> tempMatchedAnime = getMatchedAnime(tempWatchingTitles, tempReferences, animediaSearchList, username);
					Iterator<AnimediaMALTitleReferences> iterator = matchedAnimeFromCache.iterator();
					while (iterator.hasNext()) {
						AnimediaMALTitleReferences next = iterator.next();
						if (next.equals(reference)) {
							iterator.remove();
						}
					}
					reference = tempMatchedAnime.stream().findFirst().get();
					matchedAnimeFromCache.add(reference);
					continue;
				}
				String nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = null;
				int firstEpisode = Integer.parseInt(reference.getFirstEpisode());
				if (isTitleConcretizedOnMAL(reference)) {
					episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedReferences(reference, updatedTitle);
					nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = String.valueOf(updatedTitle.getNumWatchedEpisodes() + 1);
				} else {
					episodeNumberForWatch = firstEpisode + updatedTitle.getNumWatchedEpisodes();
				}
				nextEpisodeForWatchAndFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, episodeNumberForWatch);
				for (Map.Entry<String, String> nextEpisodeForWatchFinalUrl : nextEpisodeForWatchAndFinalUrl.entrySet()) {
					String nextEpisodeNumber = nextEpisodeForWatchFinalUrl.getKey();
					reference.setEpisodeNumberForWatch(nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL != null && !nextEpisodeNumber
							.equals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
							? nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL : nextEpisodeNumber);
					reference.setFinalUrl(nextEpisodeForWatchFinalUrl.getValue());
				}
			}
		}
	}

	/**
	 * Creates map with next episode for watch and final url for title
	 *
	 * @param animediaMALTitleReferences matched title
	 * @param episodeNumberForWatch      next episode for watch
	 * @return correct nextEpisodeForWatch and finalURL if new episode is available or "","" if new episode is not available
	 */
	private Map<String, String> getNextEpisodeForWatchAndFinalUrl(AnimediaMALTitleReferences animediaMALTitleReferences, int episodeNumberForWatch) {
		String finalUrl;
		Map<String, String> nextEpisodeForWatchFinalUrl;
		if (episodeNumberForWatch <= Integer.parseInt(animediaMALTitleReferences.getCurrentMax())) {
			nextEpisodeForWatchFinalUrl = new HashMap<>();
			String episodeNumberForWatchForFront = String.valueOf(episodeNumberForWatch);
			String episodeNumberForWatchForURL = episodeNumberForWatchForFront;
			List<String> episodesRange = animediaMALTitleReferences.getEpisodesRange();
			if (!isTitleConcretizedOnMAL(animediaMALTitleReferences) && isNextEpisodeForWatchInJoinedEpisode(episodeNumberForWatchForFront,
					episodesRange)) {
				String[] episodeNumberForWatchForURLandFront = getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(episodeNumberForWatchForFront,
						episodesRange);
				episodeNumberForWatchForURL = episodeNumberForWatchForURLandFront[0];
				episodeNumberForWatchForFront = episodeNumberForWatchForURLandFront[1];
			}
			finalUrl =
					animediaOnlineTv + animediaMALTitleReferences.getUrl() + "/" + animediaMALTitleReferences.getDataList() + "/" + episodeNumberForWatchForURL;
			log.info("NEW EPISODE IS AVAILABLE {} !", finalUrl);
			nextEpisodeForWatchFinalUrl.put(episodeNumberForWatchForFront, finalUrl);
		} else {
			nextEpisodeForWatchFinalUrl = EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH;
			finalUrl =
					animediaOnlineTv + animediaMALTitleReferences.getUrl() + "/" + animediaMALTitleReferences.getDataList() + "/" + animediaMALTitleReferences
							.getFirstEpisode();
			log.info("NEW EPISODE FOR {} IS NOT AVAILABLE.", finalUrl);
		}
		return nextEpisodeForWatchFinalUrl;
	}

	/**
	 * Finds joined episode in episodes range
	 *
	 * @param episodeNumberForWatch next episode for watch
	 * @param episodesRange         episodes range from min to max
	 * @return true if episodeNumberForWatch is contains in joined episode, false in other case
	 */
	private boolean isNextEpisodeForWatchInJoinedEpisode(String episodeNumberForWatch, List<String> episodesRange) {
		return episodesRange.stream().filter(episodes -> episodes.matches(JOINED_EPISODE_REGEXP)).map(episodes -> episodes.split("-"))
				.anyMatch(episodesArray -> isEpisodeInRange(episodesArray, episodeNumberForWatch));
	}

	/**
	 * Creates array with episode number for URL and front if joined episode is present
	 *
	 * @param episodeNumberForWatch next episode for watch
	 * @param episodesRange         episodes range from min to max
	 * @return array with [0] - episode number for URL, [1] - episode number for front
	 */
	private String[] getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(String episodeNumberForWatch, List<String> episodesRange) {
		String[] result = new String[2];
		String[] splittedEpisodes = episodesRange.stream().filter(episodes -> episodes.matches(JOINED_EPISODE_REGEXP))
				.map(episodes -> episodes.split("-")).filter(episodesArray -> isEpisodeInRange(episodesArray, episodeNumberForWatch)).findFirst()
				.orElse(null);
		if (splittedEpisodes != null) {
			result[0] = splittedEpisodes[0];
			result[1] = episodeNumberForWatch;
		}
		return result;
	}

	private boolean isEpisodeInRange(String[] episodesArray, String episodeNumberForWatch) {
		return Integer.parseInt(episodesArray[0]) <= Integer.parseInt(episodeNumberForWatch)
				&& Integer.parseInt(episodesArray[episodesArray.length - 1]) >= Integer.parseInt(episodeNumberForWatch);
	}

}
