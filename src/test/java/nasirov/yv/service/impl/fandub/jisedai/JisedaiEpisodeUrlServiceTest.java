package nasirov.yv.service.impl.fandub.jisedai;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildJesidaiSiteTitles;
import static nasirov.yv.utils.TestConstants.JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8_ALT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.service.impl.common.BaseEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jisedai.site.JisedaiSiteTitleService;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class JisedaiEpisodeUrlServiceTest extends AbstractTest {

	private BaseEpisodeUrlService<JisedaiSiteTitle> jisedaiSiteEpisodeUrlService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildJesidaiSiteTitles());
		jisedaiSiteEpisodeUrlService = new JisedaiEpisodeUrlService(jisedaiSiteFeignClient,
				new JisedaiSiteTitleService(githubResourcesService, gitHubResourceProps),
				jisedaiParser,
				urlsNames);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockJisedai();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 0);
		//when
		String actualUrl = jisedaiSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(JISEDAI_URL + REGULAR_TITLE_JISEDAI_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotAvailableUrlConstant() {
		//given
		mockJisedai();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 3);
		//when
		String actualUrl = jisedaiSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrlConstant() {
		//given
		mockJisedai();
		int notRegularTitleId = 5;
		MalTitle title = buildWatchingTitle(notRegularTitleId, 0);
		//when
		String actualUrl = jisedaiSiteEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	private void mockJisedai() {
		createStubWithBodyFile("/" + REGULAR_TITLE_JISEDAI_SITE_URL, TEXT_HTML_CHARSET_UTF_8_ALT, "jisedai/siteRegularTitle.html");
	}

	private void mockGitHubResourcesService(List<JisedaiSiteTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("jisedaiSiteTitles.json", JisedaiSiteTitle.class);
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