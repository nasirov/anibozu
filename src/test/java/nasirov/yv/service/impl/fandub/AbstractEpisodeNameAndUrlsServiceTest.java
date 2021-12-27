package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

/**
 * @author Nasirov Yuriy
 */
public abstract class AbstractEpisodeNameAndUrlsServiceTest<RUNTIME_RESPONSE_TYPE> {

	@Mock
	protected FanDubProps fanDubProps;

	@Mock
	protected CommonProps commonProps;

	@Mock
	protected HttpRequestServiceI httpRequestService;

	@Mock
	protected HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	protected CommonTitle regularCommonTitle;

	protected CommonTitle concretizedCommonTitle;

	@BeforeEach
	void setUp() {
		regularCommonTitle = CommonTitleTestBuilder.buildRegularTitle(getFandubSource());
		concretizedCommonTitle = CommonTitleTestBuilder.buildConcretizedTitle(getFandubSource());
	}

	void shouldReturnNameAndUrlForAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getRegularCommonTitles())
				.block();
		//then
		checkNameAndUrlForAvailableEpisode(episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		RUNTIME_RESPONSE_TYPE runtimeExpectedResponse = getRuntimeExpectedResponse();
		mockGetRuntimeResponse(runtimeExpectedResponse, regularCommonTitle);
		mockParser(runtimeExpectedResponse);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getRegularCommonTitles())
				.block();
		//then
		checkNameAndUrlForAvailableEpisodeBuiltInRuntime(episodeNameAndUrl);
	}

	void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		//given
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, Collections.emptyList())
				.block();
		//then
		assertEquals(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForNotAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getConcretizedCommonTitles())
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		RUNTIME_RESPONSE_TYPE titlePageContent = getRuntimeExpectedResponse();
		mockGetRuntimeResponse(titlePageContent, regularCommonTitle);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle, getRegularAndConcretizedCommonTitles())
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected abstract RUNTIME_RESPONSE_TYPE getRuntimeExpectedResponse();

	protected abstract String getFandubUrl();

	protected abstract void mockGetRuntimeResponse(RUNTIME_RESPONSE_TYPE runtimeExpectedResponse, CommonTitle commonTitle);

	protected abstract EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService();

	protected abstract FanDubSource getFandubSource();

	protected abstract List<FandubEpisode> getFandubEpisodes();

	protected abstract void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl);

	protected abstract void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl);

	protected abstract void mockParser(RUNTIME_RESPONSE_TYPE runtimeExpectedResponse);

	void mockCommonProps() {
		doReturn(Collections.singletonMap(getFandubSource(), true)).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}

	void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(getFandubSource(), getFandubUrl())).when(fanDubProps)
				.getUrls();
	}

	protected List<CommonTitle> getRegularCommonTitles() {
		return Lists.newArrayList(regularCommonTitle);
	}

	protected List<CommonTitle> getConcretizedCommonTitles() {
		return Lists.newArrayList(concretizedCommonTitle);
	}

	protected List<CommonTitle> getRegularAndConcretizedCommonTitles() {
		return Lists.newArrayList(regularCommonTitle, concretizedCommonTitle);
	}

	protected MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.animeUrl("https://myanimelist.net/anime/" + animeId + "/name")
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}
