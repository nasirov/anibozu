package nasirov.yv.cache;

import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CacheCleaner {

	@Value("${cache.userMAL.name}")
	private String userMALCacheName;

	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;

	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;

	private CacheManager cacheManager;

	@Autowired
	public CacheCleaner(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Scheduled(cron = "${cache.cron.expression}")
	public void clearCache() {
		clearAndLog(cacheManager, userMALCacheName);
		clearAndLog(cacheManager, userMatchedAnimeCacheName);
		clearAndLog(cacheManager, matchedReferencesCacheName);
	}

	private void clearAndLog(@NotNull CacheManager cacheManager, @NotNull String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		cache.clear();
		log.info("CLEAR " + cacheName);
	}
}
