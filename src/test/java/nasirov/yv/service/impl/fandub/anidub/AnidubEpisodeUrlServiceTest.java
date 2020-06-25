package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubTitles;
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

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.anidub.AnidubTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnidubEpisodeUrlServiceTest extends AbstractTest {

	private BaseEpisodeUrlService<AnidubTitle> anidubEpisodeUrlService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildAnidubTitles());
		anidubEpisodeUrlService = new AnidubEpisodeUrlService(anidubFeignClient, new AnidubTitleService(githubResourcesService, gitHubResourceProps),
				anidubParser,
				urlsNames);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAnidub();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 0);
		//when
		String actualUrl = anidubEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(ANIDUB_URL + REGULAR_TITLE_ANIDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotAvailableUrlConstant() {
		//given
		mockAnidub();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 3);
		//when
		String actualUrl = anidubEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrlConstant() {
		//given
		mockAnidub();
		int notRegularTitleId = 5;
		MalTitle title = buildWatchingTitle(notRegularTitleId, 0);
		//when
		String actualUrl = anidubEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	private void mockAnidub() {
		createStubWithBodyFile("/" + REGULAR_TITLE_ANIDUB_SITE_URL, TEXT_HTML_CHARSET_UTF_8, "anidub/siteRegularTitle.html");
	}

	private void mockGitHubResourcesService(List<AnidubTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("anidubSiteTitles.json", AnidubTitle.class);
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.name(REGULAR_TITLE_NAME)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL)
				.animeUrl(MY_ANIME_LIST_URL + REGULAR_TITLE_MAL_ANIME_URL)
				.build();
	}
}