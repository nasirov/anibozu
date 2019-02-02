package nasirov.yv.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Created by Хикка on 27.01.2019.
 */
@Component
@Slf4j
public class CacheHelper {
	@Value("${cache.userMAL.name}")
	private String userMALCacheName;
	
	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;
	
	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;
	
	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;
	
	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;
	
	private CacheManager cacheManager;
	
	@Autowired
	public CacheHelper(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	//@Scheduled(fixedDelayString = "${cache.fixedDelay.in.milliseconds}")
	//@Scheduled(cron = "${cache.cron.expression}")
	public void clearCache() {
		// cacheManager.getCache(animediaSearchListCacheName).clear();
		cacheManager.getCache(userMALCacheName).clear();
		cacheManager.getCache(userMatchedAnimeCacheName).clear();
		cacheManager.getCache(matchedReferencesCacheName).clear();
		log.info("Clear " + userMALCacheName);
		log.info("Clear " + userMatchedAnimeCacheName);
		log.info("Clear " + matchedReferencesCacheName);
		//logger.info("Clear {}", userMatchedAnimeCacheName);
	}
}
