package nasirov.yv.service.impl.server_sent_events;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.SseProps;
import nasirov.yv.data.task.ServerSentEventThread;
import nasirov.yv.service.CacheCleanerServiceI;
import nasirov.yv.service.ServerSentEventThreadServiceI;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterExecutorService implements SseEmitterExecutorServiceI {

	private final ExecutorService executorService;

	private final CacheCleanerServiceI cacheCleanerService;

	private final ServerSentEventThreadServiceI sseActionService;

	private final SseProps sseProps;

	/**
	 * Creates a SseAction with a SseEmitter and execute in async
	 *
	 * @param userInputDto an user input from front
	 * @return a SseEmitter
	 */
	@Override
	public SseEmitter buildAndExecuteSseEmitter(UserInputDto userInputDto) {
		log.debug("Trying to build SseEmitter for [{}]...", userInputDto);
		SseEmitter sseEmitter = new SseEmitter(Duration.ofMinutes(sseProps.getTimeoutInMin())
				.toMillis());
		enrichSseEmitterWithCallbacks(sseEmitter, userInputDto);
		ServerSentEventThread serverSentEventThread = sseActionService.buildServerSentEventThread(sseEmitter, userInputDto);
		executorService.execute(serverSentEventThread);
		log.debug("Successfully built SseEmitter for [{}].", userInputDto);
		return sseEmitter;
	}

	private void enrichSseEmitterWithCallbacks(SseEmitter sseEmitter, UserInputDto userInputDto) {
		sseEmitter.onError(onErrorConsumer(userInputDto));
		sseEmitter.onTimeout(onTimeoutCallback(userInputDto));
		sseEmitter.onCompletion(onCompletionCallback(userInputDto));
	}

	/**
	 * Builds Consumer that will request sse cache eviction
	 *
	 * @param userInputDto sse cache key
	 * @return consumer
	 */
	private Consumer<Throwable> onErrorConsumer(UserInputDto userInputDto) {
		return e -> {
			log.error("SseEmitter error callback for [{}]", userInputDto);
			cacheCleanerService.clearSseCache(userInputDto);
		};
	}

	private Runnable onTimeoutCallback(UserInputDto userInputDto) {
		return () -> log.error("SseEmitter timeout callback for [{}]", userInputDto);
	}

	/**
	 * Builds Runnable task that will request sse cache eviction
	 *
	 * @param userInputDto sse cache key
	 * @return task
	 */
	private Runnable onCompletionCallback(UserInputDto userInputDto) {
		return () -> {
			log.info("SseEmitter completed callback for [{}]", userInputDto);
			cacheCleanerService.clearSseCache(userInputDto);
		};
	}
}
