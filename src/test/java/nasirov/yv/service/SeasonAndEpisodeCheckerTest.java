package nasirov.yv.service;

import static java.util.Collections.emptyMap;
import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getAnimediaSearchList;
import static nasirov.yv.utils.ReferencesTestBuilder.buildAnnouncement;
import static nasirov.yv.utils.ReferencesTestBuilder.buildConcretizedTitleWithEpisodesRange;
import static nasirov.yv.utils.ReferencesTestBuilder.buildConcretizedTitleWithSingleEpisode;
import static nasirov.yv.utils.ReferencesTestBuilder.buildMultiSeasonsTitle;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_ANIME_URL;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_TITLE_ID;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_USERNAME;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_URL;
import static nasirov.yv.utils.TestUtils.getEpisodesRange;
import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class SeasonAndEpisodeCheckerTest extends AbstractTest {

	@Before
	@Override
	public void setUp() {
		super.setUp();
		doReturn(getReferences()).when(referencesManager)
				.getMultiSeasonsReferences();
	}

	@Test
	public void handleZeroMatchedResultInAnimediaSearchList() {
		UserMALTitleInfo notFound = buildWatchingTitle(NOT_FOUND_ON_ANIMEDIA_TITLE_NAME, 0, NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(notFound),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertTrue(matchedAnime.isEmpty());
		assertTrue(notFoundAnimeOnAnimediaRepository.findAll()
				.contains(notFound));
	}

	@Test
	public void handleOneMatchedResultInAnimediaSearchListNewEpisodeAvailable() {
		stubSingleSeasonTitle();
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 0, SINGLE_SEASON_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(singleSeasonAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences singleSeasonTitle = singleSeasonTitle("1", null);
		assertTrue(matchedAnime.contains(singleSeasonTitle));
	}

	@Test
	public void handleOneMatchedResultInAnimediaSearchListNewEpisodeNotAvailable() {
		stubSingleSeasonTitle();
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 70, SINGLE_SEASON_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(singleSeasonAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences singleSeasonTitle = singleSeasonTitle(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		assertTrue(matchedAnime.contains(singleSeasonTitle));
	}

	@Test
	public void handleOneMatchedResultInAnimediaSearchListHandleAnnouncements() {
		stubAnimeMainPageAndDataLists(ANNOUNCEMENT_TITLE_URL, "animedia/announcements/htmlWithAnnouncement.txt", null, emptyMap());
		UserMALTitleInfo singleSeasonTitleAnnouncement = buildWatchingTitle(ANNOUNCEMENT_TITLE_NAME, 0, ANNOUNCEMENT_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(singleSeasonTitleAnnouncement),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences announcementReference = buildAnnouncement();
		assertTrue(matchedAnime.contains(announcementReference));
	}

	@Test
	public void handleMoreThanOneMatchedResultInAnimediaSearchList() {
		stubSingleSeasonTitle();
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 0, SINGLE_SEASON_TITLE_POSTER_URL);
		Set<AnimediaTitleSearchInfo> animediaSearchListWithDuplicatedTitle = getAnimediaSearchListWithDuplicatedTitle();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(singleSeasonAnime),
				getReferences(),
				animediaSearchListWithDuplicatedTitle,
				TEST_USERNAME);
		assertTrue(matchedAnime.isEmpty());
	}

	@Test
	public void handleOneMatchedResultInMultiSeasonsReferencesNewEpisodeAvailable() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(MULTI_SEASONS_TITLE_NAME, 0, MULTI_SEASONS_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsTitle = multiSeasonsTitle("1", null);
		assertTrue(matchedAnime.contains(multiSeasonsTitle));
	}

	@Test
	public void handleOneMatchedResultInMultiSeasonsReferencesNewEpisodeNotAvailable() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(MULTI_SEASONS_TITLE_NAME, 12, MULTI_SEASONS_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsTitle = multiSeasonsTitle(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		assertTrue(matchedAnime.contains(multiSeasonsTitle));
	}

	@Test
	public void handleOneMatchedResultInMultiSeasonsReferencesIsConcretizedTitleWithSingleEpisodeAvailable() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME, 0, CONCRETIZED_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithSingleEpisode = multiSeasonsConcretizedTitleWithSingleEpisode("1", null);
		assertTrue(matchedAnime.contains(multiSeasonsConcretizedTitleWithSingleEpisode));
	}

	@Test
	public void handleOneMatchedResultInMultiSeasonsReferencesIsConcretizedTitleWithSingleEpisodeNotAvailable() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME, 1, CONCRETIZED_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithSingleEpisode = multiSeasonsConcretizedTitleWithSingleEpisode(
				EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		assertTrue(matchedAnime.contains(multiSeasonsConcretizedTitleWithSingleEpisode));
	}

	@Test
	public void handleOneMatchedResultInMultiSeasonsReferencesIsConcretizedTitleWithEpisodesRange() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME, 0, CONCRETIZED_TITLE_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithEpisodesRange = multiSeasonsConcretizedTitleWithEpisodesRange("1", null);
		assertTrue(matchedAnime.contains(multiSeasonsConcretizedTitleWithEpisodesRange));
	}

	@Test
	public void handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataListOneMatch() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				1,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithJoinedEpisodes = multiSeasonsConcretizedTitleWithJoinedEpisodesPart1("2", null);
		assertTrue(matchedAnime.contains(multiSeasonsConcretizedTitleWithJoinedEpisodes));
	}

	@Test
	public void handleMoreThanOneMatchedResultInMultiSeasonsReferencesOnSameDataListSeveralMatches() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				3,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithEpisodesRange = multiSeasonsConcretizedTitleWithJoinedEpisodesPart2("4", null);
		assertTrue(matchedAnime.contains(multiSeasonsConcretizedTitleWithEpisodesRange));
	}

	@Test
	public void handleMoreThanOneMatchedResultInMultiSeasonsReferences() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(TITLE_ON_SEVERAL_DATA_LISTS_NAME, 869, TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences singleSeasonTitlePlacedOnSeveralDataLists = singleSeasonTitlePlacedOnSeveralDataLists("870", null);
		assertTrue(matchedAnime.contains(singleSeasonTitlePlacedOnSeveralDataLists));
	}

	@Test
	public void handleMoreThanOneMatchedResultInMultiSeasonsReferencesJoinedEpisode() {
		UserMALTitleInfo multiSeasonsAnime = buildWatchingTitle(TITLE_WITH_JOINED_EPISODES_NAME, 2, TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(Sets.newLinkedHashSet(multiSeasonsAnime),
				getReferences(),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, matchedAnime.size());
		AnimediaMALTitleReferences singleSeasonTitleWithJoinedEpisodes = referenceWithJoinedEpisodes("3", "2");
		assertTrue(matchedAnime.contains(singleSeasonTitleWithJoinedEpisodes));
	}

	@Test
	public void updateMatchedAnimeBasedOnCurrentlyUpdatedTitleNewEpisodeAvailable() {
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 69, SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences currentlyUpdatedTitle = buildCurrentlyUpdatedTitle(singleSeasonTitle("69", null), null);
		AnimediaMALTitleReferences matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange = singleSeasonTitle("69", null, "70");
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnime),
				currentlyUpdatedTitle,
				Sets.newLinkedHashSet(matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange));
		assertEquals(singleSeasonTitle("70", null, "70"), matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange);
	}

	@Test
	public void updateMatchedAnimeBasedOnCurrentlyUpdatedTitleNewEpisodeNotAvailable() {
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 69, SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences currentlyUpdatedTitle = buildCurrentlyUpdatedTitle(singleSeasonTitle("68", null), null);
		AnimediaMALTitleReferences matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange = singleSeasonTitle("69", null);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnime),
				currentlyUpdatedTitle,
				Sets.newLinkedHashSet(matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange));
		assertEquals(singleSeasonTitle(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE),
				matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange);
	}

	@Test
	public void updateMatchedAnimeBasedOnCurrentlyUpdatedTitleCurrentMaxIsRange() {
		UserMALTitleInfo singleSeasonAnime = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME, 69, SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences currentlyUpdatedTitle = buildCurrentlyUpdatedTitle(singleSeasonTitle("69", null), "70-71");
		AnimediaMALTitleReferences matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange = singleSeasonTitle("70", null, "70-71");
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnime),
				currentlyUpdatedTitle,
				Sets.newLinkedHashSet(matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange));
		assertEquals(singleSeasonTitle("70", null, "70-71"), matchedAnimeFromCacheWithUpdatedCurrentMaxAndEpisodesRange);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleNewEpisodeAvailable() {
		UserMALTitleInfo singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME,
				1,
				SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences matchedAnimeFromCache = singleSeasonTitle("1", null);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes),
				Sets.newLinkedHashSet(matchedAnimeFromCache),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(singleSeasonTitle("2", null), matchedAnimeFromCache);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleNewEpisodeNotAvailable() {
		UserMALTitleInfo singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME,
				69,
				SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences matchedAnimeFromCache = singleSeasonTitle("69", null);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes),
				Sets.newLinkedHashSet(matchedAnimeFromCache),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(singleSeasonTitle(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE),
				matchedAnimeFromCache);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleCurrentMaxIsRange() {
		UserMALTitleInfo singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(SINGLE_SEASON_TITLE_NAME,
				69,
				SINGLE_SEASON_TITLE_POSTER_URL);
		AnimediaMALTitleReferences matchedAnimeFromCache = singleSeasonTitle("70", null, "70-71");
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(singleSeasonAnimeWithUpdatedNumberOfWatchedEpisodes),
				Sets.newLinkedHashSet(matchedAnimeFromCache),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(singleSeasonTitle("70", null, "70-71"), matchedAnimeFromCache);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleConcretizedWithSingleEpisodeNewEpisodeNotAvailable() {
		UserMALTitleInfo concretizedAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME,
				1,
				CONCRETIZED_TITLE_POSTER_URL);
		AnimediaMALTitleReferences matchedAnimeFromCache = concretizedTitleWithSingleEpisode("1", null);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(concretizedAnimeWithUpdatedNumberOfWatchedEpisodes),
				Sets.newLinkedHashSet(matchedAnimeFromCache),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(concretizedTitleWithSingleEpisode(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE), matchedAnimeFromCache);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleConcretized() {
		UserMALTitleInfo concretizedAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME,
				1,
				CONCRETIZED_TITLE_POSTER_URL);
		AnimediaMALTitleReferences matchedAnimeFromCache = concretizedTitleWithEpisodesRange("1", null);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(concretizedAnimeWithUpdatedNumberOfWatchedEpisodes),
				Sets.newLinkedHashSet(matchedAnimeFromCache),
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(concretizedTitleWithEpisodesRange("2", null), matchedAnimeFromCache);
	}

	@Test
	public void updateMatchedAnimeBasedOnUpdatedWatchingTitleConcretizedWithJoinedEpisodesOnSameDataList() {
		UserMALTitleInfo concretizedAnimeWithUpdatedNumberOfWatchedEpisodes = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				2,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL);
		Set<AnimediaMALTitleReferences> cachedMatchedAnime = Sets.newLinkedHashSet(concretizedTitleWithJoinedEpisodes(
				buildConcretizedTitleWithJoinedEpisodes("1", "1", "2"),
				"1",
				null));
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(Sets.newLinkedHashSet(concretizedAnimeWithUpdatedNumberOfWatchedEpisodes),
				cachedMatchedAnime,
				getAnimediaSearchList(),
				TEST_USERNAME);
		assertEquals(1, cachedMatchedAnime.size());
		assertTrue(cachedMatchedAnime.contains(concretizedTitleWithJoinedEpisodes(buildConcretizedTitleWithJoinedEpisodes("2", "3", "4"), "3", null)));
	}

	private AnimediaMALTitleReferences concretizedTitleWithJoinedEpisodes(AnimediaMALTitleReferences concretizedTitleWithJoinedEpisodes,
			String episodeNumberForWatch, String finalUrl) {
		concretizedTitleWithJoinedEpisodes.setEpisodeNumberForWatch(episodeNumberForWatch);
		concretizedTitleWithJoinedEpisodes.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + concretizedTitleWithJoinedEpisodes.getUrl() + "/" + concretizedTitleWithJoinedEpisodes.getDataList() + "/"
						+ concretizedTitleWithJoinedEpisodes.getFirstEpisode());
		return concretizedTitleWithJoinedEpisodes;
	}

	private AnimediaMALTitleReferences concretizedTitleWithEpisodesRange(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences concretizedTitleWithEpisodesRange = buildConcretizedTitleWithEpisodesRange();
		concretizedTitleWithEpisodesRange.setEpisodeNumberForWatch(episodeNumberForWatch);
		concretizedTitleWithEpisodesRange.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + concretizedTitleWithEpisodesRange.getUrl() + "/" + concretizedTitleWithEpisodesRange.getDataList() + "/"
						+ concretizedTitleWithEpisodesRange.getEpisodeNumberForWatch());
		return concretizedTitleWithEpisodesRange;
	}

	private AnimediaMALTitleReferences concretizedTitleWithSingleEpisode(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences concretizedTitleWithSingleEpisode = buildConcretizedTitleWithSingleEpisode();
		concretizedTitleWithSingleEpisode.setEpisodeNumberForWatch(episodeNumberForWatch);
		concretizedTitleWithSingleEpisode.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + concretizedTitleWithSingleEpisode.getUrl() + "/" + concretizedTitleWithSingleEpisode.getDataList() + "/"
						+ concretizedTitleWithSingleEpisode.getFirstEpisode());
		return concretizedTitleWithSingleEpisode;
	}

	private AnimediaMALTitleReferences singleSeasonTitle(String episodeNumberForWatch, String finalUrl, String currentMax) {
		AnimediaMALTitleReferences result = singleSeasonTitle(episodeNumberForWatch, finalUrl);
		String[] range = currentMax.split("-");
		if (range.length > 1) {
			result.setCurrentMax(range[1]);
		} else {
			result.setCurrentMax(range[0]);
		}
		result.setEpisodesRange(getEpisodesRange(result.getFirstEpisode(), currentMax));
		return result;
	}

	private AnimediaMALTitleReferences buildCurrentlyUpdatedTitle(AnimediaMALTitleReferences reference, String currentMaxRange) {
		return AnimediaMALTitleReferences.builder()
				.titleOnMAL(reference.getTitleOnMAL())
				.url(reference.getUrl())
				.dataList(reference.getDataList())
				.currentMax(currentMaxRange == null ? String.valueOf(Integer.parseInt(reference.getCurrentMax()) + 1) : currentMaxRange)
				.build();
	}

	private Set<AnimediaMALTitleReferences> getReferences() {
		return Sets.newLinkedHashSet(buildTitleOnSeveralDataLists("1", "1", "175", "175"),
				buildTitleOnSeveralDataLists("2", "176", "351", "351"),
				buildTitleOnSeveralDataLists("3", "352", "527", "527"),
				buildTitleOnSeveralDataLists("4", "528", "700", "700"),
				buildTitleOnSeveralDataLists("5", "701", "xxx", "870"),
				buildConcretizedTitleWithSingleEpisode(),
				buildConcretizedTitleWithEpisodesRange(),
				buildMultiSeasonsTitle(),
				buildConcretizedTitleWithJoinedEpisodes("1", "1", "2"),
				buildConcretizedTitleWithJoinedEpisodes("2", "3", "4"),
				buildTitleWithJoinedEpisodesUrl());
	}

	private AnimediaMALTitleReferences referenceWithJoinedEpisodes(String episodeNumberForWatch, String episodeNumberForWatchForUrl) {
		AnimediaMALTitleReferences result = buildTitleWithJoinedEpisodesUrl();
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + episodeNumberForWatchForUrl);
		return result;
	}

	private AnimediaMALTitleReferences singleSeasonTitlePlacedOnSeveralDataLists(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildTitleOnSeveralDataLists("5", "701", "xxx", "870");
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getCurrentMax());
		return result;
	}

	private AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithJoinedEpisodesPart1(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildConcretizedTitleWithJoinedEpisodes("1", "1", "2");
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getFirstEpisode());
		return result;
	}

	private AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithJoinedEpisodesPart2(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildConcretizedTitleWithJoinedEpisodes("2", "3", "4");
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getFirstEpisode());
		return result;
	}

	private AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithEpisodesRange(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildConcretizedTitleWithEpisodesRange();
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getFirstEpisode());
		return result;
	}

	private AnimediaMALTitleReferences multiSeasonsConcretizedTitleWithSingleEpisode(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildConcretizedTitleWithSingleEpisode();
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getFirstEpisode());
		return result;
	}

	private AnimediaMALTitleReferences multiSeasonsTitle(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = buildMultiSeasonsTitle();
		result.setEpisodeNumberForWatch(episodeNumberForWatch);
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getEpisodeNumberForWatch());
		return result;
	}

	private Set<AnimediaTitleSearchInfo> getAnimediaSearchListWithDuplicatedTitle() {
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		AnimediaTitleSearchInfo duplicatedSearchTitle = new AnimediaTitleSearchInfo("название тайтла",
				SINGLE_SEASON_TITLE_NAME,
				"anime/some-url",
				"https://static.animedia.tv/uploads/12312.jpg?h=350&q=100");
		animediaSearchList.add(duplicatedSearchTitle);
		return animediaSearchList;
	}

	private AnimediaMALTitleReferences singleSeasonTitle(String episodeNumberForWatch, String finalUrl) {
		AnimediaMALTitleReferences result = AnimediaMALTitleReferences.builder()
				.url(SINGLE_SEASON_ANIME_URL)
				.titleOnMAL(SINGLE_SEASON_TITLE_NAME)
				.dataList("1")
				.firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("xxx")
				.currentMax("69")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + SINGLE_SEASON_TITLE_POSTER_URL)
				.episodeNumberForWatch(episodeNumberForWatch)
				.episodesRange(getEpisodesRange("1", "69"))
				.build();
		result.setFinalUrl(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + result.getUrl() + "/" + result.getDataList() + "/" + result.getEpisodeNumberForWatch());
		return result;
	}

	private AnimediaMALTitleReferences buildTitleOnSeveralDataLists(String dataList, String firstEpisode, String maxConcretizedEpisodeOnAnimedia,
			String currentMax) {
		return AnimediaMALTitleReferences.builder()
				.url(TITLE_ON_SEVERAL_DATA_LISTS_URL)
				.titleOnMAL(TITLE_ON_SEVERAL_DATA_LISTS_NAME)
				.dataList(dataList)
				.firstEpisode(firstEpisode)
				.minConcretizedEpisodeOnAnimedia(firstEpisode)
				.maxConcretizedEpisodeOnAnimedia(maxConcretizedEpisodeOnAnimedia)
				.currentMax(currentMax)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL)
				.episodesRange(getEpisodesRange(firstEpisode, currentMax))
				.build();
	}

	private UserMALTitleInfo buildWatchingTitle(String titleName, int numWatchedEpisodes, String posterUrl) {
		return new UserMALTitleInfo(0, WATCHING.getCode(), numWatchedEpisodes, titleName, 0, MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl, "animeUrl");
	}

	private AnimediaMALTitleReferences buildTitleWithJoinedEpisodesUrl() {
		return AnimediaMALTitleReferences.builder()
				.url(TITLE_WITH_JOINED_EPISODES_URL)
				.titleOnMAL(TITLE_WITH_JOINED_EPISODES_NAME)
				.dataList("1")
				.firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("5")
				.currentMax("5")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + TITLE_WITH_JOINED_EPISODES_POSTER_URL)
				.episodesRange(Lists.newArrayList("1", "2-3", "4", "5"))
				.build();
	}

	private AnimediaMALTitleReferences buildConcretizedTitleWithJoinedEpisodes(String episodeOnAnimedia, String minConcretizedEpisodeOnMAL,
			String maxConcretizedEpisodeOnMAL) {
		return AnimediaMALTitleReferences.builder()
				.url(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL)
				.titleOnMAL(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME)
				.dataList("2")
				.firstEpisode(episodeOnAnimedia)
				.minConcretizedEpisodeOnAnimedia(episodeOnAnimedia)
				.maxConcretizedEpisodeOnAnimedia(episodeOnAnimedia)
				.currentMax(episodeOnAnimedia)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL)
				.minConcretizedEpisodeOnMAL(minConcretizedEpisodeOnMAL)
				.maxConcretizedEpisodeOnMAL(maxConcretizedEpisodeOnMAL)
				.build();
	}

	private void stubSingleSeasonTitle() {
		stubAnimeMainPageAndDataLists(SINGLE_SEASON_ANIME_URL,
				"animedia/blackClover/blackCloverHtml.txt",
				SINGLE_SEASON_TITLE_ID,
				of("1", "animedia/blackClover/blackCloverDataList1.txt"));
	}
}