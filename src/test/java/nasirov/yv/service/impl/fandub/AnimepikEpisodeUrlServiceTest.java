package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_NUMBER;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_POSTFIX;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikPlayer;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class AnimepikEpisodeUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<AnimepikEpisode>> {

	private static final Integer RUNTIME_EPISODE_NUMBER = 2;

	private static final String RUNTIME_EPISODE_NAME = RUNTIME_EPISODE_NUMBER + ANIMEPIK_EPISODE_POSTFIX;

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
	protected List<AnimepikEpisode> getRuntimeExpectedResponse() {
		return Lists.newArrayList(buildAnimepikEpisode(ANIMEPIK_EPISODE_NUMBER, null), buildAnimepikEpisode(RUNTIME_EPISODE_NUMBER, null));
	}

	@Override
	protected void mockGetRuntimeResponse(List<AnimepikEpisode> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(AnimepikTitleEpisodes.builder()
				.animepikPlayer(AnimepikPlayer.builder()
						.episodes(runtimeExpectedResponse)
						.build())
				.build())).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getUrls()
								.get(FanDubSource.ANIMEPIK) + "api/v1/" + commonTitle.getUrl())));
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.ANIMEPIK;
	}

	@Override
	protected void mockParser(List<AnimepikEpisode> animepikEpisodes) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(animepikParser)
				.extractEpisodes(animepikEpisodes);
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(ANIMEPIK_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEPIK_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANIMEPIK_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(ANIMEPIK_EPISODE_NAME, ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL), episodeNameAndUrl);
	}

	private AnimepikEpisode buildAnimepikEpisode(Integer episodeNumber, String titleUrl) {
		return AnimepikEpisode.builder()
				.episodeNumber(episodeNumber)
				.titleUrl(titleUrl)
				.build();
	}
}