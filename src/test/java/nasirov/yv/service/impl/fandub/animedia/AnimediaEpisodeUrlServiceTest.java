package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.buildUpdatedRegularAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnnouncementAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getConcretizedAnimediaTitleWithEpisodesRange;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getConcretizedAnimediaTitleWithSingleEpisode;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_ID;
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
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_WITH_EPISODES_RANGE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
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
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.mal.MalTitle;
import org.assertj.core.util.Lists;
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
		mockGitHubResourcesService(getAnimediaTitles());
	}

	@Test
	public void handleZeroMatchedResult() {
		MalTitle notFoundOnAnimediaTitle = buildWatchingTitle(NOT_FOUND_ON_ANIMEDIA_MAL_ANIME_ID, NOT_FOUND_ON_ANIMEDIA_TITLE_NAME,
				0,
				NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL,
				NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL);
		performAndCheck(notFoundOnAnimediaTitle, NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	@Test
	public void handleOneMatchedResultAnnouncement() {
		MalTitle announcementTitle = buildWatchingTitle(ANNOUNCEMENT_TITLE_MAL_ANIME_ID, ANNOUNCEMENT_TITLE_NAME,
				0,
				ANNOUNCEMENT_TITLE_POSTER_URL,
				ANNOUNCEMENT_TITLE_MAL_ANIME_URL);
		performAndCheck(announcementTitle, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Test
	public void handleOneMatchedResultNewEpisodeAvailable() {
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID,
				REGULAR_TITLE_NAME,
				0,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForAnimediaTitle("1", null));
	}

	@Test
	public void handleOneMatchedResultNewEpisodeNotAvailable() {
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID,
				REGULAR_TITLE_NAME,
				12,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForAnimediaTitle(null, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeAvailable() {
		MalTitle title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_MAL_ANIME_ID, CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME,
				0,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedAnimediaTitleWithSingleEpisode(null));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithSingleEpisodeNotAvailable() {
		MalTitle title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_MAL_ANIME_ID, CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME,
				1,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedAnimediaTitleWithSingleEpisode(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
	}

	@Test
	public void handleOneMatchedResultIsConcretizedTitleWithEpisodesRange() {
		MalTitle title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_WITH_EPISODES_RANGE_MAL_ANIME_ID, CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME,
				0,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedAnimediaTitleWithEpisodesRange());
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListOneMatch() {
		MalTitle title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID, CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				1,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedAnimediaTitleWithJoinedEpisodesPart1());
	}

	@Test
	public void handleMoreThanOneMatchedResultOnSameDataListSeveralMatches() {
		MalTitle title = buildWatchingTitle(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID, CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME,
				3,
				CONCRETIZED_TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL);
		performAndCheck(title, urlForConcretizedAnimediaTitleWithJoinedEpisodesPart2());
	}

	@Test
	public void handleMoreThanOneMatchedResult() {
		MalTitle title = buildWatchingTitle(TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_ID, TITLE_ON_SEVERAL_DATA_LISTS_NAME,
				9,
				TITLE_ON_SEVERAL_DATA_LISTS_POSTER_URL,
				TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_URL);
		performAndCheck(title, urlForAnimediaTitlePlacedOnSeveralDataLists());
	}

	@Test
	public void handleMoreThanOneMatchedResultJoinedEpisode() {
		MalTitle title = buildWatchingTitle(TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID, TITLE_WITH_JOINED_EPISODES_NAME,
				2,
				TITLE_WITH_JOINED_EPISODES_POSTER_URL,
				TITLE_WITH_JOINED_EPISODES_MAL_ANIME_URL);
		performAndCheck(title,
				ANIMEDIA_ONLINE_TV + buildAnimediaTitlesWithJoinedEpisodesUrl().getUrlOnAnimedia() + "/"
						+ buildAnimediaTitlesWithJoinedEpisodesUrl().getDataListOnAnimedia() + "/" + "2");
	}

	private void performAndCheck(MalTitle watchingTitle, String expectedUrl) {
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(watchingTitle);
		assertEquals(expectedUrl, actualUrl);
	}

	private void mockGitHubResourcesService(List<AnimediaTitle> animediaTitles) {
		doReturn(animediaTitles).when(githubResourcesService)
				.getResource("animediaTitles.json", AnimediaTitle.class);
	}

	private List<AnimediaTitle> getAnimediaTitles() {
		return Lists.newArrayList(buildAnimediaTitleOnSeveralDataLists("1", "1", "2", "2"),
				buildAnimediaTitleOnSeveralDataLists("2", "3", "4", "4"),
				buildAnimediaTitleOnSeveralDataLists("3", "5", "6", "6"),
				buildAnimediaTitleOnSeveralDataLists("4", "7", "8", "8"),
				builtLastDataList(),
				getConcretizedAnimediaTitleWithSingleEpisode(),
				getConcretizedAnimediaTitleWithEpisodesRange(),
				buildUpdatedRegularAnimediaTitle(),
				buildConcretizedAnimediaTitleWithJoinedEpisodes("1", "1", "2"),
				buildConcretizedAnimediaTitleWithJoinedEpisodes("2", "3", "4"),
				buildAnimediaTitlesWithJoinedEpisodesUrl(),
				getAnnouncementAnimediaTitle());
	}

	private AnimediaTitle builtLastDataList() {
		return buildAnimediaTitleOnSeveralDataLists("5", "9", "xxx", "10");
	}

	private String urlForAnimediaTitlePlacedOnSeveralDataLists() {
		AnimediaTitle animediaTitle = builtLastDataList();
		return ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/"
				+ animediaTitle.getCurrentMaxOnAnimedia();
	}

	private String urlForConcretizedAnimediaTitleWithJoinedEpisodesPart1() {
		AnimediaTitle animediaTitle = buildConcretizedAnimediaTitleWithJoinedEpisodes("1", "1", "2");
		return ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/"
				+ animediaTitle.getMinOnAnimedia();
	}

	private String urlForConcretizedAnimediaTitleWithJoinedEpisodesPart2() {
		AnimediaTitle animediaTitle = buildConcretizedAnimediaTitleWithJoinedEpisodes("2", "3", "4");
		return ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/"
				+ animediaTitle.getMinOnAnimedia();
	}

	private String urlForConcretizedAnimediaTitleWithEpisodesRange() {
		AnimediaTitle animediaTitle = getConcretizedAnimediaTitleWithEpisodesRange();
		return ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/"
				+ animediaTitle.getMinOnAnimedia();
	}

	private String urlForConcretizedAnimediaTitleWithSingleEpisode(String finalUrl) {
		AnimediaTitle animediaTitle = getConcretizedAnimediaTitleWithSingleEpisode();
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/"
						+ animediaTitle.getMinOnAnimedia();
	}

	private String urlForAnimediaTitle(String episodeNumberForWatch, String finalUrl) {
		AnimediaTitle animediaTitle = buildUpdatedRegularAnimediaTitle();
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(finalUrl) ? finalUrl
				: ANIMEDIA_ONLINE_TV + animediaTitle.getUrlOnAnimedia() + "/" + animediaTitle.getDataListOnAnimedia() + "/" + episodeNumberForWatch;
	}

	private AnimediaTitle buildAnimediaTitleOnSeveralDataLists(String dataList, String firstEpisode, String maxConcretizedEpisodeOnAnimedia,
			String currentMax) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(TITLE_ON_SEVERAL_DATA_LISTS_URL)
				.dataListOnAnimedia(TITLE_ON_SEVERAL_DATA_LISTS_ID)
				.titleNameOnMAL(TITLE_ON_SEVERAL_DATA_LISTS_NAME)
				.titleIdOnMal(TITLE_ON_SEVERAL_DATA_LISTS_MAL_ANIME_ID)
				.dataListOnAnimedia(dataList)
				.minOnAnimedia(firstEpisode)
				.maxOnAnimedia(maxConcretizedEpisodeOnAnimedia)
				.currentMaxOnAnimedia(currentMax)
				.episodesRangeOnAnimedia(getEpisodesRange(firstEpisode, currentMax))
				.build();
	}

	private AnimediaTitle buildAnimediaTitlesWithJoinedEpisodesUrl() {
		return AnimediaTitle.builder()
				.urlOnAnimedia(TITLE_WITH_JOINED_EPISODES_URL)
				.animeIdOnAnimedia(TITLE_WITH_JOINED_EPISODES_ID)
				.titleNameOnMAL(TITLE_WITH_JOINED_EPISODES_NAME)
				.titleIdOnMal(TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID)
				.dataListOnAnimedia("1")
				.minOnAnimedia("1")
				.maxOnAnimedia("5")
				.currentMaxOnAnimedia("5")
				.episodesRangeOnAnimedia(Lists.newArrayList("1", "2-3", "4", "5"))
				.build();
	}

	private AnimediaTitle buildConcretizedAnimediaTitleWithJoinedEpisodes(String episodeOnAnimedia, String minConcretizedEpisodeOnMAL,
			String maxConcretizedEpisodeOnMAL) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_URL)
				.animeIdOnAnimedia(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_ID)
				.titleNameOnMAL(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_NAME)
				.titleIdOnMal(CONCRETIZED_TITLE_WITH_JOINED_EPISODES_MAL_ANIME_ID)
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

	private MalTitle buildWatchingTitle(int animeId, String titleName, int numWatchedEpisodes, String posterUrl, String animeUrl) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.name(titleName)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl)
				.animeUrl(MY_ANIME_LIST_URL + animeUrl)
				.build();
	}
}