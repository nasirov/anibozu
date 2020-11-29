package nasirov.yv.service.impl.fandub.nine_anime;

import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.NineAnimeParserI;
import nasirov.yv.fandub.service.spring.boot.starter.service.NineAnimeServiceI;
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
public class NineAnimeEpisodeUrlServiceTest extends BaseEpisodeUrlsServiceTest {

	@Mock
	private NineAnimeServiceI nineAnimeService;

	@Mock
	private NineAnimeParserI nineAnimeParser;

	@InjectMocks
	private NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService;

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
		return NINE_ANIME_TO;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return nineAnimeParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(nineAnimeService)
				.getTitleEpisodes(REGULAR_TITLE_NINE_ANIME_ID);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return nineAnimeEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.NINEANIME;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-1")
						.build(),
				FandubEpisode.builder()
						.name("2")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-2")
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1", actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_NINE_ANIME_URL + "/ep-2", actualUrl);
	}
}