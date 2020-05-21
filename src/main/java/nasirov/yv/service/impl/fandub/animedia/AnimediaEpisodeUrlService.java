package nasirov.yv.service.impl.fandub.animedia;

import static com.google.common.collect.Iterables.get;
import static java.util.Collections.max;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.service.AnimediaTitlesUpdateServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.util.AnimediaUtils;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
public class AnimediaEpisodeUrlService implements EpisodeUrlServiceI {

	private static final int UNDEFINED_MAX = 0;

	private final UrlsNames urlsNames;

	private final TitlesServiceI<AnimediaTitle> animediaGitHubResourcesService;

	private final AnimediaTitlesUpdateServiceI animediaTitlesUpdateService;

	@Override
	public String getEpisodeUrl(MalTitle watchingTitle) {
		String result = NOT_FOUND_ON_FANDUB_SITE_URL;
		List<AnimediaTitle> animediaTitles = getMatchedAnimediaTitles(watchingTitle);
		animediaTitlesUpdateService.updateAnimediaTitles(animediaTitles);
		if (animediaTitles.size() == 1) {
			result = handleOneMatchedResult(animediaTitles, watchingTitle);
		}
		if (animediaTitles.size() > 1) {
			result = isMatchedAnimediaTitlesOnSameDataList(animediaTitles) ? handleMoreThanOneMatchedResultOnSameDataList(animediaTitles, watchingTitle)
					: handleMoreThanOneMatchedResult(animediaTitles, watchingTitle);
		}
		return result;
	}

	private String handleOneMatchedResult(List<AnimediaTitle> matchedAnimediaTitles, MalTitle watchingTitle) {
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
	private String handleMoreThanOneMatchedResultOnSameDataList(List<AnimediaTitle> matchedAnimediaTitles, MalTitle watchingTitle) {
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
	private String handleMoreThanOneMatchedResult(List<AnimediaTitle> matchedAnimediaTitles, MalTitle watchingTitle) {
		int nextNumberOfEpisodeForWatch = getNextEpisodeForWatch(watchingTitle);
		return matchedAnimediaTitles.stream()
				.filter(ref -> isNextNumberOfEpisodeForWatchInAnimediaTitleEpisodesRange(nextNumberOfEpisodeForWatch, ref))
				.map(ref -> getFinalUrl(ref, getNextEpisodeForWatch(watchingTitle)))
				.findFirst()
				.orElse(NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	private List<AnimediaTitle> getMatchedAnimediaTitles(MalTitle malTitle) {
		return animediaGitHubResourcesService.getTitles()
				.getOrDefault(malTitle.getId(), Collections.emptyList());
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
	 * @param animediaTitle animedia titles with concretized episodes on MAL
	 * @param malTitle      mal title
	 * @return correct episode number for watch
	 */
	private int getEpisodeNumberForWatchForConcretizedAnimediaTitle(AnimediaTitle animediaTitle, MalTitle malTitle) {
		int episodeNumberForWatch;
		int nextEpisodeNumber = getNextEpisodeForWatch(malTitle);
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

	private boolean isMatchedAnimediaTitlesOnSameDataList(List<AnimediaTitle> matchedAnimediaTitles) {
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
				.filter(AnimediaUtils::isJoinedEpisodes)
				.map(AnimediaUtils::splitJoinedEpisodes)
				.anyMatch(x -> isEpisodeInRange(x, episodeNumberForWatch));
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
				.filter(AnimediaUtils::isJoinedEpisodes)
				.map(AnimediaUtils::splitJoinedEpisodes)
				.filter(x -> isEpisodeInRange(x, episodeNumberForWatch))
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

	private String buildUrlForOneMatchedResult(AnimediaTitle animediaTitle, MalTitle watchingTitle) {
		int episodeNumberForWatch;
		int firstEpisode = Integer.parseInt(animediaTitle.getMinOnAnimedia());
		if (!AnimediaUtils.isTitleUpdated(animediaTitle)) {
			return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
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
