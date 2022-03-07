package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEDIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.buildEpisodeUrl;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class AnimediaEpisodeUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<List<AnimediaEpisode>> {

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
	protected List<AnimediaEpisode> getRuntimeExpectedResponse() {
		return getAnimediaEpisodesWithFilledTitleUrlField();
	}

	@Override
	protected void mockGetRuntimeResponse(List<AnimediaEpisode> runtimeExpectedResponse, CommonTitle commonTitle) {
		doReturn(Mono.just(runtimeExpectedResponse)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(fanDubProps.getUrls()
								.get(FanDubSource.ANIMEDIA) + "embeds/playlist-j.txt/" + commonTitle.getId()
								.getId() + "/" + commonTitle.getId()
								.getDataList())));
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.ANIMEDIA;
	}

	@Override
	protected List<CommonEpisode> getCommonEpisodes() {
		return Lists.newArrayList(CommonEpisode.builder()
						.name(ANIMEDIA_EPISODE_NAME)
						.malEpisodeId(1)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEDIA_URL + "/1/1")
						.build(),
				CommonEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.malEpisodeId(3)
						.id(3)
						.number("2")
						.url(REGULAR_TITLE_ANIMEDIA_URL + "/1/3")
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(ANIMEDIA_EPISODE_NAME, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/1"), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/3"), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(List<AnimediaEpisode> runtimeExpectedResponse) {
		List<CommonEpisode> commonEpisodes = getCommonEpisodes();
		doReturn(commonEpisodes).when(animediaParser)
				.extractEpisodes(runtimeExpectedResponse);
	}

	@Override
	protected List<CommonTitle> getRegularCommonTitles() {
		return Lists.newArrayList(CommonTitleTestBuilder.getAnimediaRegular(),
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), ANIMEDIA_EPISODE_NAME),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						2,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2), ANIMEDIA_EPISODE_NAME));
	}

	private List<AnimediaEpisode> getAnimediaEpisodesWithFilledTitleUrlField() {
		return Lists.newArrayList(buildAnimediaEpisode("s1e1", ANIMEDIA_EPISODE_NAME, REGULAR_TITLE_ANIMEDIA_URL),
				buildAnimediaEpisode("s1e3", RUNTIME_EPISODE_NAME, REGULAR_TITLE_ANIMEDIA_URL));
	}

	private AnimediaEpisode buildAnimediaEpisode(String id, String name, String titleUrl) {
		return AnimediaEpisode.builder()
				.id(id)
				.episodeName(name)
				.titleUrl(titleUrl)
				.build();
	}
}