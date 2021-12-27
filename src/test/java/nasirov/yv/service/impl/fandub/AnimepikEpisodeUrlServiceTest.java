package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_NUMBER;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_POSTFIX;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikPlayer;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Maps;
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
class AnimepikEpisodeUrlServiceTest {

	private static final Integer RUNTIME_EPISODE_NUMBER = 2;

	private static final String RUNTIME_EPISODE_NAME = RUNTIME_EPISODE_NUMBER + ANIMEPIK_EPISODE_POSTFIX;

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
	private AnimepikEpisodeNameAndUrlService animepikEpisodeUrlService;

	@Test
	void shouldReturnNameAndUrlForAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		List<CommonTitle> commonTitles = Lists.newArrayList(CommonTitleTestBuilder.getAnimepikRegular());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		Pair<String, String> episodeNameAndUrl = animepikEpisodeUrlService.getEpisodeNameAndUrl(malTitle, commonTitles)
				.block();
		//then
		assertEquals(Pair.of(ANIMEPIK_EPISODE_NAME, ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL), episodeNameAndUrl);
	}

	@Test
	void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimepikRegular();
		List<CommonTitle> commonTitles = Lists.newArrayList(commonTitle);
		mockGetTitlePage(getAnimepikEpisodes(), commonTitle);
		mockParser(getAnimepikEpisodesWithFilledTitleUrlField());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = animepikEpisodeUrlService.getEpisodeNameAndUrl(malTitle, commonTitles)
				.block();
		//then
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL), episodeNameAndUrl);
	}

	@Test
	void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		//given
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		Pair<String, String> episodeNameAndUrl = animepikEpisodeUrlService.getEpisodeNameAndUrl(malTitle, Collections.emptyList())
				.block();
		//then
		assertEquals(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Test
	void shouldReturnNameAndUrlForNotAvailableEpisode() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		List<CommonTitle> commonTitles = Lists.newArrayList(CommonTitleTestBuilder.getAnimepikConcretized());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = animepikEpisodeUrlService.getEpisodeNameAndUrl(malTitle, commonTitles)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Test
	void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		CommonTitle commonTitle = CommonTitleTestBuilder.getAnimepikRegular();
		List<CommonTitle> commonTitles = Lists.newArrayList(commonTitle);
		List<AnimepikEpisode> animepikEpisodesStub = Collections.emptyList();
		mockGetTitlePage(animepikEpisodesStub, commonTitle);
		mockParser(animepikEpisodesStub);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = animepikEpisodeUrlService.getEpisodeNameAndUrl(malTitle, commonTitles)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected void mockCommonProps() {
		doReturn(Collections.singletonMap(FanDubSource.ANIMEPIK, true)).when(commonProps)
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

	private void mockGetTitlePage(List<AnimepikEpisode> animepikEpisodes, CommonTitle commonTitle) {
		HttpRequestServiceDto<AnimepikTitleEpisodes> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.animepik(commonTitle);
		doReturn(Mono.just(AnimepikTitleEpisodes.builder()
				.animepikPlayer(AnimepikPlayer.builder()
						.episodes(animepikEpisodes)
						.build())
				.build())).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	private List<AnimepikEpisode> getAnimepikEpisodes() {
		return Lists.newArrayList(buildAnimepikEpisode(ANIMEPIK_EPISODE_NUMBER, null), buildAnimepikEpisode(RUNTIME_EPISODE_NUMBER, null));
	}

	private List<AnimepikEpisode> getAnimepikEpisodesWithFilledTitleUrlField() {
		return Lists.newArrayList(buildAnimepikEpisode(ANIMEPIK_EPISODE_NUMBER, REGULAR_TITLE_ANIMEPIK_URL),
				buildAnimepikEpisode(RUNTIME_EPISODE_NUMBER, REGULAR_TITLE_ANIMEPIK_URL));
	}

	private AnimepikEpisode buildAnimepikEpisode(Integer episodeNumber, String titleUrl) {
		return AnimepikEpisode.builder()
				.episodeNumber(episodeNumber)
				.titleUrl(titleUrl)
				.build();
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(ANIMEPIK_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANIMEPIK_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
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