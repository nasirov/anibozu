package nasirov.yv.service.impl.fandub.anilibria;

import static nasirov.yv.utils.TestConstants.ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnilibriaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anilibria.AnilibriaFeignClient;
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
public class AnilibriaEpisodeUrlServiceTest extends BaseEpisodeUrlsServiceTest {

	@Mock
	private AnilibriaFeignClient anilibriaFeignClient;

	@Mock
	private AnilibriaParserI anilibriaParser;

	@InjectMocks
	private AnilibriaEpisodeUrlService anilibriaEpisodeUrlService;

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
		return ANILIBRIA_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return anilibriaParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(anilibriaFeignClient)
				.getTitlePage(REGULAR_TITLE_ANILIBRIA_URL);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return anilibriaEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.ANILIBRIA;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 эпизод")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANILIBRIA_URL)
						.build(),
				FandubEpisode.builder()
						.name("2 эпизод")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANILIBRIA_URL)
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_ANILIBRIA_URL, actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_ANILIBRIA_URL, actualUrl);
	}
}