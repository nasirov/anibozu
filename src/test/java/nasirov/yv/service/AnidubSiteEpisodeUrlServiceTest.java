package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubSiteTitles;
import static nasirov.yv.utils.TestConstants.ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnidubSiteEpisodeUrlServiceTest extends AbstractTest {

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildAnidubSiteTitles());
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAnidub();
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 0);
		//when
		String actualUrl = anidubSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(ANIDUB_URL + REGULAR_TITLE_ANIDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotAvailableUrlConstant() {
		//given
		mockAnidub();
		UserMALTitleInfo title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 3);
		//when
		String actualUrl = anidubSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrlConstant() {
		//given
		mockAnidub();
		int notRegularTitleId = 5;
		UserMALTitleInfo title = buildWatchingTitle(notRegularTitleId, 0);
		//when
		String actualUrl = anidubSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	private void mockAnidub() {
		createStubWithBodyFile("/" + REGULAR_TITLE_ANIDUB_SITE_URL, TEXT_HTML_CHARSET_UTF_8, "anidub/siteRegularTitle.html");
	}

	private void mockGitHubResourcesService(Set<AnidubSiteTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("anidubSiteTitles.json", AnidubSiteTitle.class);
	}

	private UserMALTitleInfo buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return new UserMALTitleInfo(animeId,
				numWatchedEpisodes,
				REGULAR_TITLE_NAME,
				MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL,
				MY_ANIME_LIST_URL + REGULAR_TITLE_MAL_ANIME_URL);
	}
}