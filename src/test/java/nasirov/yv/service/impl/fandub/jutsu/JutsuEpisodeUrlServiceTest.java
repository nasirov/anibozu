package nasirov.yv.service.impl.fandub.jutsu;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JutsuParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.jutsu.JutsuFeignClient;
import nasirov.yv.service.TitlesServiceI;
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
public class JutsuEpisodeUrlServiceTest {

	@Mock
	private TitlesServiceI titlesService;

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private JutsuFeignClient jutsuFeignClient;

	@Mock
	private JutsuParserI jutsuParser;

	@InjectMocks
	private JutsuEpisodeUrlService jutsuEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getJutsuRegular())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = jutsuEpisodeUrlService.getEpisodeUrl(FanDubSource.JUTSU, malTitle);
		//then
		assertEquals(JUTSU_URL + REGULAR_TITLE_JUTSU_URL, actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getJutsuRegular())));
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = jutsuEpisodeUrlService.getEpisodeUrl(FanDubSource.JUTSU, malTitle);
		//then
		assertEquals(JUTSU_URL + REGULAR_TITLE_JUTSU_URL + "/episode-2.html", actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = jutsuEpisodeUrlService.getEpisodeUrl(FanDubSource.JUTSU, malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getJutsuConcretized())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = jutsuEpisodeUrlService.getEpisodeUrl(FanDubSource.JUTSU, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getJutsuRegular(),
				CommonTitleTestBuilder.getJutsuConcretized())));
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = jutsuEpisodeUrlService.getEpisodeUrl(FanDubSource.JUTSU, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.JUTSU, JUTSU_URL)).when(fanDubProps)
				.getUrls();
	}
	private void mockParser(String titlePage) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(jutsuParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(titlePage)));
	}

	private void mockTitleService(Map<Integer, List<CommonTitle>> mappedTitlesByMalId) {
		doReturn(mappedTitlesByMalId).when(titlesService)
				.getTitles(FanDubSource.JUTSU);
	}

	private void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(jutsuFeignClient)
				.getTitlePage(REGULAR_TITLE_JUTSU_URL);
	}

	private Map<Integer, List<CommonTitle>> getMappedTitlesByMalId(List<CommonTitle> commonTitles) {
		Map<Integer, List<CommonTitle>> map = new HashMap<>();
		map.put(REGULAR_TITLE_MAL_ID, commonTitles);
		return map;
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 серия")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-1.html")
						.build(),
				FandubEpisode.builder()
						.name("2 серия")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JUTSU_URL + "/episode-2.html")
						.build());
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}