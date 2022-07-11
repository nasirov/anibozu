package nasirov.yv.service.impl;

import static nasirov.yv.utils.CommonTitleTestFactory.ANIDUB_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestFactory.REGULAR_TITLE_ANIDUB_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.InputDto;
import nasirov.yv.data.front.ResultDto;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.data.front.TitleType;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.util.MalUtils;
import nasirov.yv.utils.CommonTitleTestFactory;
import nasirov.yv.utils.MalTitleTestFactory;
import org.apache.commons.lang3.StringUtils;
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
		Set<FandubSource> fandubSources = inputDto.getFandubSources();
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
		//when
		Mono<ResultDto> result = resultProcessingService.getResult(inputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cacheProps.getResult().getName());
		assertNotNull(resultCache);
		ResultDto resultDto = result.block();
		assertNotNull(resultDto);
		assertEquals(StringUtils.EMPTY, resultDto.getErrorMessage());
		List<TitleDto> available = resultDto.getAvailableTitles();
		List<TitleDto> notAvailable = resultDto.getNotAvailableTitles();
		List<TitleDto> notFound = resultDto.getNotFoundTitles();
		assertEquals(1, available.size());
		assertEquals(1, notAvailable.size());
		assertEquals(1, notFound.size());
		checkTitle(available.get(0), TitleType.AVAILABLE, regularTitle);
		checkTitle(notAvailable.get(0), TitleType.NOT_AVAILABLE, concretizedTitle);
		checkTitle(notFound.get(0), TitleType.NOT_FOUND, notFoundOnFandubTitle);
		ResultDto cachedResult = resultCache.get(cacheKey, ResultDto.class);
		assertEquals(resultDto, cachedResult);
		assertEquals(cachedResult, resultProcessingService.getResult(inputDto).block());
		verify(httpRequestService).performHttpRequest(argThat(x -> x.getUrl()
				.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + MAL_USERNAME + "&status="
						+ MalTitleWatchingStatus.WATCHING.name())));
	}

	@Test
	void shouldReturnResponseWithMalServiceErrorMessage() {
		//given
		InputDto inputDto = buildInputDto();
		String errorMessage = "Not found watching titles for " + MAL_USERNAME + " !";
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(), errorMessage);
		mockExternalMalServiceResponse(malServiceResponseDto);
		//when
		Mono<ResultDto> result = resultProcessingService.getResult(inputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cacheProps.getResult().getName());
		assertNotNull(resultCache);
		ResultDto resultDto = result.block();
		assertNotNull(resultDto);
		assertTrue(resultDto.getAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
		assertEquals(resultDto, resultCache.get(cacheKey, ResultDto.class));
	}

	@Test
	void shouldReturnFallbackValueBecauseOfExceptionInMalService() {
		//given
		InputDto inputDto = buildInputDto();
		mockExternalMalServiceException();
		//when
		Mono<ResultDto> result = resultProcessingService.getResult(inputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cacheProps.getResult().getName());
		assertNotNull(resultCache);
		ResultDto resultDto = result.block();
		assertNotNull(resultDto);
		assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, resultDto.getErrorMessage());
		assertTrue(resultDto.getAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
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
		//when
		Mono<ResultDto> result = resultProcessingService.getResult(inputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache resultCache = cacheManager.getCache(cacheProps.getResult().getName());
		assertNotNull(resultCache);
		ResultDto resultDto = result.block();
		assertNotNull(resultDto);
		assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, resultDto.getErrorMessage());
		assertTrue(resultDto.getAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
		assertTrue(resultDto.getNotAvailableTitles().isEmpty());
		assertNull(resultCache.get(cacheKey));
	}

	private void mockExternalFandubTitlesServiceResponse(
			Map<Integer, Map<FandubSource, List<CommonTitle>>> commonTitlesForMalTitles) {
		doReturn(Mono.just(commonTitlesForMalTitles)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl().equals(externalServicesProps.getFandubTitlesServiceUrl() + "titles")));
	}

	private void mockExternalFandubTitlesServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl().equals(externalServicesProps.getFandubTitlesServiceUrl() + "titles")));
	}

	private void mockExternalMalServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + MAL_USERNAME + "&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
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
		Map<FandubSource, String> fandubUrls = fandubProps.getUrls();
		switch (titleType) {
			case AVAILABLE:
				result.put(FandubSource.ANILIBRIA, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				result.put(FandubSource.ANIDUB, fandubUrls.get(FandubSource.ANIDUB) + REGULAR_TITLE_ANIDUB_URL);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
			case NOT_AVAILABLE:
				result.put(FandubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FandubSource.ANIDUB, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
			case NOT_FOUND:
				result.put(FandubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FandubSource.ANIDUB, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
		}
		return result;
	}

	private Map<FandubSource, String> buildFandubEpisodesNames(TitleType titleType) {
		Map<FandubSource, String> result = new LinkedHashMap<>();
		switch (titleType) {
			case AVAILABLE:
				result.put(FandubSource.ANILIBRIA, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				result.put(FandubSource.ANIDUB, ANIDUB_EPISODE_NAME);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				break;
			case NOT_AVAILABLE:
				result.put(FandubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				result.put(FandubSource.ANIDUB, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				break;
			case NOT_FOUND:
				result.put(FandubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				result.put(FandubSource.ANIDUB, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				result.put(FandubSource.SHIZAPROJECT, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				break;
		}
		return result;
	}
}