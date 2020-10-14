package nasirov.yv.service.impl.fandub.anilibria;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnilibriaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anilibria.AnilibriaFeignClient;
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
public class AnilibriaEpisodeUrlServiceTest {

	@Mock
	private TitlesServiceI titlesService;

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private AnilibriaFeignClient anilibriaFeignClient;

	@Mock
	private AnilibriaParserI anilibriaParser;

	@InjectMocks
	private AnilibriaEpisodeUrlService anilibriaEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getAnilibriaRegular())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANILIBRIA, malTitle);
		//then
		assertEquals(ANILIBRIA_URL + REGULAR_TITLE_ANILIBRIA_URL, actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getAnilibriaRegular())));
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANILIBRIA, malTitle);
		//then
		assertEquals(ANILIBRIA_URL + REGULAR_TITLE_ANILIBRIA_URL, actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANILIBRIA, malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getAnilibriaConcretized())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANILIBRIA, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getAnilibriaRegular())));
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = anilibriaEpisodeUrlService.getEpisodeUrl(FanDubSource.ANILIBRIA, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.ANILIBRIA, ANILIBRIA_URL)).when(fanDubProps)
				.getUrls();
	}
	private void mockParser(String titlePage) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(anilibriaParser)
				.extractEpisodes(argThat(x -> x.text()
						.equals(titlePage)));
	}

	private void mockTitleService(Map<Integer, List<CommonTitle>> mappedTitlesByMalId) {
		doReturn(mappedTitlesByMalId).when(titlesService)
				.getTitles(FanDubSource.ANILIBRIA);
	}

	private void mockGetTitlePage(String titlePageContent) {
		doReturn(titlePageContent).when(anilibriaFeignClient)
				.getTitlePage(REGULAR_TITLE_ANILIBRIA_URL);
	}

	private Map<Integer, List<CommonTitle>> getMappedTitlesByMalId(ArrayList<CommonTitle> commonTitles) {
		Map<Integer, List<CommonTitle>> map = new HashMap<>();
		map.put(REGULAR_TITLE_MAL_ID, commonTitles);
		return map;
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 эпизод")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_ANILIBRIA_URL)
						.build(),
				FandubEpisode.builder()
						.name("2 эпизод")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_ANILIBRIA_URL)
						.build());
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}