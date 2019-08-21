package nasirov.yv.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheCleaner {

	@Value("${cache.userMAL.name}")
	private String userMALCacheName;

	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;

	private final CacheManager cacheManager;

	@Scheduled(cron = "${cache.cron.expression}")
	public void clearCache() {
		clearAndLog(cacheManager, userMALCacheName);
		clearAndLog(cacheManager, userMatchedAnimeCacheName);
	}

	private void clearAndLog(CacheManager cacheManager, String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		cache.clear();
		log.info("{} cleared", cacheName);
	}
}
