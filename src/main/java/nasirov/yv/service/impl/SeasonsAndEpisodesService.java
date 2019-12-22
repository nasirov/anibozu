package nasirov.yv.service.impl;

import static com.google.common.collect.Iterables.get;
import static java.util.Collections.max;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.JOINED_EPISODE_REGEXP;
import static nasirov.yv.data.constants.BaseConstants.ZERO_EPISODE;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SeasonsAndEpisodesService implements SeasonsAndEpisodesServiceI {

	private static final Map<String, String> EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH = new HashMap<>(1);

	private static final int UNDEFINED_MAX = 0;

	static {
		EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH.put(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	private final UrlsNames urlsNames;

	private String animediaOnlineTv;

	@PostConstruct
	public void init() {
		animediaOnlineTv = urlsNames.getAnimediaUrls()
				.getOnlineAnimediaTv();
	}

	/**
	 * Searches for matched anime in the references and update {@link TitleReference#getEpisodeNumberForWatchForFront()} and {@link
	 * TitleReference#getFinalUrlForFront()} based on an next episode availability
	 *
	 * @param watchingTitles              user currently watching titles
	 * @param matchedAndUpdatedReferences matched and updated references based on watching titles
	 * @param username                    MAL username
	 * @return matched references
	 */
	@Override
	public Set<TitleReference> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<TitleReference> matchedAndUpdatedReferences,
			String username) {
		log.info("RESULT FOR {}:", username);
		return watchingTitles.stream()
				.map(userMALTitleInfo -> findMatchedAnime(matchedAndUpdatedReferences, userMALTitleInfo))
				.filter(Objects::nonNull)
				.map(ref -> setMALPosterUrl(ref, watchingTitles))
				.collect(Collectors.toSet());
	}

	private TitleReference findMatchedAnime(Set<TitleReference> references, UserMALTitleInfo userMALTitleInfo) {
		TitleReference result = null;
		Set<TitleReference> matchedReferences = getMatchedReferences(references, userMALTitleInfo);
		switch (matchedReferences.size()) {
			case 0:
				handleZeroMatchedResult(userMALTitleInfo);
				break;
			case 1:
				result = handleOneMatchedResult(matchedReferences, userMALTitleInfo);
				break;
			default:
				if (isMatchedReferencesOnSameDataList(matchedReferences)) {
					result = handleMoreThanOneMatchedResultOnSameDataList(matchedReferences, userMALTitleInfo);
				} else {
					result = handleMoreThanOneMatchedResult(matchedReferences, userMALTitleInfo);
				}
				break;
		}
		return result;
	}

	private void handleZeroMatchedResult(UserMALTitleInfo userMALTitleInfo) {
		log.info("ANIME {}({}) IS NOT FOUND ON ANIMEDIA!", userMALTitleInfo.getTitle(), userMALTitleInfo.getAnimeUrl());
		if (!notFoundAnimeOnAnimediaRepository.exitsByTitle(userMALTitleInfo.getTitle())) {
			notFoundAnimeOnAnimediaRepository.saveAndFlush(userMALTitleInfo);
		}
	}

	private TitleReference handleOneMatchedResult(Set<TitleReference> matchedMultiSeasonsReferences, UserMALTitleInfo userMALTitleInfo) {
		return matchedMultiSeasonsReferences.stream()
				.map(x -> enrichReference(x, userMALTitleInfo))
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
	private TitleReference handleMoreThanOneMatchedResultOnSameDataList(Set<TitleReference> matchedMultiSeasonsReferences,
			UserMALTitleInfo userMALTitleInfo) {
		int nextNumberOfEpisodeForWatch;
		TitleReference titleReference;
		int nextEpisodeNumber = getEpisodeNumberForWatch(userMALTitleInfo);
		List<TitleReference> matched = matchedMultiSeasonsReferences.stream()
				.filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinOnMAL()))
				.collect(Collectors.toList());
		if (matched.size() == 1) {
			titleReference = get(matched, 0);
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(titleReference, userMALTitleInfo);
		} else {
			titleReference = max(matchedMultiSeasonsReferences, comparing(TitleReference::getMinOnMAL));
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(titleReference, userMALTitleInfo);
		}
		Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(titleReference, nextNumberOfEpisodeForWatch);
		String numberForWatch = extractEpisodeNumberForWatch(nextEpisodeForWatchFinalUrl);
		titleReference.setEpisodeNumberForWatchForFront(
				numberForWatch.equals(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) ? numberForWatch : String.valueOf(nextEpisodeNumber));
		titleReference.setFinalUrlForFront(extractFinalUrl(nextEpisodeForWatchFinalUrl));
		return titleReference;
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when one season separated on several tabs for example,
	 * http://online.animedia.tv/anime/one-piece-van-pis-tv/
	 *
	 * @param matchedMultiSeasonsReferences the matched multi seasons references
	 */
	private TitleReference handleMoreThanOneMatchedResult(Set<TitleReference> matchedMultiSeasonsReferences, UserMALTitleInfo userMALTitleInfo) {
		int nextNumberOfEpisodeForWatch = getEpisodeNumberForWatch(userMALTitleInfo);
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> isNextNumberOfEpisodeForWatchInReferenceEpisodesRange(nextNumberOfEpisodeForWatch, ref))
				.map(ref -> enrichReference(ref, nextNumberOfEpisodeForWatch))
				.findFirst()
				.orElse(null);
	}

	private TitleReference handleAnnouncements(TitleReference reference, UserMALTitleInfo userMALTitleInfo) {
		TitleReference result = TitleReference.builder()
				.urlOnAnimedia(reference.getUrlOnAnimedia())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.titleNameOnMAL(userMALTitleInfo.getTitle())
				.minOnAnimedia(ZERO_EPISODE)
				.maxOnAnimedia(ZERO_EPISODE)
				.currentMaxOnAnimedia(ZERO_EPISODE)
				.finalUrlForFront(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatchForFront(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
		String finalUrl = animediaOnlineTv + result.getUrlOnAnimedia();
		log.info("NEW EPISODE FOR {} IS NOT AVAILABLE BECAUSE IT'S ANNOUNCEMENT", finalUrl);
		return result;
	}

	private TitleReference setMALPosterUrl(TitleReference finalMatchedReference, Set<UserMALTitleInfo> watchingTitles) {
		watchingTitles.stream()
				.filter(x -> x.getTitle()
						.equals(finalMatchedReference.getTitleNameOnMAL()))
				.findFirst()
				.ifPresent(x -> finalMatchedReference.setPosterUrlOnMAL(x.getPosterUrl()));
		return finalMatchedReference;
	}

	private Set<TitleReference> getMatchedReferences(Set<TitleReference> allReferences, UserMALTitleInfo userMALTitleInfo) {
		return allReferences.stream()
				.filter(set -> set.getTitleNameOnMAL()
						.equals(userMALTitleInfo.getTitle()))
				.collect(Collectors.toSet());
	}

	/**
	 * Get episode number for watch for titles with concretized episodes on MAL Used for references with concretized episodes on MAL for example, 1-2
	 * https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param titleReference   title reference
	 * @param userMALTitleInfo mal title
	 * @return correct episode number for watch
	 */
	private int getEpisodeNumberForWatchForConcretizedReferences(TitleReference titleReference, UserMALTitleInfo userMALTitleInfo) {
		int episodeNumberForWatch;
		int nextEpisodeNumber = getEpisodeNumberForWatch(userMALTitleInfo);
		int intMinConcretizedEpisodeOnMAL = Integer.parseInt(titleReference.getMinOnMAL());
		int intMaxConcretizedEpisodeOnMAL = Integer.parseInt(titleReference.getMaxOnMAL());
		int min = Integer.parseInt(titleReference.getMinOnAnimedia());
		int max = Integer.parseInt(titleReference.getMaxOnAnimedia());
		if (nextEpisodeNumber >= intMinConcretizedEpisodeOnMAL && nextEpisodeNumber <= intMaxConcretizedEpisodeOnMAL) {
			if (min != max) {
				int stepFromFirstEpisode = nextEpisodeNumber - intMinConcretizedEpisodeOnMAL;
				episodeNumberForWatch = min + stepFromFirstEpisode;
			} else {
				episodeNumberForWatch = min;
			}
		} else {
			episodeNumberForWatch = Integer.parseInt(titleReference.getCurrentMaxOnAnimedia()) + 1;
		}
		return episodeNumberForWatch;
	}

	private boolean isMatchedReferencesOnSameDataList(Set<TitleReference> matchedMultiSeasonsReferences) {
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> ref.getDataListOnAnimedia()
						.equals(matchedMultiSeasonsReferences.stream()
								.findFirst()
								.orElseGet(TitleReference::new)
								.getDataListOnAnimedia()))
				.count() > 1;
	}

	private int getEpisodeNumberForWatch(UserMALTitleInfo userMALTitleInfo) {
		return userMALTitleInfo.getNumWatchedEpisodes() + 1;
	}

	/**
	 * Creates map with next episode for watch and final url for title
	 *
	 * @param titleReference        matched title
	 * @param episodeNumberForWatch next episode for watch
	 * @return correct nextEpisodeForWatch and finalURL if new episode is available or "","" if new episode is not available
	 */
	private Map<String, String> getNextEpisodeForWatchAndFinalUrl(TitleReference titleReference, int episodeNumberForWatch) {
		String finalUrl;
		Map<String, String> nextEpisodeForWatchFinalUrl;
		if (episodeNumberForWatch <= Integer.parseInt(titleReference.getCurrentMaxOnAnimedia())) {
			nextEpisodeForWatchFinalUrl = new HashMap<>();
			String episodeNumberForWatchForFront = String.valueOf(episodeNumberForWatch);
			String episodeNumberForWatchForURL = episodeNumberForWatchForFront;
			List<String> episodesRange = titleReference.getEpisodesRangeOnAnimedia();
			if (!isTitleConcretizedOnMAL(titleReference) && isNextEpisodeForWatchInJoinedEpisode(episodeNumberForWatchForFront, episodesRange)) {
				String[] episodeNumberForWatchForURLandFront = getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(episodeNumberForWatchForFront,
						episodesRange);
				episodeNumberForWatchForURL = episodeNumberForWatchForURLandFront[0];
				episodeNumberForWatchForFront = episodeNumberForWatchForURLandFront[1];
			}
			finalUrl =
					animediaOnlineTv + titleReference.getUrlOnAnimedia() + "/" + titleReference.getDataListOnAnimedia() + "/" + episodeNumberForWatchForURL;
			log.info("NEW EPISODE IS AVAILABLE {} !", finalUrl);
			nextEpisodeForWatchFinalUrl.put(episodeNumberForWatchForFront, finalUrl);
		} else {
			nextEpisodeForWatchFinalUrl = EPISODE_IS_NOT_AVAILABLE_FINAL_URL_AND_EPISODE_NUMBER_FOR_WATCH;
			finalUrl = animediaOnlineTv + titleReference.getUrlOnAnimedia() + "/" + titleReference.getDataListOnAnimedia() + "/"
					+ titleReference.getMinOnAnimedia();
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
		return ofNullable(episodesRange).orElseGet(Collections::emptyList)
				.stream()
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

	private TitleReference enrichReference(TitleReference reference, UserMALTitleInfo userMALTitleInfo) {
		String nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = null;
		int episodeNumberForWatch;
		int firstEpisode = Integer.parseInt(reference.getMinOnAnimedia());
		if (StringUtils.isBlank(reference.getAnimeIdOnAnimedia())) {
			return handleAnnouncements(reference, userMALTitleInfo);
		}
		if (isTitleConcretizedOnMAL(reference)) {
			episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedReferences(reference, userMALTitleInfo);
			nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL = String.valueOf(getEpisodeNumberForWatch(userMALTitleInfo));
		} else {
			episodeNumberForWatch = firstEpisode + userMALTitleInfo.getNumWatchedEpisodes();
		}
		Map<String, String> nextEpisodeForWatchAndFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, episodeNumberForWatch);
		String nextEpisodeNumber = extractEpisodeNumberForWatch(nextEpisodeForWatchAndFinalUrl);
		reference.setEpisodeNumberForWatchForFront(nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL != null && !nextEpisodeNumber.equals(
				EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) ? nextEpisodeForWatchForTitleWithConcretizedEpisodeOnMAL : nextEpisodeNumber);
		reference.setFinalUrlForFront(extractFinalUrl(nextEpisodeForWatchAndFinalUrl));
		return reference;
	}

	private TitleReference enrichReference(TitleReference reference, int nextNumberOfEpisodeForWatch) {
		Map<String, String> nextEpisodeForWatchFinalUrl = getNextEpisodeForWatchAndFinalUrl(reference, nextNumberOfEpisodeForWatch);
		reference.setEpisodeNumberForWatchForFront(extractEpisodeNumberForWatch(nextEpisodeForWatchFinalUrl));
		reference.setFinalUrlForFront(extractFinalUrl(nextEpisodeForWatchFinalUrl));
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

	//anime/brodyaga-kensin-OVA
	//anime/one-piece-van-pis-tv
	private boolean isNextNumberOfEpisodeForWatchInReferenceEpisodesRange(int nextNumberOfEpisodeForWatch, TitleReference titleReference) {
		String maxEpisodesInDataListOnAnimedia = titleReference.getMaxOnAnimedia();
		int intMax = isMaxEpisodeUndefined(maxEpisodesInDataListOnAnimedia) ? UNDEFINED_MAX : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
		return nextNumberOfEpisodeForWatch >= Integer.parseInt(titleReference.getMinOnAnimedia()) && (nextNumberOfEpisodeForWatch <= intMax
				|| intMax == UNDEFINED_MAX);
	}
}
