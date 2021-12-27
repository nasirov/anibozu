package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANYTHING_GROUP_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveAnythingGroupServiceI;
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
class AnythingGroupEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<String> {

	private static final String RUNTIME_EPISODE_NAME = "02. Серия | Baz";

	@Mock
	private ReactiveAnythingGroupServiceI reactiveAnythingGroupService;

	@InjectMocks
	private AnythingGroupEpisodeNameAndUrlService anythingGroupEpisodeNameAndUrlService;

	@Test
	@Override
	void shouldReturnNameAndUrlForAvailableEpisode() {
		super.shouldReturnNameAndUrlForAvailableEpisode();
	}

	@Test
	@Override
	void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockReactiveAnythingGroupService();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getRegularCommonTitles())
				.block();
		//then
		checkNameAndUrlForAvailableEpisodeBuiltInRuntime(episodeNameAndUrl);
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
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockReactiveAnythingGroupService();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getRegularAndConcretizedCommonTitles())
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Override
	protected String getRuntimeExpectedResponse() {
		return "foobar";
	}

	@Override
	protected String getFandubUrl() {
		return ANYTHING_GROUP_URL;
	}

	@Override
	protected void mockGetRuntimeResponse(String runtimeExpectedResponse, CommonTitle commonTitle) {
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return anythingGroupEpisodeNameAndUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.ANYTHING_GROUP;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(ANYTHING_GROUP_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANYTHING_GROUP_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANYTHING_GROUP_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(ANYTHING_GROUP_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_ANYTHING_GROUP_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_ANYTHING_GROUP_URL), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(String runtimeExpectedResponse) {
	}

	private void mockReactiveAnythingGroupService() {
		doReturn(Mono.just(getFandubEpisodes())).when(reactiveAnythingGroupService)
				.getTitleEpisodes(regularCommonTitle.getUrl());
	}
}