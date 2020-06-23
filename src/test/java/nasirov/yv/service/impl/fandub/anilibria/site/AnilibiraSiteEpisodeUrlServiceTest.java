package nasirov.yv.service.impl.fandub.anilibria.site;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnilibriaTitleBuilder.buildAnilibriaTitles;
import static nasirov.yv.utils.TestConstants.ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.anilibria.site.AnilibriaSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.service.impl.common.BaseEpisodeUrlService;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnilibiraSiteEpisodeUrlServiceTest extends AbstractTest {

	private BaseEpisodeUrlService<AnilibriaSiteTitle> anilibriaEpisodeUrlService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildAnilibriaTitles());
		anilibriaEpisodeUrlService = new AnilibiraSiteEpisodeUrlService(new AnilibriaSiteTitleService(githubResourcesService, gitHubResourceProps),
				anilibriaSiteFeignClient,
				urlsNames,
				anilibriaParser);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAnilibria();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 0);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(ANILIBRIA_URL + REGULAR_TITLE_ANILIBRIA_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotAvailableUrlConstant() {
		//given
		mockAnilibria();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 3);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrlConstant() {
		//given
		mockAnilibria();
		int notRegularTitleId = 5;
		MalTitle title = buildWatchingTitle(notRegularTitleId, 0);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	private void mockAnilibria() {
		createStubWithBodyFile("/" + REGULAR_TITLE_ANILIBRIA_SITE_URL, TEXT_HTML_CHARSET_UTF_8, "anilibria/regularTitlePage.html");
	}

	private void mockGitHubResourcesService(List<AnilibriaSiteTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("anilibriaSiteTitles.json", AnilibriaSiteTitle.class);
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