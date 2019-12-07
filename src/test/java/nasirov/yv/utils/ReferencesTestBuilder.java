package nasirov.yv.utils;

import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.data.constants.BaseConstants.ZERO_EPISODE;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestUtils.getEpisodesRange;

import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class ReferencesTestBuilder {


	public static AnimediaMALTitleReferences buildConcretizedTitleWithEpisodesRange() {
		return AnimediaMALTitleReferences.builder()
				.url(CONCRETIZED_TITLE_URL)
				.titleOnMAL(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME)
				.dataList("7")
				.firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("6")
				.currentMax("6")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_POSTER_URL)
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6")
				.build();
	}

	public static AnimediaMALTitleReferences buildConcretizedTitleWithSingleEpisode() {
		return AnimediaMALTitleReferences.builder()
				.url(CONCRETIZED_TITLE_URL)
				.titleOnMAL(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME)
				.dataList("7")
				.firstEpisode("7")
				.minConcretizedEpisodeOnAnimedia("7")
				.maxConcretizedEpisodeOnAnimedia("7")
				.currentMax("7")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_POSTER_URL)
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("1")
				.build();
	}

	public static AnimediaMALTitleReferences buildMultiSeasonsTitle() {
		return AnimediaMALTitleReferences.builder()
				.url(MULTI_SEASONS_TITLE_URL)
				.titleOnMAL(MULTI_SEASONS_TITLE_NAME)
				.dataList("1")
				.firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("12")
				.currentMax("12")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + MULTI_SEASONS_TITLE_POSTER_URL)
				.episodesRange(getEpisodesRange("1", "12"))
				.build();
	}

	public static AnimediaMALTitleReferences buildAnnouncement() {
		return AnimediaMALTitleReferences.builder()
				.url(ANNOUNCEMENT_TITLE_URL)
				.dataList(FIRST_DATA_LIST)
				.firstEpisode(FIRST_EPISODE)
				.titleOnMAL(ANNOUNCEMENT_TITLE_NAME)
				.minConcretizedEpisodeOnAnimedia(ZERO_EPISODE)
				.maxConcretizedEpisodeOnAnimedia(ZERO_EPISODE)
				.currentMax(ZERO_EPISODE)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + ANNOUNCEMENT_TITLE_POSTER_URL)
				.finalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
	}
}
