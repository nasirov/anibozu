package nasirov.yv.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * Created by Хикка on 21.01.2019.
 */
@Configuration
@PropertySource(value = "classpath:system.properties")
@EnableCaching
@EnableScheduling
public class AppConfiguration {
	@Value("${cache.userMAL.name}")
	private String userMALCacheName;
	
	@Value("${cache.sortedAnimediaSearchList.name}")
	private String sortedAnimediaSearchListCacheName;
	
	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;
	
	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;
	
	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;
	
	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;
	
	@Bean("cacheManager")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new ConcurrentMapCache("multiSeasonsReferences"),
				new ConcurrentMapCache(sortedAnimediaSearchListCacheName),
				new ConcurrentMapCache(userMALCacheName),
				new ConcurrentMapCache(userMatchedAnimeCacheName),
				new ConcurrentMapCache(matchedReferencesCacheName),
				new ConcurrentMapCache(currentlyUpdatedTitlesCacheName),
				new ConcurrentMapCache(animediaSearchListCacheName)));
		return cacheManager;
	}
}
