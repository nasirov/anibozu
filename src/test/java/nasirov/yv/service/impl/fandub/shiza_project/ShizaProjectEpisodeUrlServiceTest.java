package nasirov.yv.service.impl.fandub.shiza_project;

import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.SHIZA_PROJECT_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.ShizaProjectParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.shiza_project.ShizaProjectFeignClient;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlsServiceTest;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class ShizaProjectEpisodeUrlServiceTest extends BaseEpisodeUrlsServiceTest {

	@Mock
	private ShizaProjectFeignClient shizaProjectFeignClient;

	@Mock
	private ShizaProjectParserI shizaProjectParser;

	@InjectMocks
	private ShizaProjectEpisodeUrlService shizaProjectEpisodeUrlService;

	@Test
	@Override
	public void shouldReturnUrlWithAvailableEpisode() {
		super.shouldReturnUrlWithAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		super.shouldReturnUrlWithAvailableEpisodeInRuntime();
	}

	@Test
	@Override
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		super.shouldReturnNotFoundOnFandubSiteUrl();
	}

	@Test
	@Override
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		super.shouldReturnFinalUrlValueIfEpisodeIsNotAvailable();
	}

	@Test
	@Override
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		super.shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime();
	}

	@Override
	protected String getFandubUrl() {
		return SHIZA_PROJECT_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return shizaProjectParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(shizaProjectFeignClient)
				.getTitlePage(REGULAR_TITLE_SHIZA_PROJECT_URL);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return shizaProjectEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.SHIZAPROJECT;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("01, video.sibnet.ru")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-1")
						.build(),
				FandubEpisode.builder()
						.name("02, video.sibnet.ru")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-2")
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-1", actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-2", actualUrl);
	}
}