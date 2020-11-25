package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub_titles_service.FandubTitlesServiceFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.assertj.core.util.Maps;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.mockito.Mock;

/**
 * Created by nasirov.yv
 */
public abstract class BaseEpisodeUrlsServiceTest {

	protected static final String BASIC_AUTH = "Basic foobar";

	@Mock
	protected FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient;

	@Mock
	protected AuthProps authProps;

	@Mock
	protected FanDubProps fanDubProps;

	private CommonTitle regularCommonTitle;

	private CommonTitle concretizedCommonTitle;

	@Before
	public void setUp() {
		List<CommonTitle> commonTitles = CommonTitleTestBuilder.buildCommonTitles(getFandubSource());
		regularCommonTitle = commonTitles.get(0);
		concretizedCommonTitle = commonTitles.get(1);
	}

	protected void shouldReturnUrlWithAvailableEpisode() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(getRegularCommonTitles(), 1);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 0);
		//when
		String actualUrl = getEpisodeUrlService().getEpisodeUrl(getFandubSource(), malTitle);
		//then
		checkUrlWithAvailableEpisode(actualUrl);
	}

	protected void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(getRegularCommonTitles(), 2);
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = getEpisodeUrlService().getEpisodeUrl(getFandubSource(), malTitle);
		//then
		checkUrlWithAvailableEpisodeInRuntime(actualUrl);
	}

	protected void shouldReturnNotFoundOnFandubSiteUrl() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		int notFoundOnFandubMalId = 42;
		MalTitle malTitle = buildWatchingTitle(notFoundOnFandubMalId, 0);
		//when
		String actualUrl = getEpisodeUrlService().getEpisodeUrl(getFandubSource(), malTitle);
		//then
		assertEquals(NOT_FOUND_ON_FANDUB_SITE_URL, actualUrl);
	}

	protected void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(getConcretizedCommonTitles(), 2);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		String actualUrl = getEpisodeUrlService().getEpisodeUrl(getFandubSource(), malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	protected void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		//given
		mockAuthProps();
		mockFandubUrlsMap();
		mockTitleService(getRegularAndConcretizedCommonTitles(), 3);
		String titlePageContent = "foobar";
		mockGetTitlePage(titlePageContent);
		mockParser(titlePageContent);
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		String actualUrl = getEpisodeUrlService().getEpisodeUrl(getFandubSource(), malTitle);
		//then
		assertEquals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, actualUrl);
	}

	protected abstract String getFandubUrl();

	protected abstract EpisodesExtractorI<Document> getParser();

	protected abstract void mockGetTitlePage(String titlePageContent);

	protected abstract EpisodeUrlServiceI getEpisodeUrlService();

	protected abstract FanDubSource getFandubSource();

	protected abstract List<FandubEpisode> getFandubEpisodes();

	protected abstract void checkUrlWithAvailableEpisode(String actualUrl);

	protected abstract void checkUrlWithAvailableEpisodeInRuntime(String actualUrl);

	protected void mockAuthProps() {
		doReturn(BASIC_AUTH).when(authProps)
				.getFandubTitlesServiceBasicAuth();
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

	protected void mockTitleService(List<CommonTitle> commonTitles, int malEpisodeId) {
		doReturn(commonTitles).when(fandubTitlesServiceFeignClient)
				.getCommonTitles(BASIC_AUTH, getFandubSource(), REGULAR_TITLE_MAL_ID, malEpisodeId);
	}

	protected MalTitle buildWatchingTitle(int animeId, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(animeId)
				.animeUrl("https://myanimelist.net/anime/" + animeId + "/name")
				.numWatchedEpisodes(numWatchedEpisodes)
				.build();
	}
}
