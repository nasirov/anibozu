package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Maps;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public abstract class AbstractEpisodeNameAndUrlsServiceTest {

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
	public void setUp() {
		List<CommonTitle> commonTitles = CommonTitleTestBuilder.buildCommonTitles(getFandubSource());
		regularCommonTitle = commonTitles.get(0);
		concretizedCommonTitle = commonTitles.get(1);
	}

	protected void shouldReturnNameAndUrlForAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		mockFandubTitleService(getRegularCommonTitles(), REGULAR_TITLE_MAL_ID, 1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		checkNameAndUrlForAvailableEpisode(episodeNameAndUrl);
	}

	protected void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(getRegularCommonTitles(), REGULAR_TITLE_MAL_ID, 2);
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent, regularCommonTitle);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		checkNameAndUrlForAvailableEpisodeBuiltInRuntime(episodeNameAndUrl);
	}

	protected void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		//given
		int notFoundOnFandubMalId = 42;
		mockFandubTitleService(Collections.emptyList(), notFoundOnFandubMalId, 1);
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected void shouldReturnNameAndUrlForNotAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(getConcretizedCommonTitles(), REGULAR_TITLE_MAL_ID, 2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(getRegularAndConcretizedCommonTitles(), REGULAR_TITLE_MAL_ID, 3);
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent, regularCommonTitle);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected abstract String getFandubUrl();

	protected abstract EpisodesExtractorI<Document> getParser();

	protected abstract void mockGetTitlePage(String titlePageContent, CommonTitle commonTitle);

	protected abstract EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService();

	protected abstract FanDubSource getFandubSource();

	protected abstract List<FandubEpisode> getFandubEpisodes();

	protected abstract void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl);

	protected abstract void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl);

	protected void mockCommonProps() {
		doReturn(Collections.singletonMap(getFandubSource(), true)).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}

	protected void mockFandubUrlsMap() {
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

	protected void mockParser(String titlePage) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(getParser())
				.extractEpisodes(argThat(x -> x.text()
						.equals(titlePage)));
	}

	protected void mockFandubTitleService(List<CommonTitle> commonTitles, int malId, int malEpisodeId) {
		HttpRequestServiceDto<List<CommonTitle>> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.fandubTitlesService(getFandubSource(), malId, malEpisodeId);
		doReturn(Mono.just(commonTitles)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	protected MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.animeUrl("https://myanimelist.net/anime/" + animeId + "/name")
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}
