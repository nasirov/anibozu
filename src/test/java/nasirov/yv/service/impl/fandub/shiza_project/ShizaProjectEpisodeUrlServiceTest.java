package nasirov.yv.service.impl.fandub.shiza_project;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.SHIZA_PROJECT_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.ShizaProjectParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.shiza_project.ShizaProjectFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
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
public class ShizaProjectEpisodeUrlServiceTest {

	@Mock
	private TitlesServiceI titlesService;

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private ShizaProjectFeignClient shizaProjectFeignClient;

	@Mock
	private ShizaProjectParserI shizaProjectParser;

	@InjectMocks
	private ShizaProjectEpisodeUrlService shizaProjectEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getShizaProjectRegular())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = shizaProjectEpisodeUrlService.getEpisodeUrl(FanDubSource.SHIZAPROJECT, malTitle);
		//then
		assertEquals(SHIZA_PROJECT_URL + REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-1", actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getShizaProjectRegular())));
		String htmlWithTitleEpisodes = "foobar";
		mockGetTitleEpisodes(htmlWithTitleEpisodes);
		mockParser(htmlWithTitleEpisodes);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = shizaProjectEpisodeUrlService.getEpisodeUrl(FanDubSource.SHIZAPROJECT, malTitle);
		//then
		assertEquals(SHIZA_PROJECT_URL + REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-2", actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = shizaProjectEpisodeUrlService.getEpisodeUrl(FanDubSource.SHIZAPROJECT, malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getShizaProjectConcretized())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = shizaProjectEpisodeUrlService.getEpisodeUrl(FanDubSource.SHIZAPROJECT, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getShizaProjectRegular(),
				CommonTitleTestBuilder.getShizaProjectConcretized())));
		String htmlWithTitleEpisodes = "foobar";
		mockGetTitleEpisodes(htmlWithTitleEpisodes);
		mockParser(htmlWithTitleEpisodes);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = shizaProjectEpisodeUrlService.getEpisodeUrl(FanDubSource.SHIZAPROJECT, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.SHIZAPROJECT, SHIZA_PROJECT_URL)).when(fanDubProps)
				.getUrls();
	}

	private void mockParser(String htmlWithTitleEpisodes) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(shizaProjectParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(htmlWithTitleEpisodes)));
	}

	private void mockTitleService(Map<Integer, List<CommonTitle>> mappedTitlesByMalId) {
		doReturn(mappedTitlesByMalId).when(titlesService)
				.getTitles(FanDubSource.SHIZAPROJECT);
	}

	private void mockGetTitleEpisodes(String htmlWithTitleEpisodes) {
		doReturn(htmlWithTitleEpisodes).when(shizaProjectFeignClient)
				.getTitlePage(REGULAR_TITLE_SHIZA_PROJECT_URL);
	}

	private Map<Integer, List<CommonTitle>> getMappedTitlesByMalId(List<CommonTitle> commonTitles) {
		Map<Integer, List<CommonTitle>> map = new HashMap<>();
		map.put(REGULAR_TITLE_MAL_ID, commonTitles);
		return map;
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("01, video.sibnet.ru")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-1")
						.build(),
				FandubEpisode.builder()
						.name("02, video.sibnet.ru")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_SHIZA_PROJECT_URL + "#online-2")
						.build());
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}