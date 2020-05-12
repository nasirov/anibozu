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
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.service.AnimediaGitHubResourcesServiceI;
import nasirov.yv.service.AnimediaTitlesUpdateServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
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

	private final AnimediaTitlesUpdateServiceI animediaTitlesUpdateService;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String result;
		Set<AnimediaTitle> animediaTitles = getMatchedAnimediaTitles(watchingTitle);
		animediaTitlesUpdateService.updateAnimediaTitles(animediaTitles);
		switch (animediaTitles.size()) {
			case 0:
				result = handleZeroMatchedResult(watchingTitle);
				break;
			case 1:
				result = handleOneMatchedResult(animediaTitles, watchingTitle);
				break;
			default:
				if (isMatchedAnimediaTitlesOnSameDataList(animediaTitles)) {
					result = handleMoreThanOneMatchedResultOnSameDataList(animediaTitles, watchingTitle);
				} else {
					result = handleMoreThanOneMatchedResult(animediaTitles, watchingTitle);
				}
				break;
		}
		return result;
	}

	private String handleZeroMatchedResult(UserMALTitleInfo watchingTitle) {
		log.debug("TITLE [{}] WAS NOT FOUND ON Animedia!", watchingTitle);
		return NOT_FOUND_ON_FANDUB_SITE_URL;
	}

	private String handleOneMatchedResult(Set<AnimediaTitle> matchedAnimediaTitles, UserMALTitleInfo watchingTitle) {
		return matchedAnimediaTitles.stream()
				.map(x -> buildUrlForOneMatchedResult(x, watchingTitle))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Handles more than one matched result in animedia titles resources
	 * <p>
	 * it happens when data list contain several titles
	 * <p>
	 * for example, 1-2 https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1
	 * <p>
	 * 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param matchedAnimediaTitles animedia titles with equal title id on mal and data list
	 * @param watchingTitle         user watching title
	 */
	private String handleMoreThanOneMatchedResultOnSameDataList(Set<AnimediaTitle> matchedAnimediaTitles, UserMALTitleInfo watchingTitle) {
		int nextNumberOfEpisodeForWatch;
		AnimediaTitle animediaTitle;
		int nextEpisodeNumber = getNextEpisodeForWatch(watchingTitle);
		List<AnimediaTitle> matched = matchedAnimediaTitles.stream()
				.filter(ref -> nextEpisodeNumber >= Integer.parseInt(ref.getMinOnMAL()))
				.collect(Collectors.toList());
		if (matched.size() == 1) {
			animediaTitle = get(matched, 0);
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedAnimediaTitle(animediaTitle, watchingTitle);
		} else {
			animediaTitle = max(matchedAnimediaTitles, comparing(AnimediaTitle::getMinOnMAL));
			nextNumberOfEpisodeForWatch = getEpisodeNumberForWatchForConcretizedAnimediaTitle(animediaTitle, watchingTitle);
		}
		return getFinalUrl(animediaTitle, nextNumberOfEpisodeForWatch);
	}

	/**
	 * Handles more than one matched result in animedia titles resources
	 * <p>
	 * it happens when one season separated on several tabs
	 * <p>
	 * http://online.animedia.tv/anime/one-piece-van-pis-tv/
	 *
	 * @param matchedAnimediaTitles animedia titles with equal title id on mal
	 */
	private String handleMoreThanOneMatchedResult(Set<AnimediaTitle> matchedAnimediaTitles, UserMALTitleInfo watchingTitle) {
		int nextNumberOfEpisodeForWatch = getNextEpisodeForWatch(watchingTitle);
		return matchedAnimediaTitles.stream()
				.filter(ref -> isNextNumberOfEpisodeForWatchInAnimediaTitleEpisodesRange(nextNumberOfEpisodeForWatch, ref))
				.map(ref -> getFinalUrl(ref, getNextEpisodeForWatch(watchingTitle)))
				.findFirst()
				.orElse(NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	private String handleAnnouncement(UserMALTitleInfo watchingTitle) {
		log.debug("NEW EPISODE FOR {} IS NOT AVAILABLE BECAUSE IT'S ANNOUNCEMENT", watchingTitle.getTitle());
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
	}

	private Set<AnimediaTitle> getMatchedAnimediaTitles(UserMALTitleInfo userMALTitleInfo) {
		return animediaGitHubResourcesService.getAnimediaTitles()
				.getOrDefault(userMALTitleInfo.getAnimeId(), Collections.emptySet());
	}

	/**
	 * Get episode number for watch for titles with concretized episodes on MAL
	 * <p>
	 * Uses for animedia titles with concretized episodes on MAL
	 * <p>
	 * 1-2 https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1
	 * <p>
	 * 3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
	 *
	 * @param animediaTitle    animedia titles with concretized episodes on MAL
	 * @param userMALTitleInfo mal title
	 * @return correct episode number for watch
	 */
	private int getEpisodeNumberForWatchForConcretizedAnimediaTitle(AnimediaTitle animediaTitle, UserMALTitleInfo userMALTitleInfo) {
		int episodeNumberForWatch;
		int nextEpisodeNumber = getNextEpisodeForWatch(userMALTitleInfo);
		int intMinConcretizedEpisodeOnMAL = Integer.parseInt(animediaTitle.getMinOnMAL());
		int intMaxConcretizedEpisodeOnMAL = Integer.parseInt(animediaTitle.getMaxOnMAL());
		int min = Integer.parseInt(animediaTitle.getMinOnAnimedia());
		int max = Integer.parseInt(animediaTitle.getMaxOnAnimedia());
		if (nextEpisodeNumber >= intMinConcretizedEpisodeOnMAL && nextEpisodeNumber <= intMaxConcretizedEpisodeOnMAL) {
			if (min != max) {
				int stepFromFirstEpisode = nextEpisodeNumber - intMinConcretizedEpisodeOnMAL;
				episodeNumberForWatch = min + stepFromFirstEpisode;
			} else {
				episodeNumberForWatch = min;
			}
		} else {
			episodeNumberForWatch = Integer.parseInt(animediaTitle.getCurrentMaxOnAnimedia()) + 1;
		}
		return episodeNumberForWatch;
	}

	private boolean isMatchedAnimediaTitlesOnSameDataList(Set<AnimediaTitle> matchedAnimediaTitles) {
		return matchedAnimediaTitles.stream()
				.map(AnimediaTitle::getDataListOnAnimedia)
				.distinct()
				.count() < matchedAnimediaTitles.size();
	}

	/**
	 * Builds a new episode url
	 *
	 * @param animediaTitle         matched animedia title
	 * @param episodeNumberForWatch next episode for watch
	 * @return finalURL if new episode is available or {@link BaseConstants#FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE} if new episode is not
	 * available
	 */
	private String getFinalUrl(AnimediaTitle animediaTitle, int episodeNumberForWatch) {
		String finalUrl;
		if (episodeNumberForWatch <= Integer.parseInt(animediaTitle.getCurrentMaxOnAnimedia())) {
			String episodeNumberForWatchForFront = String.valueOf(episodeNumberForWatch);
			String episodeNumberForWatchForURL = episodeNumberForWatchForFront;
			List<String> episodesRange = animediaTitle.getEpisodesRangeOnAnimedia();
			if (!isTitleConcretizedOnMAL(animediaTitle) && isNextEpisodeForWatchInJoinedEpisode(episodeNumberForWatchForFront, episodesRange)) {
				episodeNumberForWatchForURL = getEpisodeNumberForWatchForURLandFrontIfJoinedEpisodeIsPresent(episodeNumberForWatchForFront, episodesRange);
			}
			finalUrl = urlsNames.getAnimediaUrls()
					.getOnlineAnimediaTv() + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/" + episodeNumberForWatchForURL;
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

	private String buildUrlForOneMatchedResult(AnimediaTitle animediaTitle, UserMALTitleInfo watchingTitle) {
		int episodeNumberForWatch;
		int firstEpisode = Integer.parseInt(animediaTitle.getMinOnAnimedia());
		if (!AnimediaUtils.isTitleUpdated(animediaTitle)) {
			return handleAnnouncement(watchingTitle);
		}
		if (isTitleConcretizedOnMAL(animediaTitle)) {
			episodeNumberForWatch = getEpisodeNumberForWatchForConcretizedAnimediaTitle(animediaTitle, watchingTitle);
		} else {
			episodeNumberForWatch = firstEpisode + watchingTitle.getNumWatchedEpisodes();
		}
		return getFinalUrl(animediaTitle, episodeNumberForWatch);
	}

	//anime/brodyaga-kensin-OVA
	//anime/one-piece-van-pis-tv
	private boolean isNextNumberOfEpisodeForWatchInAnimediaTitleEpisodesRange(int nextNumberOfEpisodeForWatch, AnimediaTitle animediaTitle) {
		String maxEpisodesInDataListOnAnimedia = animediaTitle.getMaxOnAnimedia();
		int intMax = isMaxEpisodeUndefined(maxEpisodesInDataListOnAnimedia) ? UNDEFINED_MAX : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
		return nextNumberOfEpisodeForWatch >= Integer.parseInt(animediaTitle.getMinOnAnimedia()) && (nextNumberOfEpisodeForWatch <= intMax
				|| intMax == UNDEFINED_MAX);
	}
}
