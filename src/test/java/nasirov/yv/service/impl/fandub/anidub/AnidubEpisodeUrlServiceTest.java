package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.utils.TestConstants.ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnidubParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anidub.AnidubFeignClient;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlsServiceTest;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnidubEpisodeUrlServiceTest extends BaseEpisodeUrlsServiceTest {

	@Mock
	private AnidubFeignClient anidubFeignClient;

	@Mock
	private AnidubParserI anidubParser;

	@InjectMocks
	private AnidubEpisodeUrlService anidubEpisodeUrlService;

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
		return ANIDUB_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return anidubParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(anidubFeignClient)
				.getTitlePage(REGULAR_TITLE_ANIDUB_URL);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return anidubEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.ANIDUB;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 серия")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIDUB_URL)
						.build(),
				FandubEpisode.builder()
						.name("2 серия")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANIDUB_URL)
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_ANIDUB_URL, actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_ANIDUB_URL, actualUrl);
	}
}