package nasirov.yv.service.impl;

import static nasirov.yv.utils.CommonTitleTestFactory.ANIDUB_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestFactory.ANILIBRIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestFactory.REGULAR_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.CommonTitleTestFactory.REGULAR_TITLE_ANILIBRIA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.util.MalUtils;
import nasirov.yv.utils.CommonTitleTestFactory;
import nasirov.yv.utils.MalTitleTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class ServerSentEventServiceTest extends AbstractTest {

	@Test
	void shouldCompleteSuccessfully() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		Set<FanDubSource> fanDubSources = userInputDto.getFanDubSources();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalTitle concretizedTitle = MalTitleTestFactory.buildConcretizedMalTitle();
		MalTitle notFoundOnFandubTitle = MalTitleTestFactory.buildNotFoundOnFandubMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(
				Lists.newArrayList(regularTitle, concretizedTitle, notFoundOnFandubTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<Integer, Map<FanDubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), CommonTitleTestFactory.buildRegularCommonTitles(fanDubSources));
		orderedMalTitles.put(concretizedTitle.getId(), CommonTitleTestFactory.buildConcretizedCommonTitles(fanDubSources));
		orderedMalTitles.put(notFoundOnFandubTitle.getId(),
				CommonTitleTestFactory.buildNotFoundOnFandubCommonTitles(fanDubSources));
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList().block();
		assertNotNull(serverSentEvents);
		assertEquals(4, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), 0, EventType.AVAILABLE, regularTitle);
		checkServerSentEvent(serverSentEvents.get(1), 1, EventType.NOT_AVAILABLE, concretizedTitle);
		checkServerSentEvent(serverSentEvents.get(2), 2, EventType.NOT_FOUND, notFoundOnFandubTitle);
		checkServerSentEvent(serverSentEvents.get(3), -1, EventType.DONE, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldCompleteWithErrorEventBecauseUserWatchingTitlesNotFound() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(),
				"error message from " + "mts");
		mockExternalMalServiceResponse(malServiceResponseDto);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList().block();
		assertNotNull(serverSentEvents);
		assertEquals(2, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), -1, EventType.ERROR, null);
		checkServerSentEvent(serverSentEvents.get(1), -1, EventType.DONE, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldCompleteWithErrorEventBecauseOfExceptionInMalService() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		mockExternalMalServiceException();
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList().block();
		assertNotNull(serverSentEvents);
		assertEquals(1, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), -1, EventType.ERROR, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldCompleteWithErrorEventBecauseOfExceptionInFandubTitlesService() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(regularTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		mockExternalFandubTitlesServiceException();
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList().block();
		assertNotNull(serverSentEvents);
		assertEquals(1, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), -1, EventType.ERROR, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldFailOnTitleServiceAndCompleteWithErrorEvent() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		Set<FanDubSource> fanDubSources = userInputDto.getFanDubSources();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalTitle concretizedTitle = MalTitleTestFactory.buildConcretizedMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(
				Lists.newArrayList(regularTitle, concretizedTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<FanDubSource, List<CommonTitle>> commonTitlesForConcretized = CommonTitleTestFactory.buildConcretizedCommonTitles(
				fanDubSources);
		mockTitleServiceException(concretizedTitle, commonTitlesForConcretized);
		Map<Integer, Map<FanDubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), CommonTitleTestFactory.buildRegularCommonTitles(fanDubSources));
		orderedMalTitles.put(concretizedTitle.getId(), commonTitlesForConcretized);
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = MAL_USERNAME + buildCacheKeyForUser();
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList().block();
		assertNotNull(serverSentEvents);
		assertEquals(2, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), 0, EventType.AVAILABLE, regularTitle);
		checkServerSentEvent(serverSentEvents.get(1), -1, EventType.ERROR, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	private void mockExternalFandubTitlesServiceResponse(
			Map<Integer, Map<FanDubSource, List<CommonTitle>>> commonTitlesForMalTitles) {
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

	private void mockTitleServiceException(MalTitle malTitle, Map<FanDubSource, List<CommonTitle>> commonTitles) {
		doThrow(new RuntimeException("foo bar cause")).when(titleService).buildTitle(malTitle, commonTitles);
	}

	private void checkServerSentEvent(ServerSentEvent<SseDto> serverSentEvent, int eventId, EventType expectedEventType,
			MalTitle malTitle) {
		assertEquals(String.valueOf(eventId), serverSentEvent.id());
		SseDto sseDto = serverSentEvent.data();
		assertNotNull(sseDto);
		String actualErrorMessage = sseDto.getErrorMessage();
		TitleDto actualTitleDto = sseDto.getTitleDto();
		assertEquals(expectedEventType, sseDto.getEventType());
		switch (expectedEventType) {
			case ERROR:
				assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, actualErrorMessage);
			case DONE:
				assertNull(actualTitleDto);
				break;
			default:
				assertEquals(
						buildExpectedTitleDto(buildFanDubUrls(expectedEventType), buildFanDubEpisodeNames(expectedEventType), malTitle),
						actualTitleDto);
				assertNull(actualErrorMessage);
		}
	}

	private TitleDto buildExpectedTitleDto(Map<FanDubSource, String> fanDubUrls, Map<FanDubSource, String> fanDubEpisodeNames,
			MalTitle malTitle) {
		return TitleDto.builder()
				.animeName(malTitle.getName())
				.malEpisodeNumber(MalUtils.getNextEpisodeForWatch(malTitle).toString())
				.posterUrlOnMal(malTitle.getPosterUrl())
				.animeUrlOnMal(malTitle.getAnimeUrl())
				.fanDubUrls(fanDubUrls)
				.fanDubEpisodeNames(fanDubEpisodeNames)
				.build();
	}

	private Map<FanDubSource, String> buildFanDubUrls(EventType eventType) {
		Map<FanDubSource, String> result = new LinkedHashMap<>();
		Map<FanDubSource, String> fandubUrls = fanDubProps.getUrls();
		switch (eventType) {
			case AVAILABLE:
				result.put(FanDubSource.ANIDUB, fandubUrls.get(FanDubSource.ANIDUB) + REGULAR_TITLE_ANIDUB_URL);
				result.put(FanDubSource.ANILIBRIA, fandubUrls.get(FanDubSource.ANILIBRIA) + REGULAR_TITLE_ANILIBRIA_URL);
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIDUB, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				result.put(FanDubSource.ANILIBRIA, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIDUB, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FanDubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
		}
		return result;
	}

	private Map<FanDubSource, String> buildFanDubEpisodeNames(EventType eventType) {
		Map<FanDubSource, String> result = new LinkedHashMap<>();
		switch (eventType) {
			case AVAILABLE:
				result.put(FanDubSource.ANIDUB, ANIDUB_EPISODE_NAME);
				result.put(FanDubSource.ANILIBRIA, ANILIBRIA_EPISODE_NAME);
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIDUB, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				result.put(FanDubSource.ANILIBRIA, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIDUB, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				result.put(FanDubSource.ANILIBRIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				break;
		}
		return result;
	}
}