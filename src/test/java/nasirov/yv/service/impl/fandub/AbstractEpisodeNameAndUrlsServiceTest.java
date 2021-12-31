package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Nasirov Yuriy
 */
public abstract class AbstractEpisodeNameAndUrlsServiceTest<RUNTIME_RESPONSE_TYPE> extends AbstractTest {

	private CommonTitle regularCommonTitle;

	private CommonTitle concretizedCommonTitle;

	@BeforeEach
	void setUp() {
		regularCommonTitle = CommonTitleTestBuilder.buildRegularTitle(getFandubSource());
		concretizedCommonTitle = CommonTitleTestBuilder.buildConcretizedTitle(getFandubSource());
	}

	void shouldReturnNameAndUrlForAvailableEpisode() {
		//given
		MalTitle malTitle = buildRegularTitle(0);
		//when
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlServiceStrategy.get(getFandubSource())
				.getEpisodeNameAndUrl(malTitle, getRegularCommonTitles())
				.block();
		//then
		checkNameAndUrlForAvailableEpisode(episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		enableBuildUrlInRuntime();
		RUNTIME_RESPONSE_TYPE runtimeExpectedResponse = getRuntimeExpectedResponse();
		mockGetRuntimeResponse(runtimeExpectedResponse, regularCommonTitle);
		mockParser(runtimeExpectedResponse);
		MalTitle malTitle = buildRegularTitle(1);
		//when
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlServiceStrategy.get(getFandubSource())
				.getEpisodeNameAndUrl(malTitle, getRegularCommonTitles())
				.block();
		//then
		checkNameAndUrlForAvailableEpisodeBuiltInRuntime(episodeNameAndUrl);
	}

	void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		//given
		MalTitle malTitle = buildNotFoundOnFandubTitle();
		//when
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlServiceStrategy.get(getFandubSource())
				.getEpisodeNameAndUrl(malTitle, Collections.emptyList())
				.block();
		//then
		assertEquals(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForNotAvailableEpisode() {
		//given
		enableBuildUrlInRuntime();
		MalTitle malTitle = buildRegularTitle(1);
		//when
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlServiceStrategy.get(getFandubSource())
				.getEpisodeNameAndUrl(malTitle, getConcretizedCommonTitles())
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		enableBuildUrlInRuntime();
		RUNTIME_RESPONSE_TYPE titlePageContent = getRuntimeExpectedResponse();
		mockGetRuntimeResponse(titlePageContent, regularCommonTitle);
		MalTitle malTitle = buildRegularTitle(2);
		//when
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlServiceStrategy.get(getFandubSource())
				.getEpisodeNameAndUrl(malTitle, getRegularAndConcretizedCommonTitles())
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	protected abstract RUNTIME_RESPONSE_TYPE getRuntimeExpectedResponse();

	protected abstract void mockGetRuntimeResponse(RUNTIME_RESPONSE_TYPE runtimeExpectedResponse, CommonTitle commonTitle);

	protected abstract FanDubSource getFandubSource();

	protected abstract List<FandubEpisode> getFandubEpisodes();

	protected abstract void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl);

	protected abstract void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl);

	protected abstract void mockParser(RUNTIME_RESPONSE_TYPE runtimeExpectedResponse);

	protected List<CommonTitle> getRegularCommonTitles() {
		return Lists.newArrayList(regularCommonTitle);
	}

	protected List<CommonTitle> getConcretizedCommonTitles() {
		return Lists.newArrayList(concretizedCommonTitle);
	}

	protected List<CommonTitle> getRegularAndConcretizedCommonTitles() {
		return Lists.newArrayList(regularCommonTitle, concretizedCommonTitle);
	}

	private void enableBuildUrlInRuntime() {
		doReturn(Collections.singletonMap(getFandubSource(), true)).when(commonProps)
				.getEnableBuildUrlInRuntime();
	}
}
