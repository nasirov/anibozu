package nasirov.yv.data.task;

import static nasirov.yv.data.front.EventType.DONE;

import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.impl.common.AnimeService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * Created by nasirov.yv
 */
@Slf4j
@RequiredArgsConstructor
public class SseAction extends RecursiveAction {

	private final AnimeService animeService;

	private final MALServiceI malService;

	private final SseEmitter sseEmitter;

	private final MALUser malUser;

	@Getter
	private final AtomicBoolean isRunning = new AtomicBoolean(true);

	@Override
	protected void compute() {
		try {
			log.info("Start process SseAction for [{}]", malUser);
			List<UserMALTitleInfo> watchingTitles = malService.getWatchingTitles(malUser.getUsername());
			for (int i = 0; i < watchingTitles.size() && isRunning.get(); i++) {
				Set<FanDubSource> fanDubSources = malUser.getFanDubSources();
				Anime anime = animeService.buildAnime(fanDubSources, watchingTitles.get(i));
				SseDto sseDto = buildSseDto(fanDubSources, anime);
				sseEmitter.send(buildSseEvent(i, sseDto));
			}
			sseEmitter.send(buildSseEvent(-1, buildDtoWithFinalEvent()));
			sseEmitter.complete();
			log.info("End process SseAction for [{}]", malUser);
		} catch (Exception e) {
			log.error("Exception has been occurred during process SseAction for [{}]", malUser, e);
			sseEmitter.completeWithError(e);
		}
	}

	private SseEventBuilder buildSseEvent(int eventId, SseDto sseDto) {
		return SseEmitter.event()
				.id(String.valueOf(eventId))
				.data(sseDto);
	}

	private SseDto buildSseDto(Set<FanDubSource> fanDubSources, Anime anime) {
		return SseDto.builder()
				.eventType(determineEvent(fanDubSources, anime))
				.anime(anime)
				.build();
	}

	private EventType determineEvent(Set<FanDubSource> fanDubSources, Anime anime) {
		EventType result;
		int available = 0;
		int notAvailable = 0;
		for (FanDubSource source : fanDubSources) {
			String name = source.getName();
			if (anime.isAvailable(name)) {
				available++;
			} else if (anime.isNotAvailable(name)) {
				notAvailable++;
			}
		}
		if (available != 0) {
			result = EventType.AVAILABLE;
		} else if (notAvailable != 0) {
			result = EventType.NOT_AVAILABLE;
		} else {
			result = EventType.NOT_FOUND;
		}
		return result;
	}

	private SseDto buildDtoWithFinalEvent() {
		return SseDto.builder()
				.eventType(DONE)
				.build();
	}
}
