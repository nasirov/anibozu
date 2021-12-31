package nasirov.yv.service.impl.common;

import static nasirov.yv.data.front.EventType.AVAILABLE;
import static nasirov.yv.data.front.EventType.DONE;
import static nasirov.yv.data.front.EventType.ERROR;
import static nasirov.yv.data.front.EventType.NOT_AVAILABLE;
import static nasirov.yv.data.front.EventType.NOT_FOUND;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.CacheCleanerServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ServerSentEventServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerSentEventService implements ServerSentEventServiceI {

	private static final String LAST_EVENT_ID = "-1";

	private final MalServiceI malService;

	private final AnimeServiceI animeService;

	private final CacheCleanerServiceI cacheCleanerService;

	@Override
	@Cacheable(value = "sse", key = "#userInputDto.getUsername() + ':' +#userInputDto.getFanDubSources()", condition = "#userInputDto != null")
	public Flux<ServerSentEvent<SseDto>> getServerSentEvents(UserInputDto userInputDto) {
		return Mono.just(userInputDto)
				.flatMap(malService::getUserWatchingTitles)
				.map(MalServiceResponseDto::getMalTitles)
				.flatMapMany(Flux::fromIterable)
				.flatMap(x -> animeService.buildAnime(userInputDto.getFanDubSources(), x))
				.map(x -> buildSseDto(userInputDto.getFanDubSources(), x))
				.index()
				.map(x -> buildServerSentEvent(x.getT2(),
						x.getT1()
								.toString()))
				.defaultIfEmpty(buildServerSentEvent(buildDtoWithErrorEvent(), LAST_EVENT_ID))
				.concatWith(Mono.just(buildServerSentEvent(buildDtoWithFinalEvent(), LAST_EVENT_ID)))
				.share()
				.onErrorReturn(buildServerSentEvent(buildDtoWithErrorEvent(), LAST_EVENT_ID))
				.doOnError(x -> log.error("ServerSentEvent failed for [{}]", userInputDto, x))
				.doOnComplete(() -> log.info("ServerSentEvent successfully completed for [{}]", userInputDto))
				.doOnCancel(() -> log.info("ServerSentEvent was canceled for [{}]", userInputDto))
				.doFinally(x -> cacheCleanerService.clearSseCache(userInputDto));
	}

	private SseDto buildSseDto(Set<FanDubSource> fanDubSources, Anime anime) {
		return SseDto.builder()
				.eventType(determineEventType(fanDubSources, anime))
				.anime(anime)
				.build();
	}

	private EventType determineEventType(Set<FanDubSource> fanDubSources, Anime anime) {
		EventType result = NOT_FOUND;
		for (FanDubSource source : fanDubSources) {
			String name = source.getName();
			if (anime.isAvailable(name)) {
				result = AVAILABLE;
				break;
			} else if (anime.isNotAvailable(name)) {
				result = NOT_AVAILABLE;
			}
		}
		return result;
	}

	private ServerSentEvent<SseDto> buildServerSentEvent(SseDto sseDto, String id) {
		return ServerSentEvent.builder(sseDto)
				.id(id)
				.build();
	}

	private SseDto buildDtoWithFinalEvent() {
		return SseDto.builder()
				.eventType(DONE)
				.build();
	}

	private SseDto buildDtoWithErrorEvent() {
		return SseDto.builder()
				.eventType(ERROR)
				.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
				.build();
	}
}
