package nasirov.yv.configuration;

import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
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
 * Created by nasirov.yv
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
	
	@Value("${cache.multiSeasonsReferences.name}")
	private String multiSeasonsReferencesCacheName;
	
	/**
	 * multiSeasonsReferencesCache-cache for multi seasons references from resources
	 * {@link ReferencesManager#getMultiSeasonsReferences}
	 * <p>
	 * sortedAnimediaSearchListCache-for sorted animedia titles(multi,single,announcements)
	 * {@link nasirov.yv.service.AnimediaService#getSortedForSeasonAnime,nasirov.yv.service.AnimediaService#getAnime}
	 * <p>
	 * userMALCache-for user watching titles(username-watching titles)
	 * {@link nasirov.yv.service.MALService#getWatchingTitles}
	 * <p>
	 * userMatchedAnimeCache-for user matched anime (single,multi) (after matchedReferencesCacheName)
	 * {@link SeasonAndEpisodeChecker#getMatchedAnime}
	 * <p>
	 * matchedReferencesCache-for updated user matched references(only multi) (before userMatchedAnimeCacheName)
	 * {@link nasirov.yv.controller.CheckResultController#handleNewUser}
	 * <p>
	 * currentlyUpdatedTitlesCache-for currently updated titles on animedia
	 * {@link nasirov.yv.service.AnimediaService#getCurrentlyUpdatedTitles}
	 * <p>
	 * animediaSearchListCache-for animedia search list
	 * {@link nasirov.yv.service.AnimediaService#getAnimediaSearchList}
	 *
	 * @return the cache manager
	 */
	@Bean("cacheManager")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new ConcurrentMapCache(multiSeasonsReferencesCacheName),
				new ConcurrentMapCache(sortedAnimediaSearchListCacheName),
				new ConcurrentMapCache(userMALCacheName),
				new ConcurrentMapCache(userMatchedAnimeCacheName),
				new ConcurrentMapCache(matchedReferencesCacheName),
				new ConcurrentMapCache(currentlyUpdatedTitlesCacheName),
				new ConcurrentMapCache(animediaSearchListCacheName)));
		return cacheManager;
	}
}
