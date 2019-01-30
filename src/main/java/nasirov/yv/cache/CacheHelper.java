package nasirov.yv.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Created by Хикка on 27.01.2019.
 */
@Component
public class CacheHelper {
	private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);
	
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
		System.out.println("Clear " + userMALCacheName);
		System.out.println("Clear " + userMatchedAnimeCacheName);
		System.out.println("Clear " + matchedReferencesCacheName);
		//logger.info("Clear {}", userMatchedAnimeCacheName);
	}
}
