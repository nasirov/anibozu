package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
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
public class AnimepikEpisodeUrlServiceTest {

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	protected CommonProps commonProps;

	@Mock
	private HttpRequestServiceI httpRequestService;

	@Mock
	private HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Mock
	private AnimepikParserI animepikParser;

	@InjectMocks
	private AnimepikEpisodeUrlService animepikEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikRegular()), REGULAR_TITLE_MAL_ID, 1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle)
				.block();
		//then
		assertEquals(ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL, actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimepikRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle), REGULAR_TITLE_MAL_ID, 2);
		mockGetTitlePage(getAnimepikEpisodes(), commonTitle);
		mockParser(getAnimepikEpisodesWithFilledTitleUrlField());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle)
				.block();
		//then
		assertEquals(ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL, actualUrl);
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
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle)
				.block();
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikConcretized()), REGULAR_TITLE_MAL_ID, 2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle)
				.block();
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimepikRegular();
		mockFandubTitleService(Lists.newArrayList(commonTitle), REGULAR_TITLE_MAL_ID, 3);
		List<AnimepikEpisode> animepikEpisodesStub = Collections.emptyList();
		mockGetTitlePage(animepikEpisodesStub, commonTitle);
		mockParser(animepikEpisodesStub);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle)
				.block();
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	protected void mockCommonProps() {
		doReturn(true).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.ANIMEPIK, ANIMEPIK_URL)).when(fanDubProps)
				.getUrls();
	}

	private void mockParser(List<AnimepikEpisode> animepikEpisodes) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(animepikParser)
				.extractEpisodes(animepikEpisodes);
	}

	private void mockFandubTitleService(List<CommonTitle> commonTitles, int malId, int malEpisodeId) {
		HttpRequestServiceDto<List<CommonTitle>> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.fandubTitlesService(FanDubSource.ANIMEPIK, malId, malEpisodeId);
		doReturn(Mono.just(commonTitles)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	private void mockGetTitlePage(List<AnimepikEpisode> animepikEpisodes, CommonTitle commonTitle) {
		HttpRequestServiceDto<List<AnimepikEpisode>> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.animepik(commonTitle);
		doReturn(Mono.just(animepikEpisodes)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	private List<AnimepikEpisode> getAnimepikEpisodes() {
		return Lists.newArrayList(buildAnimepikEpisode("1 серия", null), buildAnimepikEpisode("2 серия", null));
	}

	private List<AnimepikEpisode> getAnimepikEpisodesWithFilledTitleUrlField() {
		return Lists.newArrayList(buildAnimepikEpisode("1 серия", REGULAR_TITLE_ANIMEPIK_URL),
				buildAnimepikEpisode("2 серия", REGULAR_TITLE_ANIMEPIK_URL));
	}

	private AnimepikEpisode buildAnimepikEpisode(String name, String titleUrl) {
		return AnimepikEpisode.builder()
				.name(name)
				.titleUrl(titleUrl)
				.build();
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 серия")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEPIK_URL)
						.build(),
				FandubEpisode.builder()
						.name("2 серия")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANIMEPIK_URL)
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