package nasirov.yv.service.impl.fandub.sovet_romantica;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.SOVET_ROMANTICA_URL;
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
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.sovet_romantica.SovetRomanticaFeignClient;
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
public class SovetRomanticaEpisodeUrlServiceTest {

	@Mock
	private TitlesServiceI titlesService;

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private SovetRomanticaFeignClient sovetRomanticaFeignClient;

	@Mock
	private SovetRomanticaParserI sovetRomanticaParserI;

	@InjectMocks
	private SovetRomanticaEpisodeUrlService sovetRomanticaEpisodeUrlService;

	@Test
	public void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getSovetRomanticaRegular())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = sovetRomanticaEpisodeUrlService.getEpisodeUrl(FanDubSource.SOVETROMANTICA, malTitle);
		//then
		assertEquals(SOVET_ROMANTICA_URL + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles", actualUrl);
	}

	@Test
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getSovetRomanticaRegular())));
		String htmlWithTitleEpisodes = "foobar";
		mockGetTitleEpisodes(htmlWithTitleEpisodes);
		mockParser(htmlWithTitleEpisodes);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = sovetRomanticaEpisodeUrlService.getEpisodeUrl(FanDubSource.SOVETROMANTICA, malTitle);
		//then
		assertEquals(SOVET_ROMANTICA_URL + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles", actualUrl);
	}

	@Test
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = sovetRomanticaEpisodeUrlService.getEpisodeUrl(FanDubSource.SOVETROMANTICA, malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getSovetRomanticaConcretized())));
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = sovetRomanticaEpisodeUrlService.getEpisodeUrl(FanDubSource.SOVETROMANTICA, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	@Test
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockFandubUrlsMap();
		mockTitleService(getMappedTitlesByMalId(Lists.newArrayList(CommonTitleTestBuilder.getSovetRomanticaRegular(),
				CommonTitleTestBuilder.getSovetRomanticaConcretized())));
		String htmlWithTitleEpisodes = "foobar";
		mockGetTitleEpisodes(htmlWithTitleEpisodes);
		mockParser(htmlWithTitleEpisodes);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = sovetRomanticaEpisodeUrlService.getEpisodeUrl(FanDubSource.SOVETROMANTICA, malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	private void mockFandubUrlsMap() {
		doReturn(Maps.newHashMap(FanDubSource.SOVETROMANTICA, SOVET_ROMANTICA_URL)).when(fanDubProps)
				.getUrls();
	}

	private void mockParser(String htmlWithTitleEpisodes) {
		List<FandubEpisode> fandubEpisodes = getFandubEpisodes();
		doReturn(fandubEpisodes).when(sovetRomanticaParserI)
				.extractEpisodes(argThat(x -> x.text()
						.equals(htmlWithTitleEpisodes)));
	}

	private void mockTitleService(Map<Integer, List<CommonTitle>> mappedTitlesByMalId) {
		doReturn(mappedTitlesByMalId).when(titlesService)
				.getTitles(FanDubSource.SOVETROMANTICA);
	}

	private void mockGetTitleEpisodes(String htmlWithTitleEpisodes) {
		doReturn(htmlWithTitleEpisodes).when(sovetRomanticaFeignClient)
				.getTitlePage(REGULAR_TITLE_SOVET_ROMANTICA_URL);
	}

	private Map<Integer, List<CommonTitle>> getMappedTitlesByMalId(List<CommonTitle> commonTitles) {
		Map<Integer, List<CommonTitle>> map = new HashMap<>();
		map.put(REGULAR_TITLE_MAL_ID, commonTitles);
		return map;
	}

	private List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("Эпизод 1")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles")
						.build(),
				FandubEpisode.builder()
						.name("Эпизод 2")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_2-subtitles")
						.build());
	}

	private MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}