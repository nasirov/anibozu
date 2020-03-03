package nasirov.yv.service.impl;

import static java.util.Objects.nonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.task.SseAction;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.SseActionServiceI;
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
public class SseActionService implements SseActionServiceI {

	private final AnimeService animeService;

	private final MALServiceI malService;

	private final CacheProps cacheProps;

	private final CacheManager cacheManager;

	/**
	 * Builds and caches a SseAction
	 *
	 * @param sseEmitter a SseEmitter for Server-Sent Events
	 * @param malUser    a MALUser from front
	 * @return an SseAction
	 */
	@Override
	public SseAction buildSseAction(SseEmitter sseEmitter, MALUser malUser) {
		log.debug("Received request for SseAction creation");
		SseAction result = new SseAction(animeService, malService, sseEmitter, malUser);
		Cache cache = cacheManager.getCache(cacheProps.getSse()
				.getName());
		String key = String.valueOf(malUser.hashCode());
		stopCachedTask(key, cache);
		cache.put(key, result);
		return result;
	}

	/**
	 * Stops cached SseAction in order to avoid duplicating and resources leak
	 *
	 * @param key   sse cache key
	 * @param cache sse cache
	 */
	private void stopCachedTask(String key, Cache cache) {
		SseAction cachedSseAction = cache.get(key, SseAction.class);
		if (nonNull(cachedSseAction)) {
			log.info("SseAction with key [{}] will be stopped!", key);
			cachedSseAction.getIsRunning()
					.set(false);
		}
	}
}
