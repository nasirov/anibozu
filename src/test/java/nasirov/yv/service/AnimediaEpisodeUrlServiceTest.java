package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedRegularReference;
import static nasirov.yv.utils.ReferencesBuilder.getAnnouncementReference;
import static nasirov.yv.utils.ReferencesBuilder.getConcretizedReferenceWithEpisodesRange;
import static nasirov.yv.utils.ReferencesBuilder.getConcretizedReferenceWithSingleEpisode;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_ID;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_ON_SEVERAL_DATA_LISTS_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_ID;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_NAME;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TITLE_WITH_JOINED_EPISODES_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaEpisodeUrlServiceTest extends AbstractTest {

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockReferencesService(getReferences());
	}

	@Test
	public void handleZeroMatchedResult() {
		UserMALTitleInfo notFoundOnAnimediaTitle = buildWatchingTitle(NOT_FOUND_ON_ANIMEDIA_TITLE_NAME,
				0,
				NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL,
				NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL);
		performAndCheck(notFoundOnAnimediaTitle, NOT_FOUND_ON_FUNDUB_SITE_URL);
	}

	@Test
	public void handleOneMatchedResultAnnouncement() {
		UserMALTitleInfo announcementTitle = buildWatchingTitle(ANNOUNCEMENT_TITLE_NAME,
				0,
				ANNOUNCEMENT_TITLE_POSTER_URL,
				ANNOUNCEMENT_TITLE_MAL_ANIME_URL);
		performAndCheck(announcementTitle, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Test
	public void handleOneMatchedResultNewEpisodeAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_NAME, 0, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForReference("1", null));
	}

	@Test
	public void handleOneMatchedResultNewEpisodeNotAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_NAME, 12, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForReference(EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME,
				0,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedReferenceWithSingleEpisode(null));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeNotAvailable() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME,
				1,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedReferenceWithSingleEpisode(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithEpisodesRange() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME,
				0,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedReferenceWithEpisodesRange());
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListOneMatch() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				1,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedReferenceWithJoinedEpisodesPart1());
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListSeveralMatches() {
		UserMALTitleInfo title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				3,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedReferenceWithJoinedEpisodesPart2());
	}

	@Test
	public void handleMoreThanOneMatchedResult() {
		UserMALTitleInfo title = buildWatchingTitle(TITLE_ON_SEVERAL_DATA_LISTS_NAME,
				9,
				TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL,
				TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_URL);
		performAndCheck(title, urlForReferencePlacedOnSeveralDataLists());
	}

	@Test
	public void handleMoreThanOneMatchedResultJoinedEpisode() {
		UserMALTitleInfo title = buildWatchingTitle(TITLE_WITH_JOINED_EPISODES_NAME,
				2,
				TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				TITLE_WITH_JOINED_EPISODES_MAL_ANIME_URL);
		performAndCheck(title,
				ANIMEDIA_ONLINE_TV + buildReferenceWithJoinedEpisodesUrl().getUrlOnAnimedia() + "/"
						+ buildReferenceWithJoinedEpisodesUrl().getDataListOnAnimedia() + "/" + "2");
	}

	private void performAndCheck(UserMALTitleInfo watchingTitle, String expectedUrl) {
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(watchingTitle);
		assertEquals(expectedUrl, actualUrl);
	}

	private void mockReferencesService(Set<TitleReference> references) {
		doReturn(references).when(referencesService)
				.getReferences();
	}

	private Set<TitleReference> getReferences() {
		return Sets.newLinkedHashSet(buildReferenceOnSeveralDataLists("1", "1", "2", "2"),
				buildReferenceOnSeveralDataLists("2", "3", "4", "4"),
				buildReferenceOnSeveralDataLists("3", "5", "6", "6"),
				buildReferenceOnSeveralDataLists("4", "7", "8", "8"),
				builtLastDataList(),
				getConcretizedReferenceWithSingleEpisode(),
				getConcretizedReferenceWithEpisodesRange(),
				buildUpdatedRegularReference(),
				buildConcretizedReferenceWithJoinedEpisodes("1", "1", "2"),
				buildConcretizedReferenceWithJoinedEpisodes("2", "3", "4"),
				buildReferenceWithJoinedEpisodesUrl(),
				getAnnouncementReference());
	}

	private TitleReference builtLastDataList() {
		return buildReferenceOnSeveralDataLists("5", "9", "xxx", "10");
	}

	private String urlForReferencePlacedOnSeveralDataLists() {
		TitleReference reference = builtLastDataList();
		return ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + reference.getCurrentMaxOnAnimedia();
	}

	private String urlForConcretizedReferenceWithJoinedEpisodesPart1() {
		TitleReference reference = buildConcretizedReferenceWithJoinedEpisodes("1", "1", "2");
		return ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + reference.getMinOnAnimedia();
	}

	private String urlForConcretizedReferenceWithJoinedEpisodesPart2() {
		TitleReference reference = buildConcretizedReferenceWithJoinedEpisodes("2", "3", "4");
		return ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + reference.getMinOnAnimedia();
	}

	private String urlForConcretizedReferenceWithEpisodesRange() {
		TitleReference reference = getConcretizedReferenceWithEpisodesRange();
		return ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + reference.getMinOnAnimedia();
	}

	private String urlForConcretizedReferenceWithSingleEpisode(String finalUrl) {
		TitleReference reference = getConcretizedReferenceWithSingleEpisode();
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + reference.getMinOnAnimedia();
	}

	private String urlForReference(String episodeNumberForWatch, String finalUrl) {
		TitleReference reference = buildUpdatedRegularReference();
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + reference.getUrlOnAnimedia() + "/" + reference.getDataListOnAnimedia() + "/" + episodeNumberForWatch;
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
				.episodesRangeOnAnimedia(getEpisodesRange(firstEpisode, currentMax))
				.build();
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

	private UserMALTitleInfo buildWatchingTitle(String titleName, int numWatchedEpisodes, String posterUrl, String animeUrl) {
		return new UserMALTitleInfo(1, numWatchedEpisodes, titleName, MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl, MY_ANIME_LIST_URL + animeUrl);
	}
}