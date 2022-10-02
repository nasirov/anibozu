package nasirov.yv.ac.service.impl;

import static nasirov.yv.ac.utils.CommonTitleTestFactory.ANILIBRIA_EPISODE_NAME;
import static nasirov.yv.ac.utils.CommonTitleTestFactory.REGULAR_TITLE_ANILIBRIA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import nasirov.yv.ac.AbstractTest;
import nasirov.yv.ac.data.constants.BaseConstants;
import nasirov.yv.ac.data.front.InputDto;
import nasirov.yv.ac.data.front.ResultDto;
import nasirov.yv.ac.data.front.TitleDto;
import nasirov.yv.ac.data.front.TitleType;
import nasirov.yv.ac.util.MalUtils;
import nasirov.yv.ac.utils.CommonTitleTestFactory;
import nasirov.yv.ac.utils.MalTitleTestFactory;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class ResultProcessingServiceTest extends AbstractTest {

	@Test
	void shouldReturnAllKindsOfTitles() {
		//given
		InputDto inputDto = buildInputDto();
		Set<FandubSource> fandubSources = getEnabledFandubSources();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalTitle concretizedTitle = MalTitleTestFactory.buildConcretizedMalTitle();
		MalTitle notFoundOnFandubTitle = MalTitleTestFactory.buildNotFoundOnFandubMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(
				Lists.newArrayList(regularTitle, concretizedTitle, notFoundOnFandubTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<Integer, Map<FandubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), CommonTitleTestFactory.buildRegularCommonTitles(fandubSources));
		orderedMalTitles.put(concretizedTitle.getId(), CommonTitleTestFactory.buildConcretizedCommonTitles(fandubSources));
		orderedMalTitles.put(notFoundOnFandubTitle.getId(),
				CommonTitleTestFactory.buildNotFoundOnFandubCommonTitles(fandubSources));
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		waitForCacheThread();
		//then
		assertNotNull(result);
		assertTrue(result.getErrorMessage().isEmpty());
		assertEquals(getEnabledFandubSources(), result.getFandubSources());
		List<TitleDto> titles = result.getTitles();
		assertEquals(3, titles.size());
		checkTitle(titles.get(0), TitleType.AVAILABLE, regularTitle);
		checkTitle(titles.get(1), TitleType.NOT_AVAILABLE, concretizedTitle);
		checkTitle(titles.get(2), TitleType.NOT_AVAILABLE, notFoundOnFandubTitle);
		ResultDto cachedResult = resultCache.get(cacheKey, ResultDto.class);
		assertEquals(result, cachedResult);
		assertEquals(cachedResult, resultProcessingService.getResult(inputDto).block());
		verify(httpRequestService).performHttpRequest(argThat(x -> x.getUrl()
				.equals(
						starterCommonProperties.getExternalServices().getMalServiceUrl() + "titles?username=" + MAL_USERNAME +
								"&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}

	@Test
	void shouldReturnAllKindsOfTitlesCacheFails() {
		//given
		InputDto inputDto = buildInputDto();
		Set<FandubSource> fandubSources = getEnabledFandubSources();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalTitle concretizedTitle = MalTitleTestFactory.buildConcretizedMalTitle();
		MalTitle notFoundOnFandubTitle = MalTitleTestFactory.buildNotFoundOnFandubMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(
				Lists.newArrayList(regularTitle, concretizedTitle, notFoundOnFandubTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<Integer, Map<FandubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), CommonTitleTestFactory.buildRegularCommonTitles(fandubSources));
		orderedMalTitles.put(concretizedTitle.getId(), CommonTitleTestFactory.buildConcretizedCommonTitles(fandubSources));
		orderedMalTitles.put(notFoundOnFandubTitle.getId(),
				CommonTitleTestFactory.buildNotFoundOnFandubCommonTitles(fandubSources));
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		Cache spiedCache = spy(resultCache);
		doReturn(spiedCache).when(cacheManager).getCache(cachesNames.getResultCache());
		doThrow(new RuntimeException("fail on get")).when(spiedCache).get(cacheKey, ResultDto.class);
		doThrow(new RuntimeException("fail on put")).when(spiedCache).put(eq(cacheKey), any(ResultDto.class));
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		waitForCacheThread();
		//then
		assertNotNull(result);
		assertTrue(result.getErrorMessage().isEmpty());
		assertEquals(getEnabledFandubSources(), result.getFandubSources());
		List<TitleDto> titles = result.getTitles();
		assertEquals(3, titles.size());
		checkTitle(titles.get(0), TitleType.AVAILABLE, regularTitle);
		checkTitle(titles.get(1), TitleType.NOT_AVAILABLE, concretizedTitle);
		checkTitle(titles.get(2), TitleType.NOT_AVAILABLE, notFoundOnFandubTitle);
		assertEquals(result, resultProcessingService.getResult(inputDto).block());
		verify(httpRequestService, times(2)).performHttpRequest(argThat(x -> x.getUrl()
				.equals(
						starterCommonProperties.getExternalServices().getMalServiceUrl() + "titles?username=" + MAL_USERNAME +
								"&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}

	@Test
	void shouldReturnResponseWithMalServiceErrorMessage() {
		//given
		InputDto inputDto = buildInputDto();
		String errorMessage = "Not found watching titles for " + MAL_USERNAME + " !";
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(), errorMessage);
		mockExternalMalServiceResponse(malServiceResponseDto);
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		waitForCacheThread();
		//then
		assertNotNull(result);
		assertEquals(errorMessage, result.getErrorMessage());
		assertTrue(result.getFandubSources().isEmpty());
		assertTrue(result.getTitles().isEmpty());
		assertEquals(result, resultCache.get(cacheKey, ResultDto.class));
	}

	@Test
	void shouldReturnFallbackValueBecauseMalServiceReturnEmptyListAndNoError() {
		//given
		InputDto inputDto = buildInputDto();
		String errorMessage = "";
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(), errorMessage);
		mockExternalMalServiceResponse(malServiceResponseDto);
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		//then
		assertNotNull(result);
		assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, result.getErrorMessage());
		assertTrue(result.getFandubSources().isEmpty());
		assertTrue(result.getTitles().isEmpty());
		assertNull(resultCache.get(cacheKey));
	}

	@Test
	void shouldReturnFallbackValueBecauseOfExceptionInMalService() {
		//given
		InputDto inputDto = buildInputDto();
		mockExternalMalServiceException();
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		//then
		assertNotNull(result);
		assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, result.getErrorMessage());
		assertTrue(result.getFandubSources().isEmpty());
		assertTrue(result.getTitles().isEmpty());
		assertNull(resultCache.get(cacheKey));
	}

	@Test
	void shouldReturnFallbackValueBecauseOfExceptionInFandubTitlesService() {
		//given
		InputDto inputDto = buildInputDto();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(regularTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		mockExternalFandubTitlesServiceException();
		String cacheKey = buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cachesNames.getResultCache());
		assertNotNull(resultCache);
		//when
		ResultDto result = resultProcessingService.getResult(inputDto).block();
		//then
		assertNotNull(result);
		assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, result.getErrorMessage());
		assertTrue(result.getFandubSources().isEmpty());
		assertTrue(result.getTitles().isEmpty());
		assertNull(resultCache.get(cacheKey));
	}

	private void mockExternalFandubTitlesServiceResponse(
			Map<Integer, Map<FandubSource, List<CommonTitle>>> commonTitlesForMalTitles) {
		doReturn(Mono.just(commonTitlesForMalTitles)).when(httpRequestService)
				.performHttpRequest(argThat(
						x -> x.getUrl().equals(starterCommonProperties.getExternalServices().getFandubTitlesServiceUrl() + "titles")));
	}

	private void mockExternalFandubTitlesServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(
						x -> x.getUrl().equals(starterCommonProperties.getExternalServices().getFandubTitlesServiceUrl() + "titles")));
	}

	private void mockExternalMalServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(starterCommonProperties.getExternalServices().getMalServiceUrl() + "titles?username=" + MAL_USERNAME
								+ "&status=" + MalTitleWatchingStatus.WATCHING.name())));
	}

	private void checkTitle(TitleDto actualTitle, TitleType expectedTitleType, MalTitle malTitle) {
		assertEquals(buildExpectedTitle(expectedTitleType, buildFandubUrls(expectedTitleType),
				buildFandubEpisodesNames(expectedTitleType), malTitle), actualTitle);
	}

	private TitleDto buildExpectedTitle(TitleType expectedTitleType, Map<FandubSource, String> fandubToUrl,
			Map<FandubSource, String> fandubToEpisodeName, MalTitle malTitle) {
		return TitleDto.builder()
				.type(expectedTitleType)
				.nameOnMal(malTitle.getName())
				.episodeNumberOnMal(MalUtils.getNextEpisodeForWatch(malTitle).toString())
				.posterUrlOnMal(malTitle.getPosterUrl())
				.animeUrlOnMal(malTitle.getAnimeUrl())
				.fandubToUrl(fandubToUrl)
				.fandubToEpisodeName(fandubToEpisodeName)
				.build();
	}

	private Map<FandubSource, String> buildFandubUrls(TitleType titleType) {
		Map<FandubSource, String> result = new LinkedHashMap<>();
		Map<FandubSource, String> fandubUrls = starterCommonProperties.getFandub().getUrls();
		if (titleType == TitleType.AVAILABLE) {
			result.put(FandubSource.ANILIBRIA, fandubUrls.get(FandubSource.ANILIBRIA) + REGULAR_TITLE_ANILIBRIA_URL);
		}
		return result;
	}

	private Map<FandubSource, String> buildFandubEpisodesNames(TitleType titleType) {
		Map<FandubSource, String> result = new LinkedHashMap<>();
		if (titleType == TitleType.AVAILABLE) {
			result.put(FandubSource.ANILIBRIA, ANILIBRIA_EPISODE_NAME);
		}
		return result;
	}

	@SneakyThrows
	private void waitForCacheThread() {
		TimeUnit.MILLISECONDS.sleep(300);
	}
}