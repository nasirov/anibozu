package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.SOVET_ROMANTICA_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.SOVET_ROMANTICA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@ExtendWith(MockitoExtension.class)
public class SovetRomanticaEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest {

	private static final String RUNTIME_EPISODE_NAME = "Эпизод 2";

	@Mock
	private SovetRomanticaParserI sovetRomanticaParser;

	@InjectMocks
	private SovetRomanticaEpisodeNameAndUrlService sovetRomanticaEpisodeUrlService;

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisode() {
		super.shouldReturnNameAndUrlForAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		super.shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime();
	}

	@Test
	@Override
	public void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		super.shouldReturnNotFoundOnFandubSiteNameAndUrl();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForNotAvailableEpisode() {
		super.shouldReturnNameAndUrlForNotAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		super.shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime();
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
	protected void mockGetTitlePage(String titlePageContent, CommonTitle commonTitle) {
		HttpRequestServiceDto<String> sovetRomanticaDdosGuardDto = mock(HttpRequestServiceDto.class);
		doReturn(sovetRomanticaDdosGuardDto).when(httpRequestServiceDtoBuilder)
				.sovetRomanticaDdosGuard();
		String pageWithDdosGuardCookie = "pageWithDdosGuardCookie";
		doReturn(Mono.just(pageWithDdosGuardCookie)).when(httpRequestService)
				.performHttpRequest(sovetRomanticaDdosGuardDto);
		String cookie = "foobar42";
		doReturn(Optional.of(cookie)).when(sovetRomanticaParser)
				.extractCookie(pageWithDdosGuardCookie);
		HttpRequestServiceDto<String> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.sovetRomantica(commonTitle, cookie);
		doReturn(Mono.just(titlePageContent)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return sovetRomanticaEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.SOVETROMANTICA;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(SOVET_ROMANTICA_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles")
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(SOVET_ROMANTICA_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles"),
				episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles"), episodeNameAndUrl);
	}
}