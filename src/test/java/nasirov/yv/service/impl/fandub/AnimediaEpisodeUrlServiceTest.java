package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
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
	private AnimediaEpisodeUrlService animediaEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimediaRegular(),
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL, 2, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2), null)),
				REGULAR_TITLE_MAL_ID,
				1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEDIA, malTitle)
				.block();
		//then
		assertEquals(ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/1", actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimediaRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle,
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL, 2, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2), null)),
				REGULAR_TITLE_MAL_ID,
				2);
		mockGetTitleEpisodesByPlaylist(getAnimediaEpisodes(), commonTitle);
		mockParser(getAnimediaEpisodesWithFilledTitleUrlField());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEDIA, malTitle)
				.block();
		//then
		assertEquals(ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/3", actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		mockFandubTitleService(Collections.emptyList(), notFoundOnFandubMalId, 1);
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEDIA, malTitle)
				.block();
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimediaConcretized(),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL, 2, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2), null)),
				REGULAR_TITLE_MAL_ID,
				2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEDIA, malTitle)
				.block();
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimediaRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle,
				CommonTitleTestBuilder.getRegular(REGULAR_TITLE_ANIMEDIA_URL, 0, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 0), null),
				CommonTitleTestBuilder.getConcretized(REGULAR_TITLE_ANIMEDIA_URL, 2, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 2), null)),
				REGULAR_TITLE_MAL_ID,
				3);
		List<AnimediaEpisode> animediaEpisodesStub = Collections.emptyList();
		mockGetTitleEpisodesByPlaylist(animediaEpisodesStub, commonTitle);
		mockParser(animediaEpisodesStub);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = animediaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEDIA, malTitle)
				.block();
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
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
		doReturn(true).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.ANIMEDIA, ANIMEDIA_ONLINE_TV)).when(fanDubProps)
				.getUrls();
	}

	private List<AnimediaEpisode> getAnimediaEpisodes() {
		return Lists.newArrayList(buildAnimediaEpisode("s1e1", "Серия 1", null), buildAnimediaEpisode("s1e3", "Серия 2", null));
	}

	private List<AnimediaEpisode> getAnimediaEpisodesWithFilledTitleUrlField() {
		return Lists.newArrayList(buildAnimediaEpisode("s1e1", "Серия 1", REGULAR_TITLE_ANIMEDIA_URL),
				buildAnimediaEpisode("s1e3", "Серия 2", REGULAR_TITLE_ANIMEDIA_URL));
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
						.name("Серия 1")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEDIA_URL + "/1/1")
						.build(),
				FandubEpisode.builder()
						.name("Серия 2")
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