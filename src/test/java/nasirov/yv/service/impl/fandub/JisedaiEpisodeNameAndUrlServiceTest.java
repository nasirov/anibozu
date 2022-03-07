package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.JISEDAI_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.jisedai.JisedaiTitleEpisodeDto;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class JisedaiEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<JisedaiTitleEpisodeDto>> {

	private static final String RUNTIME_EPISODE_NAME = "Серия 2";

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
	protected List<JisedaiTitleEpisodeDto> getRuntimeExpectedResponse() {
		return Lists.newArrayList(JisedaiTitleEpisodeDto.builder()
						.episodeNumber(1)
						.build(),
				JisedaiTitleEpisodeDto.builder()
						.episodeNumber(2)
						.build());
	}

	@Override
	protected void mockGetRuntimeResponse(List<JisedaiTitleEpisodeDto> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(runtimeExpectedResponse)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getJisedaiApiUrl() + "api/v1/anime/" + commonTitle.getId()
								.getId() + "/episode")));
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JISEDAI;
	}

	@Override
	protected List<CommonEpisode> getCommonEpisodes() {
		return Lists.newArrayList(CommonEpisode.builder()
						.name(JISEDAI_EPISODE_NAME)
						.malEpisodeId(1)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build(),
				CommonEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.malEpisodeId(2)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JISEDAI_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JISEDAI_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME,
				fanDubProps.getUrls()
						.get(getFandubSource()) + REGULAR_TITLE_JISEDAI_URL), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(List<JisedaiTitleEpisodeDto> runtimeExpectedResponse) {
		List<CommonEpisode> commonEpisodes = getCommonEpisodes();
		doReturn(commonEpisodes).when(jisedaiParser)
				.extractEpisodes(runtimeExpectedResponse);
	}
}