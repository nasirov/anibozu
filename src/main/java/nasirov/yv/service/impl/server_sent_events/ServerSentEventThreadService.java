package nasirov.yv.service.impl.server_sent_events;

import static java.util.Objects.nonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.task.ServerSentEventThread;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ServerSentEventThreadServiceI;
import nasirov.yv.service.impl.common.AnimeService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerSentEventThreadService implements ServerSentEventThreadServiceI {

	private final AnimeService animeService;

	private final MalServiceI malService;

	private final CacheProps cacheProps;

	private final CacheManager cacheManager;

	/**
	 * Builds and caches a ServerSentEventThread
	 *
	 * @param sseEmitter a SseEmitter for Server-Sent Events
	 * @param userInputDto    an user input from front
	 * @return ServerSentEventThread
	 */
	@Override
	public ServerSentEventThread buildServerSentEventThread(SseEmitter sseEmitter, UserInputDto userInputDto) {
		log.debug("Trying to build ServerSentEventThread for [{}]...", userInputDto);
		ServerSentEventThread result = new ServerSentEventThread(animeService, malService, sseEmitter, userInputDto);
		Cache cache = cacheManager.getCache(cacheProps.getSse()
				.getName());
		if (nonNull(cache)) {
			String key = userInputDto.getUsername();
			stopCachedTask(key, cache);
			cache.put(key, result);
		}
		log.debug("Successfully built and cached ServerSentEventThread for [{}].", userInputDto);
		return result;
	}

	/**
	 * Stops cached ServerSentEventThread in order to avoid duplicating and resources leak
	 *
	 * @param key   sse cache key
	 * @param cache sse cache
	 */
	private void stopCachedTask(String key, Cache cache) {
		ServerSentEventThread cachedServerSentEventThread = cache.get(key, ServerSentEventThread.class);
		if (nonNull(cachedServerSentEventThread)) {
			log.info("ServerSentEventThread with key [{}] will be stopped!", key);
			cachedServerSentEventThread.getRunning()
					.set(false);
		}
	}
}
