package nasirov.yv.service.impl.fandub.animepik;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildAnimepikTitles;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.anime_pik.AnimepikTitle;
import nasirov.yv.fandub.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animepik.AnimepikResourcesFeignClient;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import nasirov.yv.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Created by nasirov.yv
 */
public class AnimepikEpisodeUrlServiceTest extends AbstractTest {

	@MockBean
	private AnimepikResourcesFeignClient animepikResourcesFeignClient;

	private BaseEpisodeUrlService<AnimepikTitle> animepikEpisodeUrlService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		mockGitHubResourcesService(buildAnimepikTitles());
		animepikEpisodeUrlService = new AnimepikEpisodeUrlService(new AnimepikTitleService(githubResourcesService, gitHubResourceProps),
				animepikResourcesFeignClient,
				urlsNames,
				animepikParser);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAnimepikResources();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 0);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotAvailableUrlConstant() {
		//given
		mockAnimepikResources();
		MalTitle title = buildWatchingTitle(REGULAR_TITLE_MAL_ANIME_ID, 3);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrlConstant() {
		//given
		mockAnimepikResources();
		int notRegularTitleId = 5;
		MalTitle title = buildWatchingTitle(notRegularTitleId, 0);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(title);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	private void mockAnimepikResources() {
		doReturn(IOUtils.unmarshal(IOUtils.readFromFile("classpath:__files/animepik/episodesForRegularTitle.html"),
				AnimepikEpisode.class,
				ArrayList.class)).when(animepikResourcesFeignClient)
				.getTitleEpisodes(1);
	}

	private void mockGitHubResourcesService(List<AnimepikTitle> titles) {
		doReturn(titles).when(githubResourcesService)
				.getResource("animepikTitles.json", AnimepikTitle.class);
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