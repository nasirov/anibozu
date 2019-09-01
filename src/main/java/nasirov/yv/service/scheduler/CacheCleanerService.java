package nasirov.yv.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.CacheNamesConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheCleanerService {

	private final CacheManager cacheManager;

	@Scheduled(cron = "${application.cron.cache-cron-expression}")
	public void clearCache() {
		clearAndLog(cacheManager, CacheNamesConstants.USER_MAL_CACHE);
		clearAndLog(cacheManager, CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
	}

	private void clearAndLog(CacheManager cacheManager, String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		cache.clear();
		log.info("{} cleared", cacheName);
	}
}
