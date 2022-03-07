package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.NINE_ANIME_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class NineAnimeEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<CommonEpisode>> {

	private static final String RUNTIME_EPISODE_NAME = "2";

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
	protected List<CommonEpisode> getRuntimeExpectedResponse() {
		return getCommonEpisodes();
	}

	@Override
	protected void mockGetRuntimeResponse(List<CommonEpisode> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(runtimeExpectedResponse)).when(reactiveNineAnimeService)
				.getTitleEpisodes(commonTitle.getId()
						.getId());
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.NINEANIME;
	}

	@Override
	protected List<CommonEpisode> getCommonEpisodes() {
		return Lists.newArrayList(CommonEpisode.builder()
						.name(NINE_ANIME_EPISODE_NAME)
						.malEpisodeId(1)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-1")
						.build(),
				CommonEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.malEpisodeId(2)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-2")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(NINE_ANIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_NINE_ANIME_URL + "/ep-2"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(List<CommonEpisode> runtimeExpectedResponse) {
	}
}