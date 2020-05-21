package nasirov.yv.service.impl.server_sent_events;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.properties.SseProps;
import nasirov.yv.data.task.SseAction;
import nasirov.yv.service.CacheCleanerServiceI;
import nasirov.yv.service.SseActionServiceI;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterExecutorService implements SseEmitterExecutorServiceI {

	private final ForkJoinPool commonPool;

	private final CacheCleanerServiceI cacheCleanerService;

	private final SseActionServiceI sseActionService;

	private final SseProps sseProps;

	/**
	 * Creates a SseAction with a SseEmitter and execute in async
	 *
	 * @param malUser a MALUser from front
	 * @return a SseEmitter
	 */
	@Override
	public SseEmitter buildAndExecuteSseEmitter(MALUser malUser) {
		log.debug("Trying to build SseEmitter for [{}]...", malUser);
		SseEmitter sseEmitter = new SseEmitter(Duration.ofMinutes(sseProps.getTimeoutInMin())
				.toMillis());
		enrichSseEmitterWithCallbacks(sseEmitter, malUser);
		SseAction task = sseActionService.buildSseAction(sseEmitter, malUser);
		commonPool.execute(task);
		log.debug("Successfully built SseEmitter for [{}].", malUser);
		return sseEmitter;
	}

	private void enrichSseEmitterWithCallbacks(SseEmitter sseEmitter, MALUser malUser) {
		sseEmitter.onError(onErrorConsumer(malUser));
		sseEmitter.onTimeout(onTimeoutCallback(malUser));
		sseEmitter.onCompletion(onCompletionCallback(malUser));
	}

	/**
	 * Builds Consumer that will request sse cache eviction
	 *
	 * @param malUser sse cache key
	 * @return consumer
	 */
	private Consumer<Throwable> onErrorConsumer(MALUser malUser) {
		return e -> {
			log.error("SseEmitter error callback for [{}]", malUser);
			cacheCleanerService.clearSseCache(malUser);
		};
	}

	private Runnable onTimeoutCallback(MALUser malUser) {
		return () -> log.error("SseEmitter timeout callback for [{}]", malUser);
	}

	/**
	 * Builds Runnable task that will request sse cache eviction
	 *
	 * @param malUser sse cache key
	 * @return task
	 */
	private Runnable onCompletionCallback(MALUser malUser) {
		return () -> {
			log.info("SseEmitter completed callback for [{}]", malUser);
			cacheCleanerService.clearSseCache(malUser);
		};
	}
}
