package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.SOVET_ROMANTICA_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class SovetRomanticaEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<String> {

	private static final String RUNTIME_EPISODE_NAME = "Эпизод 2";

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
	protected void mockGetRuntimeResponse(String runtimeExpectedResponse, CommonTitle commonTitle) {
		String pageWithDdosGuardCookie = "pageWithDdosGuardCookie";
		doReturn(Mono.just(pageWithDdosGuardCookie)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getSovetRomanticaDdosGuardUrl() + "check.js")));
		String cookie = "foobar42";
		doReturn(Optional.of(cookie)).when(sovetRomanticaParser)
				.extractCookie(pageWithDdosGuardCookie);
		doReturn(Mono.just(runtimeExpectedResponse)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getUrls()
								.get(getFandubSource()) + commonTitle.getUrl()) && x.getHeaders()
						.equals(Collections.singletonMap(HttpHeaders.COOKIE, cookie))));
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
		assertEquals(Pair.of(SOVET_ROMANTICA_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(String runtimeExpectedResponse) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(sovetRomanticaParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(runtimeExpectedResponse)));
	}
}