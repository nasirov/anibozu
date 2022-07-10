package nasirov.yv.service.impl;

import static nasirov.yv.data.front.EventType.AVAILABLE;
import static nasirov.yv.data.front.EventType.DONE;
import static nasirov.yv.data.front.EventType.ERROR;
import static nasirov.yv.data.front.EventType.NOT_AVAILABLE;
import static nasirov.yv.data.front.EventType.NOT_FOUND;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.service.CacheCleanerServiceI;
import nasirov.yv.service.FandubTitlesServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ServerSentEventServiceI;
import nasirov.yv.service.TitleServiceI;
import org.apache.commons.collections4.CollectionUtils;
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

	private static final SseDto SSE_DTO_WITH_FINAL_EVENT = SseDto.builder().eventType(DONE).build();

	private static final SseDto SSE_DTO_WITH_ERROR_EVENT = SseDto.builder()
			.eventType(ERROR)
			.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
			.build();

	private final MalServiceI malService;

	private final FandubTitlesServiceI fandubTitlesService;

	private final TitleServiceI titleService;

	private final CacheCleanerServiceI cacheCleanerService;

	@Override
	@Cacheable(value = "sse", key = "#userInputDto.getUsername() + ':' +#userInputDto.getFanDubSources()", condition =
			"#userInputDto != null")
	public Flux<ServerSentEvent<SseDto>> getServerSentEvents(UserInputDto userInputDto) {
		return Mono.just(userInputDto)
				.flatMap(this::getUserWatchingTitles)
				.flatMap(x -> getCommonTitlesForMalTitles(userInputDto, x))
				.flatMapMany(x -> Flux.fromStream(x.entrySet().stream()))
				.map(x -> buildSseDto(userInputDto.getFanDubSources(), x))
				.index()
				.map(x -> buildServerSentEvent(x.getT2(), x.getT1().toString()))
				.defaultIfEmpty(buildServerSentEvent(SSE_DTO_WITH_ERROR_EVENT, LAST_EVENT_ID))
				.concatWith(Mono.just(buildServerSentEvent(SSE_DTO_WITH_FINAL_EVENT, LAST_EVENT_ID)))
				.share()
				.onErrorReturn(buildServerSentEvent(SSE_DTO_WITH_ERROR_EVENT, LAST_EVENT_ID))
				.doOnError(x -> log.error("ServerSentEvent failed for [{}]", userInputDto, x))
				.doOnComplete(() -> log.info("ServerSentEvent successfully completed for [{}]", userInputDto))
				.doOnCancel(() -> log.info("ServerSentEvent was canceled for [{}]", userInputDto))
				.doFinally(x -> cacheCleanerService.clearSseCache(userInputDto));
	}

	private Mono<List<MalTitle>> getUserWatchingTitles(UserInputDto userInputDto) {
		return malService.getUserWatchingTitles(userInputDto)
				.map(MalServiceResponseDto::getMalTitles)
				.filter(CollectionUtils::isNotEmpty);
	}

	private Mono<Map<MalTitle, Map<FanDubSource, List<CommonTitle>>>> getCommonTitlesForMalTitles(UserInputDto userInputDto,
			List<MalTitle> malTitles) {
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		return fandubTitlesService.getCommonTitles(userInputDto.getFanDubSources(), malTitles)
				.map(x -> remapMalIdToMalTitle(x, malIdToMalTitle));
	}

	private Map<MalTitle, Map<FanDubSource, List<CommonTitle>>> remapMalIdToMalTitle(
			Map<Integer, Map<FanDubSource, List<CommonTitle>>> commonTitlesForAllMalTitles,
			Map<Integer, MalTitle> malIdToMalTitle) {
		return commonTitlesForAllMalTitles.entrySet()
				.stream()
				.collect(Collectors.toMap(x -> malIdToMalTitle.get(x.getKey()), Entry::getValue, (o, n) -> o, LinkedHashMap::new));
	}

	private SseDto buildSseDto(Set<FanDubSource> fanDubSources, Entry<MalTitle, Map<FanDubSource, List<CommonTitle>>> entry) {
		TitleDto titleDto = titleService.buildTitle(entry.getKey(), entry.getValue());
		return SseDto.builder().eventType(determineEventType(fanDubSources, titleDto)).titleDto(titleDto).build();
	}

	private EventType determineEventType(Set<FanDubSource> fanDubSources, TitleDto titleDto) {
		EventType result = NOT_FOUND;
		for (FanDubSource source : fanDubSources) {
			String name = source.getName();
			if (titleDto.isAvailable(name)) {
				result = AVAILABLE;
				break;
			} else if (titleDto.isNotAvailable(name)) {
				result = NOT_AVAILABLE;
			}
		}
		return result;
	}

	private ServerSentEvent<SseDto> buildServerSentEvent(SseDto sseDto, String id) {
		return ServerSentEvent.builder(sseDto).id(id).build();
	}
}
