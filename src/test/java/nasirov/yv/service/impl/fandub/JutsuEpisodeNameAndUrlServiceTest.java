package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.JUTSU_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class JutsuEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<String> {

	private static final String RUNTIME_EPISODE_NAME = "2 серия";

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
		doReturn(Mono.just(runtimeExpectedResponse)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getUrls()
								.get(getFandubSource()) + commonTitle.getUrl())));
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JUTSU;
	}

	@Override
	protected List<CommonEpisode> getCommonEpisodes() {
		return Lists.newArrayList(CommonEpisode.builder()
						.name(JUTSU_EPISODE_NAME)
						.malEpisodeId(1)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-1.html")
						.build(),
				CommonEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.malEpisodeId(2)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-2.html")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JUTSU_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JUTSU_URL + "/episode-1.html"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JUTSU_URL + "/episode-2.html"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(String runtimeExpectedResponse) {
		List<CommonEpisode> commonEpisodes = getCommonEpisodes();
		doReturn(commonEpisodes).when(jutsuParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(runtimeExpectedResponse)));
	}
}