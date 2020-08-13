package nasirov.yv.data.task;

import static nasirov.yv.data.front.EventType.DONE;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.mal.MalUser;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.impl.common.AnimeService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class ServerSentEventThread implements Runnable {

	private final AnimeService animeService;

	private final MalServiceI malService;

	private final SseEmitter sseEmitter;

	private final MalUser malUser;

	@Getter
	private final AtomicBoolean running;

	public ServerSentEventThread(AnimeService animeService, MalServiceI malService, SseEmitter sseEmitter, MalUser malUser) {
		this.animeService = animeService;
		this.malService = malService;
		this.sseEmitter = sseEmitter;
		this.malUser = malUser;
		this.running = new AtomicBoolean(false);
	}

	@Override
	public void run() {
		if (running.compareAndSet(false, true)) {
			try {
				log.info("Start process ServerSentEventThread for [{}]", malUser);
				List<MalTitle> watchingTitles = malService.getWatchingTitles(malUser.getUsername());
				for (int i = 0; i < watchingTitles.size() && running.get(); i++) {
					Set<FanDubSource> fanDubSources = malUser.getFanDubSources();
					Anime anime = animeService.buildAnime(fanDubSources, watchingTitles.get(i));
					SseDto sseDto = buildSseDto(fanDubSources, anime);
					sseEmitter.send(buildSseEvent(i, sseDto));
				}
				sseEmitter.send(buildSseEvent(-1, buildDtoWithFinalEvent()));
				sseEmitter.complete();
				log.info("End process ServerSentEventThread for [{}]", malUser);
			} catch (Exception e) {
				log.error("Exception has occurred during process ServerSentEventThread for [{}]", malUser, e);
				sseEmitter.completeWithError(e);
			}
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
