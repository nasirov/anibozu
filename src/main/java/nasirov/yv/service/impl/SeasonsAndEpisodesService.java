package nasirov.yv.service.impl;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.max;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.data.constants.BaseConstants.ZERO_EPISODE;
import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getDataList;
import static nasirov.yv.util.AnimediaUtils.getEpisodesRange;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.getMaxEpisode;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnimediaFeignClient;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import nasirov.yv.util.AnimediaUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SeasonsAndEpisodesService implements SeasonsAndEpisodesServiceI {

	private static final Map<String, String> EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH = new HashMap<>(1);

	private static final String JOINED_EPISODE_REGEXP = "\\d{1,3}-\\d{1,3}";

	private static final int UNDEFINED_MAX = 0;

	static {
		EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH.put(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private final AnimediaFeignClient animediaFeignClient;

	private final AnimediaHTMLParser animediaHTMLParser;

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	private final ReferencesServiceI referencesManager;

	private final UrlsNames urlsNames;

	private String animediaOnlineTv;

	@PostConstruct
	public void init() {
		animediaOnlineTv = urlsNames.getAnimediaUrls()
				.getOnlineAnimediaTv();
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
	@Override
	@CachePut(value = "userMatchedAnimeCache", key = "#username", condition = "#root.caches[0].get(#username) == null")
	public Set<AnimediaMALTitleReferences> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<AnimediaMALTitleReferences> references,
			Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		log.info("RESULT FOR {}:", username);
		return watchingTitles.stream()
				.map(userMALTitleInfo -> findMatchedAnime(references, animediaSearchList, userMALTitleInfo))
				.filter(Objects::nonNull)
				.map(ref -> setMALPosterUrl(ref, watchingTitles))
				.collect(Collectors.toSet());
	}

	private AnimediaMALTitleReferences findMatchedAnime(Set<AnimediaMALTitleReferences> references, Set<AnimediaTitleSearchInfo> animediaSearchList,
			UserMALTitleInfo userMALTitleInfo) {
		AnimediaMALTitleReferences result;
		Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences = getMatchedReferences(references, userMALTitleInfo);
		if (matchedMultiSeasonsReferences.isEmpty()) {
			result = handleSingleSeasonTitle(animediaSearchList, userMALTitleInfo);
		} else {
			result = handleMultiSeasonsReferences(matchedMultiSeasonsReferences, userMALTitleInfo);
		}
		return result;
	}

	private AnimediaMALTitleReferences handleSingleSeasonTitle(Set<AnimediaTitleSearchInfo> animediaSearchList, UserMALTitleInfo userMALTitleInfo) {
		AnimediaMALTitleReferences result = null;
		List<AnimediaTitleSearchInfo> matchedSingleSeasonAnime = getMatchedSingleSeasonAnime(animediaSearchList, userMALTitleInfo);
		switch (matchedSingleSeasonAnime.size()) {
			case 0:
				handleZeroMatchedResultInAnimediaSearchList(userMALTitleInfo);
				break;
			case 1:
				result = handleOneMatchedResultInAnimediaSearchList(get(matchedSingleSeasonAnime, 0), userMALTitleInfo);
				break;
			default:
				handleMoreThanOneMatchedResultInAnimediaSearchList(userMALTitleInfo);
				break;
		}
		return result;
	}

	private AnimediaMALTitleReferences handleMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
			UserMALTitleInfo userMALTitleInfo) {
		AnimediaMALTitleReferences result;
		if (matchedMultiSeasonsReferences.size() == 1) {
			result = handleOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, userMALTitleInfo);
		} else {
			if (isMatchedReferencesOnSameDataList(matchedMultiSeasonsReferences)) {
				result = handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataList(matchedMultiSeasonsReferences, userMALTitleInfo);
			} else {
				result = handleMoreThanOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, userMALTitleInfo);
			}
		}
		return result;
	}

	private void handleZeroMatchedResultInAnimediaSearchList(UserMALTitleInfo userMALTitleInfo) {
		log.info("ANIME {}({}) IS NOT FOUND ON ANIMEDIA!", userMALTitleInfo.getTitle(), userMALTitleInfo.getAnimeUrl());
		if (!notFoundAnimeOnAnimediaRepository.exitsByTitle(userMALTitleInfo.getTitle())) {
			notFoundAnimeOnAnimediaRepository.saveAndFlush(userMALTitleInfo);
		}
	}

	private AnimediaMALTitleReferences handleOneMatchedResultInAnimediaSearchList(AnimediaTitleSearchInfo matchedOfSingleSeasonAnime,
			UserMALTitleInfo userMALTitleInfo) {
		String url = matchedOfSingleSeasonAnime.getUrl();
		ResponseEntity<String> responseWithAnimePage = animediaFeignClient.getAnimePageWithDataLists(url);
		String animePage = ofNullable(responseWithAnimePage.getBody()).orElse("");
		if (isAnnouncement(animePage)) {
			return handleAnnouncements(url, userMALTitleInfo.getTitle(), matchedOfSingleSeasonAnime.getPosterUrl());
		}
		Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(animePage);
		String animeId = AnimediaUtils.getAnimeId(animeIdDataListsAndMaxEpisodesMap);
		Map<String, String> dataListsAndMaxEpisodesMap = AnimediaUtils.getDataListsAndMaxEpisodesMap(animeIdDataListsAndMaxEpisodesMap);
		String dataList = getDataList(dataListsAndMaxEpisodesMap);
		ResponseEntity<String> responseWithDataList = animediaFeignClient.getDataListWithEpisodes(animeId, dataList);
		Map<String, List<String>> maxEpisodesAndEpisodesRange = animediaHTMLParser.getEpisodesRange(responseWithDataList.getBody());
		List<String> episodesRange = getEpisodesRange(maxEpisodesAndEpisodesRange);
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesRange));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesRange));
		AnimediaMALTitleReferences temp = AnimediaMALTitleReferences.builder()
				.url(url)
				.dataList(dataList)
				.firstEpisode(correctFirstEpisodeAndMin)
				.titleOnMAL(userMALTitleInfo.getTitle())
				.currentMax(correctCurrentMax)
				.minConcretizedEpisodeOnAnimedia(correctFirstEpisodeAndMin)
				.maxConcretizedEpisodeOnAnimedia(getMaxEpisode(maxEpisodesAndEpisodesRange))
				.posterUrl(matchedOfSingleSeasonAnime.getPosterUrl())
				.episodesRange(episodesRange)
				.build();
		return enrichReference(temp, getEpisodeNumberForWatch(userMALTitleInfo));
	}

	private void handleMoreThanOneMatchedResultInAnimediaSearchList(UserMALTitleInfo userMALTitleInfo) {
		log.error("WATCHING TITLE NAME {} DUPLICATED IN ANIMEDIA SEARCH LIST KEYWORDS. CHECK ANIMEDIA SEARCH LIST RESOURCE!", userMALTitleInfo);
	}

	private AnimediaMALTitleReferences handleOneMatchedResultInMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
			UserMALTitleInfo userMALTitleInfo) {
		return matchedMultiSeasonsReferences.stream()
				.map(x -> enrichReference(x, userMALTitleInfo))
				.map(AnimediaMALTitleReferences::new)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when data list contain several titles for example, 1-2
	 * https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param matchedMultiSeasonsReferences the references with equals titles and data lists
	 * @param userMALTitleInfo              user watching title
	 */
	private AnimediaMALTitleReferences handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataList(
			Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences, UserMALTitleInfo userMALTitleInfo) {
		int nextNumberOfEpisodeForWatch;
		AnimediaMALTitleReferences animediaMALTitleReferences;
		int nextEpisodeNumber = getEpisodeNumberForWatch(userMALTitleInfo);
		List<AnimediaMALTitleReferences> matched = matchedMultiSeasonsReferences.stream()
				.filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinConcretizedEpisodeOnMAL()))
				.collect(Collectors.toList());
		if (matched.size() == 1) {
			animediaMALTitleReferences = get(matched, 0);
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(animediaMALTitleReferences, userMALTitleInfo);
		} else {
			animediaMALTitleReferences = max(matchedMultiSeasonsReferences, comparing(AnimediaMALTitleReferences::getMinConcretizedEpisodeOnMAL));
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(animediaMALTitleReferences, userMALTitleInfo);
		}
		Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(animediaMALTitleReferences, nextNumberOfEpisodeForWatch);
		String numberForWatch = extractEpisodeNumberForWatch(nextEpisodeForWatchFinalUrl);
		animediaMALTitleReferences.setEpisodeNumberForWatch(
				numberForWatch.equals(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) ? numberForWatch : String.valueOf(nextEpisodeNumber));
		animediaMALTitleReferences.setFinalUrl(extractFinalUrl(nextEpisodeForWatchFinalUrl));
		return new AnimediaMALTitleReferences(animediaMALTitleReferences);
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when one season separated on several tabs for example,
	 * http://online.animedia.tv/anime/one-piece-van-pis-tv/
	 *
	 * @param matchedMultiSeasonsReferences the matched multi seasons references
	 */
	private AnimediaMALTitleReferences handleMoreThanOneMatchedResultInMultiSeasonsReferences(
			Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences, UserMALTitleInfo userMALTitleInfo) {
		int nextNumberOfEpisodeForWatch = getEpisodeNumberForWatch(userMALTitleInfo);
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> isNextNumberOfEpisodeForWatchInReferenceEpisodesRange(nextNumberOfEpisodeForWatch, ref))
				.map(ref -> enrichReference(ref, nextNumberOfEpisodeForWatch))
				.findFirst()
				.orElse(null);
	}

	private AnimediaMALTitleReferences handleAnnouncements(String url, String titleOnMal, String posterUrl) {
		AnimediaMALTitleReferences result = AnimediaMALTitleReferences.builder()
				.url(url)
				.dataList(FIRST_DATA_LIST)
				.firstEpisode(FIRST_EPISODE)
				.titleOnMAL(titleOnMal)
				.minConcretizedEpisodeOnAnimedia(ZERO_EPISODE)
				.maxConcretizedEpisodeOnAnimedia(ZERO_EPISODE)
				.currentMax(ZERO_EPISODE)
				.posterUrl(posterUrl)
				.finalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
		String finalUrl = animediaOnlineTv + result.getUrl();
		log.info("NEW EPISODE FOR {} IS NOT AVAILABLE BECAUSE IT'S ANNOUNCEMENT", finalUrl);
		return result;
	}
	private AnimediaMALTitleReferences setMALPosterUrl(AnimediaMALTitleReferences finalMatchedReference, Set<UserMALTitleInfo> watchingTitles) {
		watchingTitles.stream()
				.filter(x -> x.getTitle()
						.equals(finalMatchedReference.getTitleOnMAL()))
				.findFirst()
				.ifPresent(x -> finalMatchedReference.setPosterUrl(x.getPosterUrl()));
		return finalMatchedReference;
	}

	private Set<AnimediaMALTitleReferences> getMatchedReferences(Set<AnimediaMALTitleReferences> allReferences, UserMALTitleInfo userMALTitleInfo) {
		return allReferences.stream()
				.filter(set -> set.getTitleOnMAL()
						.equals(userMALTitleInfo.getTitle()))
				.collect(Collectors.toSet());
	}

	private List<AnimediaTitleSearchInfo> getMatchedSingleSeasonAnime(Set<AnimediaTitleSearchInfo> animediaSearchList,
			UserMALTitleInfo userMALTitleInfo) {
		return animediaSearchList.stream()
				.filter(list -> list.getKeywords()
						.equals(userMALTitleInfo.getTitle()))
				.collect(Collectors.toList());
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
		int nextEpisodeNumber = getEpisodeNumberForWatch(userMALTitleInfo);
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


	private boolean isMatchedReferencesOnSameDataList(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences) {
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> ref.getDataList()
						.equals(matchedMultiSeasonsReferences.stream()
								.findFirst()
								.orElseGet(AnimediaMALTitleReferences::new)
								.getDataList()))
				.count() > 1;
	}

	private int getEpisodeNumberForWatch(UserMALTitleInfo userMALTitleInfo) {
		return userMALTitleInfo.getNumWatchedEpisodes() + 1;
	}

	/**
	 * Updates the matched reference (episode number for watch, final url for front)
	 *
	 * @param watchingTitles                  the user watching titles
	 * @param currentlyUpdatedTitleOnAnimedia the currently updated title on animedia
	 * @param matchedAnimeFromCache           the matched user anime from cache
	 */
	@Override
	public void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> watchingTitles,
			AnimediaMALTitleReferences currentlyUpdatedTitleOnAnimedia,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache) {
		log.info("UPDATING MATCHED REFERENCES...");
		watchingTitles.stream()
				.filter(x -> currentlyUpdatedTitleOnAnimedia.getTitleOnMAL()
						.equals(x.getTitle()))
				.forEach(x -> findAndUpdateReference(matchedAnimeFromCache, currentlyUpdatedTitleOnAnimedia, x));
	}

	private void findAndUpdateReference(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			AnimediaMALTitleReferences currentlyUpdatedTitleOnAniedia, UserMALTitleInfo userMALTitleInfo) {
		matchedAnimeFromCache.stream()
				.filter(title -> title.getTitleOnMAL()
						.equals(currentlyUpdatedTitleOnAniedia.getTitleOnMAL()) && title.getDataList()
						.equals(currentlyUpdatedTitleOnAniedia.getDataList()))
				.findFirst()
				.ifPresent(x -> enrichReference(x, getEpisodeNumberForWatch(x, userMALTitleInfo)));
	}

	private int getEpisodeNumberForWatch(AnimediaMALTitleReferences referenceForUpdate, UserMALTitleInfo userMALTitleInfo) {
		int firstEpisode = Integer.parseInt(referenceForUpdate.getFirstEpisode());
		return firstEpisode == 0 ? userMALTitleInfo.getNumWatchedEpisodes() : getEpisodeNumberForWatch(userMALTitleInfo);
	}

	/**
	 * Updates the matched references (episode number for watch, final url for front)
	 *
	 * @param updatedWatchingTitles the user watching with updated number of watched episodes
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param animediaSearchList    animedia search list
	 * @param username              the MAL username
	 */
	@Override
	public void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> updatedWatchingTitles,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		Set<AnimediaMALTitleReferences> allMultiSeasonsReferences = referencesManager.getMultiSeasonsReferences();
		log.info("UPDATING MATCHED REFERENCES...");
		updatedWatchingTitles.forEach(updatedTitle -> findAndUpdateReference(matchedAnimeFromCache,
				updatedTitle,
				allMultiSeasonsReferences,
				animediaSearchList,
				username));
	}

	private void findAndUpdateReference(Set<AnimediaMALTitleReferences> matchedAnimeFromCache, UserMALTitleInfo updatedTitle,
			Set<AnimediaMALTitleReferences> allMultiSeasonsReferences, Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		matchedAnimeFromCache.stream()
				.filter(ref -> ref.getTitleOnMAL()
						.equals(updatedTitle.getTitle()))
				.findFirst()
				.ifPresent(ref -> updateReference(allMultiSeasonsReferences, updatedTitle, ref, matchedAnimeFromCache, animediaSearchList, username));
	}

	private void updateReference(Set<AnimediaMALTitleReferences> allMultiSeasonsReferences, UserMALTitleInfo updatedTitle,
			AnimediaMALTitleReferences reference, Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<AnimediaTitleSearchInfo> animediaSearchList,
			String username) {
		Set<AnimediaMALTitleReferences> tempReferences = allMultiSeasonsReferences.stream()
				.filter(ref -> ref.getTitleOnMAL()
						.equals(updatedTitle.getTitle()))
				.collect(Collectors.toSet());
		if (tempReferences.size() > 1) {
			Set<AnimediaMALTitleReferences> tempMatchedAnime = getMatchedAnime(newHashSet(updatedTitle), tempReferences, animediaSearchList, username);
			matchedAnimeFromCache.removeIf(ref -> ref.equals(reference));
			matchedAnimeFromCache.addAll(tempMatchedAnime);
		} else {
			enrichReference(reference, updatedTitle);
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
			finalUrl = animediaOnlineTv + animediaMALTitleReferences.getUrl() + "/" + animediaMALTitleReferences.getDataList() + "/"
					+ animediaMALTitleReferences.getFirstEpisode();
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
		return episodesRange.stream()
				.filter(episodes -> episodes.matches(JOINED_EPISODE_REGEXP))
				.map(episodes -> episodes.split("-"))
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
		String[] splittedEpisodes = episodesRange.stream()
				.filter(episodes -> episodes.matches(JOINED_EPISODE_REGEXP))
				.map(episodes -> episodes.split("-"))
				.filter(episodesArray -> isEpisodeInRange(episodesArray, episodeNumberForWatch))
				.findFirst()
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

	private AnimediaMALTitleReferences enrichReference(AnimediaMALTitleReferences reference, UserMALTitleInfo userMALTitleInfo) {
		String nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = null;
		int episodeNumberForWatch;
		int firstEpisode = Integer.parseInt(reference.getFirstEpisode());
		if (isTitleConcretizedOnMAL(reference)) {
			episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedReferences(reference, userMALTitleInfo);
			nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = String.valueOf(getEpisodeNumberForWatch(userMALTitleInfo));
		} else {
			episodeNumberForWatch = firstEpisode + userMALTitleInfo.getNumWatchedEpisodes();
		}
		Map<String, String> nextEpisodeForWatchAndFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, episodeNumberForWatch);
		String nextEpisodeNumber = extractEpisodeNumberForWatch(nextEpisodeForWatchAndFinalUrl);
		reference.setEpisodeNumberForWatch(nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL != null && !nextEpisodeNumber.equals(
				EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) ? nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL : nextEpisodeNumber);
		reference.setFinalUrl(extractFinalUrl(nextEpisodeForWatchAndFinalUrl));
		return reference;
	}

	private AnimediaMALTitleReferences enrichReference(AnimediaMALTitleReferences reference, int nextNumberOfEpisodeForWatch) {
		Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, nextNumberOfEpisodeForWatch);
		reference.setEpisodeNumberForWatch(extractEpisodeNumberForWatch(nextEpisodeForWatchFinalUrl));
		reference.setFinalUrl(extractFinalUrl(nextEpisodeForWatchFinalUrl));
		return reference;
	}

	private String extractEpisodeNumberForWatch(Map<String, String> nextEpisodeForWatchAndFinalUrl) {
		return nextEpisodeForWatchAndFinalUrl.keySet()
				.stream()
				.findFirst()
				.orElse(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private String extractFinalUrl(Map<String, String> nextEpisodeForWatchAndFinalUrl) {
		return nextEpisodeForWatchAndFinalUrl.values()
				.stream()
				.findFirst()
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private boolean isNextNumberOfEpisodeForWatchInReferenceEpisodesRange(int nextNumberOfEpisodeForWatch,
			AnimediaMALTitleReferences animediaMALTitleReferences) {
		String maxEpisodesInDataListOnAnimedia = animediaMALTitleReferences.getMaxConcretizedEpisodeOnAnimedia();
		int intMax = isMaxEpisodeUndefined(maxEpisodesInDataListOnAnimedia) ? UNDEFINED_MAX : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
		return nextNumberOfEpisodeForWatch >= Integer.parseInt(animediaMALTitleReferences.getFirstEpisode()) && (nextNumberOfEpisodeForWatch <= intMax
				|| intMax == UNDEFINED_MAX);
	}
}
