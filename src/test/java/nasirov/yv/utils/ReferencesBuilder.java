package nasirov.yv.utils;

import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_MAL;
import static nasirov.yv.data.constants.BaseConstants.ZERO_EPISODE;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;

import java.util.Collection;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.TitleReference;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class ReferencesBuilder {


	public static TitleReference buildConcretizedReferenceWithEpisodesRange() {
		TitleReference concretizedReference = getConcretizedReferenceWithEpisodesRange();
		concretizedReference.setPosterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_POSTER_URL);
		return concretizedReference;
	}

	public static TitleReference buildConcretizedReferenceWithSingleEpisode() {
		TitleReference concretizedReferenceWithSingleEpisode = getConcretizedReferenceWithSingleEpisode();
		concretizedReferenceWithSingleEpisode.setPosterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_POSTER_URL);
		return concretizedReferenceWithSingleEpisode;
	}

	public static TitleReference buildUpdatedRegularReference() {
		TitleReference regularReferenceNotUpdated = getRegularReferenceNotUpdated();
		regularReferenceNotUpdated.setMinOnAnimedia("1");
		regularReferenceNotUpdated.setMaxOnAnimedia("5");
		regularReferenceNotUpdated.setCurrentMaxOnAnimedia("5");
		regularReferenceNotUpdated.setPosterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL);
		return regularReferenceNotUpdated;
	}

	public static TitleReference buildUpdatedAnnouncementReference() {
		TitleReference announcementReference = getAnnouncementReference();
		announcementReference.setMinOnAnimedia(ZERO_EPISODE);
		announcementReference.setMaxOnAnimedia(ZERO_EPISODE);
		announcementReference.setCurrentMaxOnAnimedia(ZERO_EPISODE);
		announcementReference.setPosterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + ANNOUNCEMENT_TITLE_POSTER_URL);
		announcementReference.setFinalUrlForFront(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		announcementReference.setEpisodeNumberForWatchForFront(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		return announcementReference;
	}

	public static <T extends Collection> T getReferences(Class<T> collection, boolean updated) throws IllegalAccessException, InstantiationException {
		T refs = collection.newInstance();
		TitleReference regularReferenceNotUpdated = getRegularReferenceNotUpdated();
		TitleReference announcementReference = getAnnouncementReference();
		TitleReference concretizedReference = getConcretizedReferenceWithEpisodesRange();
		TitleReference concretizedAndOngoingReference = buildConcretizedAndOngoingReference();
		if (updated) {
			regularReferenceNotUpdated.setMinOnAnimedia("1");
			// TODO: 17.12.2019 uncomment when animedia improve api object that will return max episode in a season
//			regularReferenceNotUpdated.setMaxConcretizedEpisodeOnAnimedia("5");
			regularReferenceNotUpdated.setCurrentMaxOnAnimedia("5");
			concretizedAndOngoingReference.setCurrentMaxOnAnimedia("5");
		}
		refs.add(regularReferenceNotUpdated);
		refs.add(announcementReference);
		refs.add(concretizedReference);
		refs.add(concretizedAndOngoingReference);
		return refs;
	}

	public static TitleReference buildConcretizedAndOngoingReference() {
		return TitleReference.builder()
				.urlOnAnimedia(CONCRETIZED_AND_ONGOING_TITLE_URL)
				.dataListOnAnimedia("3")
				.animeIdOnAnimedia(CONCRETIZED_AND_ONGOING_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_AND_ONGOING_TITLE_NAME)
				.titleIdOnMAL(CONCRETIZED_AND_ONGOING_TITLE_MAL_ANIME_ID)
				.minOnAnimedia("1")
				.maxOnAnimedia("5")
				.minOnMAL("1")
				.maxOnMAL("5")
				.build();
	}

	public static TitleReference getConcretizedReferenceWithEpisodesRange() {
		return TitleReference.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_URL)
				.dataListOnAnimedia("7")
				.animeIdOnAnimedia(CONCRETIZED_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME)
				.titleIdOnMAL(CONCRETIZED_TITLE_MAL_ANIME_ID)
				.minOnAnimedia("1")
				.maxOnAnimedia("6")
				.minOnMAL("1")
				.maxOnMAL("6")
				.currentMaxOnAnimedia("6")
				.build();
	}

	public static TitleReference getConcretizedReferenceWithSingleEpisode() {
		return TitleReference.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_URL)
				.animeIdOnAnimedia(CONCRETIZED_TITLE_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME)
				.titleIdOnMAL(CONCRETIZED_TITLE_MAL_ANIME_ID)
				.dataListOnAnimedia("7")
				.minOnAnimedia("7")
				.maxOnAnimedia("7")
				.currentMaxOnAnimedia("7")
				.minOnMAL("1")
				.maxOnMAL("1")
				.build();
	}

	public static TitleReference getAnnouncementReference() {
		return TitleReference.builder()
				.urlOnAnimedia(ANNOUNCEMENT_TITLE_URL)
				.animeIdOnAnimedia(ANNOUNCEMENT_TITLE_ID)
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.titleNameOnMAL(ANNOUNCEMENT_TITLE_NAME)
				.titleIdOnMAL(ANNOUNCEMENT_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static TitleReference getRegularReferenceNotUpdated() {
		return TitleReference.builder()
				.urlOnAnimedia(REGULAR_TITLE_URL)
				.animeIdOnAnimedia(REGULAR_TITLE_ID)
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.titleNameOnMAL(REGULAR_TITLE_NAME)
				.titleIdOnMAL(REGULAR_TITLE_MAL_ID)
				.build();
	}

	public static TitleReference notFoundOnAnimedia() {
		return TitleReference.builder()
				.urlOnAnimedia("anime/something")
				.animeIdOnAnimedia("1234")
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.titleNameOnMAL(NOT_FOUND_ON_MAL)
				.build();
	}
}
