package nasirov.yv.service.impl.fandub.animepik;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animepik.AnimepikResourcesFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub_titles_service.FandubTitlesServiceFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimepikEpisodeUrlServiceTest {

	private static final String BASIC_AUTH = "Basic foobar";

	@Mock
	private FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient;

	@Mock
	private AuthProps authProps;

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private AnimepikResourcesFeignClient animepikResourcesFeignClient;

	@Mock
	private AnimepikParserI animepikParser;

	@InjectMocks
	private AnimepikEpisodeUrlService animepikEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikRegular()), 1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle);
		//then
		assertEquals(ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL, actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikRegular()), 2);
		mockGetTitlePage(getAnimepikEpisodes());
		mockParser(getAnimepikEpisodesWithFilledTitleUrlField());
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle);
		//then
		assertEquals(ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikConcretized()), 2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(Lists.newArrayList(CommonTitleTestBuilder.getAnimepikRegular()), 3);
		List<AnimepikEpisode> animepikEpisodesStub = Collections.emptyList();
		mockGetTitlePage(animepikEpisodesStub);
		mockParser(animepikEpisodesStub);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = animepikEpisodeUrlService.getEpisodeUrl(FanDubSource.ANIMEPIK, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockAuthProps() {
		doReturn(BASIC_AUTH).when(authProps)
				.getFandubTitlesServiceBasicAuth();
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

	private void mockTitleService(List<CommonTitle> commonTitles, int malEpisodeId) {
		doReturn(commonTitles).when(fandubTitlesServiceFeignClient)
				.getCommonTitles(BASIC_AUTH, FanDubSource.ANIMEPIK, REGULAR_TITLE_MAL_ID, malEpisodeId);
	}

	private void mockGetTitlePage(List<AnimepikEpisode> animepikEpisodes) {
		doReturn(animepikEpisodes).when(animepikResourcesFeignClient)
				.getTitleEpisodes(REGULAR_TITLE_ID);
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