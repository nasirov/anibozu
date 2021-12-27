package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.JUTSU_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JutsuParserI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import org.apache.commons.lang3.tuple.Pair;
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
class JutsuEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<String> {

	private static final String RUNTIME_EPISODE_NAME = "2 серия";

	@Mock
	private JutsuParserI jutsuParser;

	@InjectMocks
	private JutsuEpisodeNameAndUrlService jutsuEpisodeUrlService;

	@Test
	@Override
	void shouldReturnNameAndUrlForAvailableEpisode() {
		super.shouldReturnNameAndUrlForAvailableEpisode();
	}

	@Test
	@Override
	void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		super.shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime();
	}

	@Test
	@Override
	void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		super.shouldReturnNotFoundOnFandubSiteNameAndUrl();
	}

	@Test
	@Override
	void shouldReturnNameAndUrlForNotAvailableEpisode() {
		super.shouldReturnNameAndUrlForNotAvailableEpisode();
	}

	@Test
	@Override
	void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		super.shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime();
	}

	@Override
	protected String getRuntimeExpectedResponse() {
		return "foobar";
	}

	@Override
	protected String getFandubUrl() {
		return JUTSU_URL;
	}

	@Override
	protected void mockGetRuntimeResponse(String runtimeExpectedResponse, CommonTitle commonTitle) {
		HttpRequestServiceDto<String> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.jutsu(commonTitle);
		doReturn(Mono.just(runtimeExpectedResponse)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return jutsuEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JUTSU;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(JUTSU_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-1.html")
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-2.html")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JUTSU_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JUTSU_URL + "/episode-1.html"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JUTSU_URL + "/episode-2.html"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(String runtimeExpectedResponse) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(jutsuParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(runtimeExpectedResponse)));
	}
}