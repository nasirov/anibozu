package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.utils.ReferencesBuilder.buildConcretizedReferenceWithEpisodesRange;
import static nasirov.yv.utils.ReferencesBuilder.buildConcretizedReferenceWithSingleEpisode;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedAnnouncementReference;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedRegularReference;
import static nasirov.yv.utils.ReferencesBuilder.getAnnouncementReference;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_ID;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_ID;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class SeasonAndEpisodeCheckerTest extends AbstractTest {


	@Test
	public void handleZeroMatchedResult() {
		UserMALTitleInfo notFoundOnAnimediaTitle = buildWatchingTitle(NOT_FOUND_ON_ANIMEDIA_TITLE_NAME, 0, NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(notFoundOnAnimediaTitle),
				getReferences(),
				TEST_ACC_FOR_DEV);
		assertTrue(matchedAnime.isEmpty());
	}

	@Test
	public void handleOneMatchedResultAnnouncement() {
		UserMALTitleInfo announcementTitle = buildWatchingTitle(ANNOUNCEMENT_TITLE_NAME, 0, ANNOUNCEMENT_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(announcementTitle),
				getReferences(),
				TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference announcementReference = buildUpdatedAnnouncementReference();
		assertTrue(matchedAnime.contains(announcementReference));
	}

	@Test
	public void handleOneMatchedResultNewEpisodeAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_NAME, 0, REGULAR_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference reference = reference("1", null);
		assertTrue(matchedAnime.contains(reference));
	}

	@Test
	public void handleOneMatchedResultNewEpisodeNotAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_NAME, 12, REGULAR_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference reference = reference(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		assertTrue(matchedAnime.contains(reference));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME, 0, CONCRETIZED_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference concretizedReferenceWithSingleEpisode = concretizedReferenceWithSingleEpisode("1", null);
		assertTrue(matchedAnime.contains(concretizedReferenceWithSingleEpisode));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeNotAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME, 1, CONCRETIZED_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference concretizedReferenceWithSingleEpisode = concretizedReferenceWithSingleEpisode(
				EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		assertTrue(matchedAnime.contains(concretizedReferenceWithSingleEpisode));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithEpisodesRange() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME, 0, CONCRETIZED_TITLE_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference concretizedReferenceWithEpisodesRange = concretizedReferenceWithEpisodesRange();
		assertTrue(matchedAnime.contains(concretizedReferenceWithEpisodesRange));
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListOneMatch() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME, 1, CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference concretizedReferenceWithJoinedEpisodes = concretizedReferenceWithJoinedEpisodesPart1();
		assertTrue(matchedAnime.contains(concretizedReferenceWithJoinedEpisodes));
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListSeveralMatches() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME, 3, CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference concretizedReferenceWithEpisodesRange = concretizedReferenceWithJoinedEpisodesPart2();
		assertTrue(matchedAnime.contains(concretizedReferenceWithEpisodesRange));
	}

	@Test
	public void handleMoreThanOneMatchedResult() {
		UserMALTitleInfo title = buildWatchingTitle(TITLE_ON_SEVERAL_DATA_LISTS_NAME, 9, TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference referencePlacedOnSeveralDataLists = referencePlacedOnSeveralDataLists();
		assertTrue(matchedAnime.contains(referencePlacedOnSeveralDataLists));
	}

	@Test
	public void handleMoreThanOneMatchedResultJoinedEpisode() {
		UserMALTitleInfo title = buildWatchingTitle(TITLE_WITH_JOINED_EPISODES_NAME, 2, TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<TitleReference> matchedAnime = seasonsAndEpisodesService.getMatchedAnime(Sets.newLinkedHashSet(title), getReferences(), TEST_ACC_FOR_DEV);
		assertEquals(1, matchedAnime.size());
		TitleReference referenceWithJoinedEpisodes = referenceWithJoinedEpisodes();
		assertTrue(matchedAnime.contains(referenceWithJoinedEpisodes));
	}

	private Set<TitleReference> getReferences() {
		return Sets.newLinkedHashSet(buildReferenceOnSeveralDataLists("1", "1", "2", "2"),
				buildReferenceOnSeveralDataLists("2", "3", "4", "4"),
				buildReferenceOnSeveralDataLists("3", "5", "6", "6"),
				buildReferenceOnSeveralDataLists("4", "7", "8", "8"),
				builtLastDataList(),
				buildConcretizedReferenceWithSingleEpisode(),
				buildConcretizedReferenceWithEpisodesRange(),
				buildUpdatedRegularReference(),
				buildConcretizedReferenceWithJoinedEpisodes("1", "1", "2"),
				buildConcretizedReferenceWithJoinedEpisodes("2", "3", "4"),
				buildReferenceWithJoinedEpisodesUrl(), getAnnouncementReference());
	}

	private TitleReference builtLastDataList() {
		return buildReferenceOnSeveralDataLists("5", "9", "xxx", "10");
	}

	private TitleReference referenceWithJoinedEpisodes() {
		TitleReference result = buildReferenceWithJoinedEpisodesUrl();
		result.setEpisodeNumberForWatchForFront("3");
		result.setFinalUrlForFront(ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + "2");
		return result;
	}

	private TitleReference referencePlacedOnSeveralDataLists() {
		TitleReference result = builtLastDataList();
		result.setEpisodeNumberForWatchForFront("10");
		result.setFinalUrlForFront(
				ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getCurrentMaxOnAnimedia());
		return result;
	}

	private TitleReference concretizedReferenceWithJoinedEpisodesPart1() {
		TitleReference result = buildConcretizedReferenceWithJoinedEpisodes("1", "1", "2");
		result.setEpisodeNumberForWatchForFront("2");
		result.setFinalUrlForFront(
				ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getMinOnAnimedia());
		return result;
	}

	private TitleReference concretizedReferenceWithJoinedEpisodesPart2() {
		TitleReference result = buildConcretizedReferenceWithJoinedEpisodes("2", "3", "4");
		result.setEpisodeNumberForWatchForFront("4");
		result.setFinalUrlForFront(
				ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getMinOnAnimedia());
		return result;
	}

	private TitleReference concretizedReferenceWithEpisodesRange() {
		TitleReference result = buildConcretizedReferenceWithEpisodesRange();
		result.setEpisodeNumberForWatchForFront("1");
		result.setFinalUrlForFront(
				ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getMinOnAnimedia());
		return result;
	}

	private TitleReference concretizedReferenceWithSingleEpisode(String episodeNumberForWatch, String finalUrl) {
		TitleReference result = buildConcretizedReferenceWithSingleEpisode();
		result.setEpisodeNumberForWatchForFront(episodeNumberForWatch);
		result.setFinalUrlForFront(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getMinOnAnimedia());
		return result;
	}

	private TitleReference reference(String episodeNumberForWatch, String finalUrl) {
		TitleReference result = buildUpdatedRegularReference();
		result.setEpisodeNumberForWatchForFront(episodeNumberForWatch);
		result.setFinalUrlForFront(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrlOnAnimedia() + "/" + result.getDataListOnAnimedia() + "/" + result.getEpisodeNumberForWatchForFront());
		return result;
	}

	private TitleReference buildReferenceOnSeveralDataLists(String dataList, String firstEpisode, String maxConcretizedEpisodeOnAnimedia,
			String currentMax) {
		return TitleReference.builder()
				.urlOnAnimedia(TITLE_ON_SEVERAL_DATA_LISTS_URL)
				.dataListOnAnimedia(TITLE_ON_SEVERAL_DATA_LISTS_ID)
				.titleNameOnMAL(TITLE_ON_SEVERAL_DATA_LISTS_NAME)
				.titleIdOnMAL(TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_ID)
				.dataListOnAnimedia(dataList)
				.minOnAnimedia(firstEpisode)
				.maxOnAnimedia(maxConcretizedEpisodeOnAnimedia)
				.currentMaxOnAnimedia(currentMax)
				.posterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL)
				.episodesRangeOnAnimedia(getEpisodesRange(firstEpisode, currentMax))
				.build();
	}

	private UserMALTitleInfo buildWatchingTitle(String titleName, int numWatchedEpisodes, String posterUrl) {
		return new UserMALTitleInfo(1, numWatchedEpisodes, titleName, MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl, "animeUrl");
	}

	private TitleReference buildReferenceWithJoinedEpisodesUrl() {
		return TitleReference.builder()
				.urlOnAnimedia(TITLE_WITH_JOINED_EPISODES_URL)
				.animeIdOnAnimedia(TITLE_WITH_JOINED_EPISODES_ID)
				.titleNameOnMAL(TITLE_WITH_JOINED_EPISODES_NAME)
				.titleIdOnMAL(TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID)
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.maxOnAnimedia("5")
				.currentMaxOnAnimedia("5")
				.posterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + TITLE_WITH_JOINED_EPISODES_POSTER_URL)
				.episodesRangeOnAnimedia(Lists.newArrayList("1", "2-3", "4", "5"))
				.build();
	}

	private TitleReference buildConcretizedReferenceWithJoinedEpisodes(String episodeOnAnimedia, String minConcretizedEpisodeOnMAL,
			String maxConcretizedEpisodeOnMAL) {
		return TitleReference.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL)
				.animeIdOnAnimedia(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME)
				.titleIdOnMAL(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID)
				.dataListOnAnimedia("2")
				.minOnAnimedia(episodeOnAnimedia)
				.maxOnAnimedia(episodeOnAnimedia)
				.currentMaxOnAnimedia(episodeOnAnimedia)
				.posterUrlOnMAL(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL)
				.minOnMAL(minConcretizedEpisodeOnMAL)
				.maxOnMAL(maxConcretizedEpisodeOnMAL)
				.build();
	}

	private List<String> getEpisodesRange(String min, String max) {
		List<String> result = new LinkedList<>();
		String[] range = max.split("-");
		int intMax;
		if (range.length > 1) {
			intMax = Integer.parseInt(range[0]) - 1;
		} else {
			intMax = Integer.parseInt(range[0]);
		}
		for (int i = Integer.parseInt(min); i <= intMax; i++) {
			result.add(String.valueOf(i));
		}
		if (range.length > 1) {
			result.add(max);
		}
		return result;
	}
}