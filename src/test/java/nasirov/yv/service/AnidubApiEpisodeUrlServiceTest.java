package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubApiTitles;
import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8_ALT;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_API_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_TYPE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_TYPE_SUPPORTED_SOURCE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.mal.MalTitle;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnidubApiEpisodeUrlServiceTest extends AbstractTest {

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildAnidubApiTitles());
	}

	@Test
	public void shouldFindAvailableUrl() {
		//given
		mockAnidub("anidub/titleEpisodes.json");
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID,
				REGULAR_TITLE_NAME,
				0,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL);
		//when
		String actualUrl = anidubApiEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(REGULAR_TITLE_ANIDUB_API_URL, actualUrl);
	}

	@Test
	public void shouldFindNotAvailableUrl() {
		//given
		mockAnidub("anidub/titleEpisodes.json");
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID,
				REGULAR_TITLE_NAME,
				3,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL);
		//when
		String actualUrl = anidubApiEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldFindNotFoundOnFandubSiteUrl() {
		//given
		mockAnidub("anidub/titleEpisodes.json");
		int notRegularTitleId = 5;
		MalTitle title = buildWatchingTitle(notRegularTitleId, REGULAR_TITLE_NAME, 0, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL);
		//when
		String actualUrl = anidubApiEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldIgnoreEpisodesWithInvalidHosts() {
		//given
		mockAnidub("anidub/titleEpisodesInvalid.json");
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID,
				REGULAR_TITLE_NAME,
				1,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL);
		//when
		String actualUrl = anidubApiEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockAnidub(String episodesBody) {
		createStubWithBodyFile("/episode/" + REGULAR_TITLE_ANIDUB_ID, APPLICATION_JSON_CHARSET_UTF_8_ALT, "anidub/titleAvailableFandubs.json");
		createStubWithBodyFile("/episode/" + REGULAR_TITLE_ANIDUB_ID + "/" + REGULAR_TITLE_ANIDUB_TYPE_ID,
				APPLICATION_JSON_CHARSET_UTF_8_ALT,
				"anidub/titleFandubEpisodesSources.json");
		createStubWithBodyFile(
				"/episode/" + REGULAR_TITLE_ANIDUB_ID + "/" + REGULAR_TITLE_ANIDUB_TYPE_ID + "/" + REGULAR_TITLE_ANIDUB_TYPE_SUPPORTED_SOURCE_ID,
				APPLICATION_JSON_CHARSET_UTF_8_ALT,
				episodesBody);
	}

	private void mockGitHubResourcesService(List<AnidubApiTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("anidubApiTitles.json", AnidubApiTitle.class);
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