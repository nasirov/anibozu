package nasirov.yv.service.impl.common;

import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public class ServerSentEventServiceTest extends AbstractTest {

	@Test
	public void shouldCompleteSuccessfully() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto();
		mockMalService(userInputDto, malServiceResponseDto);
		mockAnimeService(userInputDto.getFanDubSources());
		//when
		Flux<ServerSentEvent<SseDto>> result = serverSentEventService.getServerSentEvents(userInputDto);
		//then
		String cacheKey = "foobar:ANIMEDIA,NINEANIME";
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		assertEquals(result, sseCache.get(cacheKey, Flux.class));
		List<ServerSentEvent<SseDto>> serverSentEvents = result.collectList()
				.block();
		assertNotNull(serverSentEvents);
		assertEquals(4, serverSentEvents.size());
		checkServerSentEvent(serverSentEvents.get(0), 0, EventType.AVAILABLE);
		checkServerSentEvent(serverSentEvents.get(1), 1, EventType.NOT_AVAILABLE);
		checkServerSentEvent(serverSentEvents.get(2), 2, EventType.NOT_FOUND);
		checkServerSentEvent(serverSentEvents.get(3), -1, EventType.DONE);
		verify(cacheCleanerService).clearSseCache(userInputDto);
		assertNull(sseCache.get(cacheKey));
	}

	private UserInputDto buildUserInputDto() {
		Set<FanDubSource> fanDubSources = new LinkedHashSet<>();
		fanDubSources.add(FanDubSource.ANIMEDIA);
		fanDubSources.add(FanDubSource.NINEANIME);
		return UserInputDto.builder()
				.username("foobar")
				.fanDubSources(fanDubSources)
				.build();
	}

	private MalServiceResponseDto buildMalServiceResponseDto() {
		return MalServiceResponseDto.builder()
				.username("foobar")
				.malTitles(Lists.newArrayList(new MalTitle(), new MalTitle(), new MalTitle()))
				.errorMessage("")
				.build();
	}

	private void mockMalService(UserInputDto userInputDto, MalServiceResponseDto malServiceResponseDto) {
		doReturn(Mono.just(malServiceResponseDto)).when(malService)
				.getUserWatchingTitles(userInputDto);
	}

	private void mockAnimeService(Set<FanDubSource> fanDubSources) {
		doReturn(Mono.just(buildAnime(buildFanDubUrls(EventType.AVAILABLE))),
				Mono.just(buildAnime(buildFanDubUrls(EventType.NOT_AVAILABLE))),
				Mono.just(buildAnime(buildFanDubUrls(EventType.NOT_FOUND)))).when(animeService)
				.buildAnime(eq(fanDubSources), any(MalTitle.class));
	}

	private void mockAnimeServiceFail(Set<FanDubSource> fanDubSources) {
		doThrow(new RuntimeException("foo bar cause")).when(animeService)
				.buildAnime(eq(fanDubSources), any(MalTitle.class));
	}

	private Map<FanDubSource, String> buildFanDubUrls(EventType eventType) {
		LinkedHashMap<FanDubSource, String> result = new LinkedHashMap<>();
		switch (eventType) {
			case AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ORIGINAL_NAME + "/1/1");
				result.put(FanDubSource.NINEANIME, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.NOT_AVAILABLE_EPISODE_URL);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.TITLE_NOT_FOUND_EPISODE_URL);
				break;
		}
		return result;
	}

	private Anime buildAnime(Map<FanDubSource, String> fanDubUrls) {
		return Anime.builder()
				.animeName(REGULAR_TITLE_ORIGINAL_NAME)
				.malEpisodeNumber("1")
				.posterUrlOnMal(MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL)
				.animeUrlOnMal(MY_ANIME_LIST_URL + REGULAR_TITLE_MAL_ANIME_URL)
				.fanDubUrls(fanDubUrls)
				.build();
	}

	private void checkServerSentEvent(ServerSentEvent<SseDto> serverSentEvent, int eventId, EventType eventType) {
		assertEquals(String.valueOf(eventId), serverSentEvent.id());
		SseDto sseDto = serverSentEvent.data();
		assertNotNull(sseDto);
		if (eventType == EventType.DONE) {
			assertNull(sseDto.getAnime());
		} else {
			assertEquals(buildAnime(buildFanDubUrls(eventType)), sseDto.getAnime());
		}
		assertEquals(eventType, sseDto.getEventType());
	}
}