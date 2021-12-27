package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.NINE_ANIME_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveNineAnimeServiceI;
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
class NineAnimeEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<FandubEpisode>> {

	private static final String RUNTIME_EPISODE_NAME = "2";

	@Mock
	private ReactiveNineAnimeServiceI reactiveNineAnimeService;

	@InjectMocks
	private NineAnimeEpisodeNameAndUrlService nineAnimeEpisodeUrlService;

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
	protected String getFandubUrl() {
		return NINE_ANIME_TO;
	}

	@Override
	protected void mockGetRuntimeResponse(List<FandubEpisode> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(runtimeExpectedResponse)).when(reactiveNineAnimeService)
				.getTitleEpisodes(commonTitle.getId());
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return nineAnimeEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.NINEANIME;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(NINE_ANIME_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-1")
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_NINE_ANIME_URL + "/ep-2")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(NINE_ANIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_NINE_ANIME_URL + "/ep-2"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(List<FandubEpisode> runtimeExpectedResponse) {
	}
}