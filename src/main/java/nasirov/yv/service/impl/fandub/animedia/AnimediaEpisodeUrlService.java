package nasirov.yv.service.impl.fandub.animedia;

import static com.google.common.collect.Iterables.get;
import static java.util.Collections.max;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.JOINED_EPISODE_REGEXP;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.service.AnimediaGitHubResourcesServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitleReferenceUpdateServiceI;
import nasirov.yv.util.AnimediaUtils;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaEpisodeUrlService implements EpisodeUrlServiceI {

	private static final int UNDEFINED_MAX = 0;

	private final UrlsNames urlsNames;

	private final AnimediaGitHubResourcesServiceI animediaGitHubResourcesService;

	private final TitleReferenceUpdateServiceI titleReferenceUpdateService;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		return buildEpisodeUrl(watchingTitle);
	}

	private String buildEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String result;
		Set<TitleReference> matchedReferences = getMatchedReferences(watchingTitle);
		titleReferenceUpdateService.updateReferences(matchedReferences);
		switch (matchedReferences.size()) {
			case 0:
				result = handleZeroMatchedResult(watchingTitle);
				break;
			case 1:
				result = handleOneMatchedResult(matchedReferences, watchingTitle);
				break;
			default:
				if (isMatchedReferencesOnSameDataList(matchedReferences)) {
					result = handleMoreThanOneMatchedResultOnSameDataList(matchedReferences, watchingTitle);
				} else {
					result = handleMoreThanOneMatchedResult(matchedReferences, watchingTitle);
				}
				break;
		}
		return result;
	}

	private String handleZeroMatchedResult(UserMALTitleInfo watchingTitle) {
		log.debug("TITLE [{}] WAS NOT FOUND ON Animedia!", watchingTitle);
		return NOT_FOUND_ON_FANDUB_SITE_URL;
	}

	private String handleOneMatchedResult(Set<TitleReference> matchedMultiSeasonsReferences, UserMALTitleInfo watchingTitle) {
		return matchedMultiSeasonsReferences.stream()
				.map(x -> buildUrlForOneMatchedResult(x, watchingTitle))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when data list contain several titles
	 * <p>
	 * for example, 1-2 https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1
	 * <p>
	 * 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param matchedMultiSeasonsReferences the references with equals titles and data lists
	 * @param watchingTitle                 user watching title
	 */
	private String handleMoreThanOneMatchedResultOnSameDataList(Set<TitleReference> matchedMultiSeasonsReferences, UserMALTitleInfo watchingTitle) {
		int nextNumberOfEpisodeForWatch;
		TitleReference titleReference;
		int nextEpisodeNumber = getNextEpisodeForWatch(watchingTitle);
		List<TitleReference> matched = matchedMultiSeasonsReferences.stream()
				.filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinOnMAL()))
				.collect(Collectors.toList());
		if (matched.size() == 1) {
			titleReference = get(matched, 0);
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(titleReference, watchingTitle);
		} else {
			titleReference = max(matchedMultiSeasonsReferences, comparing(TitleReference::getMinOnMAL));
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedReferences(titleReference, watchingTitle);
		}
		return getFinalUrl(titleReference, nextNumberOfEpisodeForWatch);
	}

	/**
	 * Handles more than one matched result in the multi seasons references it happens when one season separated on several tabs
	 * <p>
	 * http://online.animedia.tv/anime/one-piece-van-pis-tv/
	 *
	 * @param matchedMultiSeasonsReferences matched references
	 */
	private String handleMoreThanOneMatchedResult(Set<TitleReference> matchedMultiSeasonsReferences, UserMALTitleInfo watchingTitle) {
		int nextNumberOfEpisodeForWatch = getNextEpisodeForWatch(watchingTitle);
		return matchedMultiSeasonsReferences.stream()
				.filter(ref -> isNextNumberOfEpisodeForWatchInReferenceEpisodesRange(nextNumberOfEpisodeForWatch, ref))
				.map(ref -> getFinalUrl(ref, getNextEpisodeForWatch(watchingTitle)))
				.findFirst()
				.orElse(NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	private String handleAnnouncement(UserMALTitleInfo watchingTitle) {
		log.debug("NEW EPISODE FOR {} IS NOT AVAILABLE BECAUSE IT'S ANNOUNCEMENT", watchingTitle.getTitle());
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
	}

	private Set<TitleReference> getMatchedReferences(UserMALTitleInfo userMALTitleInfo) {
		return animediaGitHubResourcesService.getTitleReferences()
				.getOrDefault(userMALTitleInfo.getAnimeId(), Collections.emptySet());
	}

	/**
	 * Get episode number for watch for titles with concretized episodes on MAL Used for references with concretized episodes on MAL
	 * <p>
	 * 1-2 https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1
	 * <p>
	 * 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param titleReference   title reference
	 * @param userMALTitleInfo mal title
	 * @return correct episode number for watch
	 */
	private int getEpisodeNumberForWatchForConcretizedReferences(TitleReference titleReference, UserMALTitleInfo userMALTitleInfo) {
		int episodeNumberForWatch;
		int nextEpisodeNumber = getNextEpisodeForWatch(userMALTitleInfo);
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

	/**
	 * Builds a new episode url
	 *
	 * @param titleReference        matched reference
	 * @param episodeNumberForWatch next episode for watch
	 * @return finalURL if new episode is available or "" if new episode is not available
	 */
	private String getFinalUrl(TitleReference titleReference, int episodeNumberForWatch) {
		String finalUrl;
		if (episodeNumberForWatch <= Integer.parseInt(titleReference.getCurrentMaxOnAnimedia())) {
			String episodeNumberForWatchForFront = String.valueOf(episodeNumberForWatch);
			String episodeNumberForWatchForURL = episodeNumberForWatchForFront;
			List<String> episodesRange = titleReference.getEpisodesRangeOnAnimedia();
			if (!isTitleConcretizedOnMAL(titleReference) && isNextEpisodeForWatchInJoinedEpisode(episodeNumberForWatchForFront, episodesRange)) {
				episodeNumberForWatchForURL = getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(episodeNumberForWatchForFront, episodesRange);
			}
			finalUrl = urlsNames.getAnimediaUrls()
					.getOnlineAnimediaTv() + titleReference.getUrlOnAnimedia() + "/" + titleReference.getDataListOnAnimedia() + "/"
					+ episodeNumberForWatchForURL;
		} else {
			finalUrl = FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		}
		return finalUrl;
	}

	/**
	 * Finds joined episode in episodes range
	 *
	 * @param episodeNumberForWatch next episode for watch
	 * @param episodesRange         episodes range
	 * @return true if episodeNumberForWatch contains in joined episode, false in other case
	 */
	private boolean isNextEpisodeForWatchInJoinedEpisode(String episodeNumberForWatch, List<String> episodesRange) {
		return ofNullable(episodesRange).orElseGet(Collections::emptyList)
				.stream()
				.filter(episodes -> episodes.matches(JOINED_EPISODE_REGEXP))
				.map(episodes -> episodes.split("-"))
				.anyMatch(episodesArray -> isEpisodeInRange(episodesArray, episodeNumberForWatch));
	}

	/**
	 * Creates an array with episode number for URL and front if joined episode is present
	 *
	 * @param episodeNumberForWatch next episode for watch
	 * @param episodesRange         episodes range from min to max
	 * @return array with [0] - episode number for URL, [1] - episode number for front
	 */
	private String getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(String episodeNumberForWatch, List<String> episodesRange) {
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
		return result[0];
	}

	private boolean isEpisodeInRange(String[] episodesArray, String episodeNumberForWatch) {
		return Integer.parseInt(episodesArray[0]) <= Integer.parseInt(episodeNumberForWatch)
				&& Integer.parseInt(episodesArray[episodesArray.length - 1]) >= Integer.parseInt(episodeNumberForWatch);
	}

	private String buildUrlForOneMatchedResult(TitleReference reference, UserMALTitleInfo watchingTitle) {
		int episodeNumberForWatch;
		int firstEpisode = Integer.parseInt(reference.getMinOnAnimedia());
		if (!AnimediaUtils.isTitleUpdated(reference)) {
			return handleAnnouncement(watchingTitle);
		}
		if (isTitleConcretizedOnMAL(reference)) {
			episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedReferences(reference, watchingTitle);
		} else {
			episodeNumberForWatch = firstEpisode + watchingTitle.getNumWatchedEpisodes();
		}
		return getFinalUrl(reference, episodeNumberForWatch);
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
