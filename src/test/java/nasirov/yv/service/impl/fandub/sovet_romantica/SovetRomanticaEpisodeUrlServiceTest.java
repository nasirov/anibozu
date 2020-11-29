package nasirov.yv.service.impl.fandub.sovet_romantica;

import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.SOVET_ROMANTICA_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.sovet_romantica.SovetRomanticaFeignClient;
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
public class SovetRomanticaEpisodeUrlServiceTest extends BaseEpisodeUrlsServiceTest {

	@Mock
	private SovetRomanticaFeignClient sovetRomanticaFeignClient;

	@Mock
	private SovetRomanticaParserI sovetRomanticaParser;

	@InjectMocks
	private SovetRomanticaEpisodeUrlService sovetRomanticaEpisodeUrlService;

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
		return SOVET_ROMANTICA_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return sovetRomanticaParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(sovetRomanticaFeignClient)
				.getTitlePage(REGULAR_TITLE_SOVET_ROMANTICA_URL);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return sovetRomanticaEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.SOVETROMANTICA;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("Эпизод 1")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles")
						.build(),
				FandubEpisode.builder()
						.name("Эпизод 2")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles")
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles", actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles", actualUrl);
	}
}