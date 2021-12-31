package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.JAM_CLUB_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JAM_CLUB_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class JamClubEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<FandubEpisode>> {

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
	protected List<FandubEpisode> getRuntimeExpectedResponse() {
		return getFandubEpisodes();
	}

	@Override
	protected void mockGetRuntimeResponse(List<FandubEpisode> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(runtimeExpectedResponse)).when(reactiveJamClubService)
				.getTitleEpisodes(commonTitle.getUrl(), commonTitle.getId());
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JAMCLUB;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(JAM_CLUB_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JAM_CLUB_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JAM_CLUB_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JAM_CLUB_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JAM_CLUB_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JAM_CLUB_URL), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(List<FandubEpisode> runtimeExpectedResponse) {
	}
}