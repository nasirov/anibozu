package nasirov.yv.utils;

import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_MAL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_WITH_EPISODES_RANGE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;

import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.fandub.animedia.AnimediaTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimediaTitlesTestBuilder {

	public static AnimediaTitle buildUpdatedRegularAnimediaTitle() {
		AnimediaTitle regularNotUpdatedAnimediaTitle = getRegularNotUpdatedAnimediaTitle();
		regularNotUpdatedAnimediaTitle.setMinOnAnimedia("1");
		regularNotUpdatedAnimediaTitle.setMaxOnAnimedia("5");
		regularNotUpdatedAnimediaTitle.setCurrentMaxOnAnimedia("5");
		return regularNotUpdatedAnimediaTitle;
	}

	@SneakyThrows
	public static List<AnimediaTitle> getAnimediaTitles(boolean updated) {
		List<AnimediaTitle> refs = new ArrayList<>();
		AnimediaTitle regularNotUpdatedAnimediaTitle = getRegularNotUpdatedAnimediaTitle();
		AnimediaTitle announcementAnimediaTitle = getAnnouncementAnimediaTitle();
		AnimediaTitle concretizedAnimediaTitle = getConcretizedAnimediaTitleWithEpisodesRange();
		AnimediaTitle concretizedAndOngoingAnimediaTitle = buildConcretizedAndOngoingAnimediaTitle();
		if (updated) {
			regularNotUpdatedAnimediaTitle.setMinOnAnimedia("1");
			// TODO: 17.12.2019 uncomment when animedia improve api object that will return max episode in a season
//			regularNotUpdatedAnimediaTitle.setMaxConcretizedEpisodeOnAnimedia("5");
			regularNotUpdatedAnimediaTitle.setCurrentMaxOnAnimedia("5");
			concretizedAndOngoingAnimediaTitle.setCurrentMaxOnAnimedia("5");
		}
		refs.add(regularNotUpdatedAnimediaTitle);
		refs.add(announcementAnimediaTitle);
		refs.add(concretizedAnimediaTitle);
		refs.add(concretizedAndOngoingAnimediaTitle);
		return refs;
	}

	public static AnimediaTitle buildConcretizedAndOngoingAnimediaTitle() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(CONCRETIZED_AND_ONGOING_TITLE_URL)
				.dataListOnAnimedia("3")
				.animeIdOnAnimedia(CONCRETIZED_AND_ONGOING_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_AND_ONGOING_TITLE_NAME)
				.titleIdOnMal(CONCRETIZED_AND_ONGOING_TITLE_MAL_ANIME_ID)
				.minOnAnimedia("1")
				.maxOnAnimedia("5")
				.minOnMAL("1")
				.maxOnMAL("5")
				.build();
	}

	public static AnimediaTitle getConcretizedAnimediaTitleWithEpisodesRange() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_URL)
				.dataListOnAnimedia("7")
				.animeIdOnAnimedia(CONCRETIZED_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME)
				.titleIdOnMal(CONCRETIZED_TITLE_WITH_WITH_EPISODES_RANGE_MAL_ANIME_ID)
				.minOnAnimedia("1")
				.maxOnAnimedia("6")
				.minOnMAL("1")
				.maxOnMAL("6")
				.currentMaxOnAnimedia("6")
				.build();
	}

	public static AnimediaTitle getConcretizedAnimediaTitleWithSingleEpisode() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_URL)
				.animeIdOnAnimedia(CONCRETIZED_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME)
				.titleIdOnMal(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_MAL_ANIME_ID)
				.dataListOnAnimedia("7")
				.minOnAnimedia("7")
				.maxOnAnimedia("7")
				.currentMaxOnAnimedia("7")
				.minOnMAL("1")
				.maxOnMAL("1")
				.build();
	}

	public static AnimediaTitle getAnnouncementAnimediaTitle() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(ANNOUNCEMENT_TITLE_URL)
				.animeIdOnAnimedia(ANNOUNCEMENT_TITLE_ID)
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.titleNameOnMAL(ANNOUNCEMENT_TITLE_NAME)
				.titleIdOnMal(ANNOUNCEMENT_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnimediaTitle getRegularNotUpdatedAnimediaTitle() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(REGULAR_TITLE_URL)
				.animeIdOnAnimedia(REGULAR_TITLE_ID)
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.titleNameOnMAL(REGULAR_TITLE_NAME)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnimediaTitle getNotFoundOnMalAnimediaTitle() {
		return AnimediaTitle.builder()
				.urlOnAnimedia("anime/something")
				.animeIdOnAnimedia("1234")
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.titleNameOnMAL(NOT_FOUND_ON_MAL)
				.build();
	}
}
