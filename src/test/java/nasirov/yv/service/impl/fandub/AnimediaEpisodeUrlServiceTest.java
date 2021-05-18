package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEDIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.buildEpisodeUrl;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimediaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimediaEpisodeUrlServiceTest {

	private static final String RUNTIME_EPISODE_NAME = "Серия 2";

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	protected CommonProps commonProps;

	@Mock
	private HttpRequestServiceI httpRequestService;

	@Mock
	private HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Mock
	private AnimediaParserI animediaParser;

	@InjectMocks
	private AnimediaEpisodeNameAndUrlService animediaEpisodeUrlService;

	@Test
	public void shouldReturnNameAndUrlForAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimediaRegular(),
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null,
						ANIMEDIA_EPISODE_NAME),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						2,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2),
						null,
						ANIMEDIA_EPISODE_NAME)),
				REGULAR_TITLE_MAL_ID,
				1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		Pair<String, String> episodeNameAndUrl = animediaEpisodeUrlService.getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(Pair.of(ANIMEDIA_EPISODE_NAME, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/1"), episodeNameAndUrl);
	}

	@Test
	public void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimediaRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle,
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null, RUNTIME_EPISODE_NAME),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						2,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2),
						null,
						ANIMEDIA_EPISODE_NAME)),
				REGULAR_TITLE_MAL_ID,
				2);
		mockGetTitleEpisodesByPlaylist(getAnimediaEpisodes(), commonTitle);
		mockParser(getAnimediaEpisodesWithFilledTitleUrlField());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = animediaEpisodeUrlService.getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/3"), episodeNameAndUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		mockFandubTitleService(Collections.emptyList(), notFoundOnFandubMalId, 1);
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		Pair<String, String> episodeNameAndUrl = animediaEpisodeUrlService.getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Test
	public void shouldReturnNameAndUrlForNotAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimediaConcretized(),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						0,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0),
						null,
						RUNTIME_EPISODE_NAME),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						2,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2),
						null,
						ANIMEDIA_EPISODE_NAME)),
				REGULAR_TITLE_MAL_ID,
				2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = animediaEpisodeUrlService.getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Test
	public void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimediaRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle,
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null, RUNTIME_EPISODE_NAME),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL,
						2,
						buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2),
						null,
						ANIMEDIA_EPISODE_NAME)),
				REGULAR_TITLE_MAL_ID,
				3);
		List<AnimediaEpisode> animediaEpisodesStub = Collections.emptyList();
		mockGetTitleEpisodesByPlaylist(animediaEpisodesStub, commonTitle);
		mockParser(animediaEpisodesStub);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = animediaEpisodeUrlService.getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	private void mockParser(List<AnimediaEpisode> animediaEpisodes) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(animediaParser)
				.extractEpisodes(animediaEpisodes);
	}

	private void mockGetTitleEpisodesByPlaylist(List<AnimediaEpisode> episodes, CommonTitle commonTitle) {
		HttpRequestServiceDto<List<AnimediaEpisode>> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.animedia(commonTitle);
		doReturn(Mono.just(episodes)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	private void mockFandubTitleService(List<CommonTitle> commonTitles, int malId, int malEpisodeId) {
		HttpRequestServiceDto<List<CommonTitle>> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.fandubTitlesService(FanDubSource.ANIMEDIA, malId, malEpisodeId);
		doReturn(Mono.just(commonTitles)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	protected void mockCommonProps() {
		doReturn(Collections.singletonMap(FanDubSource.ANIMEDIA, true)).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.ANIMEDIA, ANIMEDIA_ONLINE_TV)).when(fanDubProps)
				.getUrls();
	}

	private List<AnimediaEpisode> getAnimediaEpisodes() {
		return Lists.newArrayList(buildAnimediaEpisode("s1e1", ANIMEDIA_EPISODE_NAME, null), buildAnimediaEpisode("s1e3", RUNTIME_EPISODE_NAME, null));
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

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(ANIMEDIA_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEDIA_URL + "/1/1")
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(3)
						.number("2")
						.url(REGULAR_TITLE_ANIMEDIA_URL + "/1/3")
						.build());
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.animeUrl("https://myanimelist.net/anime/" + animeId + "/name")
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}