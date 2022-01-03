package nasirov.yv.service.impl.common;

import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEDIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.NINE_ANIME_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.util.MalUtils;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
class ServerSentEventServiceTest extends AbstractTest {

	@Test
	void shouldCompleteSuccessfully() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		Set<FanDubSource> fanDubSources = userInputDto.getFanDubSources();
		MalTitle regularTitle = buildRegularTitle();
		MalTitle concretizedTitle = buildConcretizedTitle();
		MalTitle notFoundOnFandubTitle = buildNotFoundOnFandubTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(regularTitle,
				concretizedTitle,
				notFoundOnFandubTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<Integer, Map<FanDubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), buildRegularCommonTitles(fanDubSources));
		orderedMalTitles.put(concretizedTitle.getId(), buildConcretizedCommonTitles(fanDubSources));
		orderedMalTitles.put(notFoundOnFandubTitle.getId(), buildNotFoundOnFandubCommonTitles(fanDubSources));
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = TEST_ACC_FOR_DEV + ":ANIMEDIA,NINEANIME";
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList()
				.block();
		assertNotNull(serverSentEvents);
		assertEquals(4, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), 0, EventType.NOT_FOUND, notFoundOnFandubTitle);
		checkServerSentEvent(serverSentEvents.get(1), 1, EventType.AVAILABLE, regularTitle);
		checkServerSentEvent(serverSentEvents.get(2), 2, EventType.NOT_AVAILABLE, concretizedTitle);
		checkServerSentEvent(serverSentEvents.get(3), -1, EventType.DONE, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldCompleteWithErrorEventBecauseUserWatchingTitlesNotFound() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(), "error message from mts");
		mockExternalMalServiceResponse(malServiceResponseDto);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = TEST_ACC_FOR_DEV + ":ANIMEDIA,NINEANIME";
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList()
				.block();
		assertNotNull(serverSentEvents);
		assertEquals(2, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), -1, EventType.ERROR, null);
		checkServerSentEvent(serverSentEvents.get(1), -1, EventType.DONE, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	@Test
	void shouldFailOnAnimeServiceAndCompleteWithErrorEvent() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		Set<FanDubSource> fanDubSources = userInputDto.getFanDubSources();
		MalTitle regularTitle = buildRegularTitle();
		MalTitle concretizedTitle = buildConcretizedTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(regularTitle, concretizedTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<FanDubSource, List<CommonTitle>> commonTitlesForConcretized = buildConcretizedCommonTitles(fanDubSources);
		mockAnimeServiceException(concretizedTitle, commonTitlesForConcretized);
		Map<Integer, Map<FanDubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), buildRegularCommonTitles(fanDubSources));
		orderedMalTitles.put(concretizedTitle.getId(), commonTitlesForConcretized);
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = TEST_ACC_FOR_DEV + ":ANIMEDIA,NINEANIME";
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList()
				.block();
		assertNotNull(serverSentEvents);
		assertEquals(2, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), 0, EventType.AVAILABLE, regularTitle);
		checkServerSentEvent(serverSentEvents.get(1), -1, EventType.ERROR, null);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	private void mockAnimeServiceException(MalTitle malTitle, Map<FanDubSource, List<CommonTitle>> commonTitles) {
		doThrow(new RuntimeException("foo bar cause")).when(animeService)
				.buildAnime(malTitle, commonTitles);
	}

	private void checkServerSentEvent(ServerSentEvent<SseDto> serverSentEvent, int eventId, EventType expectedEventType, MalTitle malTitle) {
		assertEquals(String.valueOf(eventId), serverSentEvent.id());
		SseDto sseDto = serverSentEvent.data();
		assertNotNull(sseDto);
		String actualErrorMessage = sseDto.getErrorMessage();
		Anime actualAnime = sseDto.getAnime();
		assertEquals(expectedEventType, sseDto.getEventType());
		switch (expectedEventType) {
			case ERROR:
				assertEquals(BaseConstants.GENERIC_ERROR_MESSAGE, actualErrorMessage);
			case DONE:
				assertNull(actualAnime);
				break;
			default:
				assertEquals(buildExpectedAnime(buildFanDubUrls(expectedEventType), buildFanDubEpisodeNames(expectedEventType), malTitle), actualAnime);
				assertNull(actualErrorMessage);
		}
	}

	private Anime buildExpectedAnime(Map<FanDubSource, String> fanDubUrls, Map<FanDubSource, String> fanDubEpisodeNames, MalTitle malTitle) {
		return Anime.builder()
				.animeName(malTitle.getName())
				.malEpisodeNumber(MalUtils.getNextEpisodeForWatch(malTitle)
						.toString())
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
				result.put(FanDubSource.ANIMEDIA, fandubUrls.get(ANIMEDIA) + REGULAR_TITLE_ANIMEDIA_URL + "/1/1");
				result.put(FanDubSource.NINEANIME, fandubUrls.get(NINEANIME) + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1");
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
		}
		return result;
	}

	private Map<FanDubSource, String> buildFanDubEpisodeNames(EventType eventType) {
		Map<FanDubSource, String> result = new LinkedHashMap<>();
		switch (eventType) {
			case AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, ANIMEDIA_EPISODE_NAME);
				result.put(FanDubSource.NINEANIME, NINE_ANIME_EPISODE_NAME);
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				result.put(FanDubSource.NINEANIME, BaseConstants.NOT_AVAILABLE_EPISODE_NAME);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				result.put(FanDubSource.NINEANIME, BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME);
				break;
		}
		return result;
	}
}