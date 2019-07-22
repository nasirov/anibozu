package nasirov.yv.configuration;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by nasirov.yv
 */
@Configuration
@PropertySource(value = "classpath:system.properties")
@EnableCaching
public class AppConfiguration {

	@Value("${cache.userMAL.name}")
	private String userMALCacheName;

	@Value("${cache.sortedAnimediaSearchList.name}")
	private String sortedAnimediaSearchListCacheName;

	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;

	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;

	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;


	/**
	 * sortedAnimediaSearchListCache-for sorted animedia titles(multi,single,announcements)
	 * {@link nasirov.yv.service.AnimediaService#getAnimeSortedByType ,nasirov.yv.service.AnimediaService#getAnimeSortedByTypeFromResources}
	 * <p>
	 * userMALCache-for user watching titles(username-watching titles)
	 * {@link nasirov.yv.service.MALService#getWatchingTitles}
	 * <p>
	 * userMatchedAnimeCache-for user matched anime (single,multi) (after matchedReferencesCacheName)
	 * {@link nasirov.yv.service.SeasonAndEpisodeChecker#getMatchedAnime}
	 * <p>
	 * matchedReferencesCache-for updated user matched references(only multi) (before userMatchedAnimeCacheName)
	 * {@link nasirov.yv.controller.ResultController#handleNewUser}
	 * <p>
	 * currentlyUpdatedTitlesCache-for currently updated titles on animedia
	 * {@link nasirov.yv.service.AnimediaService#getCurrentlyUpdatedTitles}
	 * <p>
	 *
	 * @return the cache manager
	 */
	@Bean("cacheManager")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(sortedAnimediaSearchListCacheName),
				new ConcurrentMapCache(userMALCacheName),
				new ConcurrentMapCache(userMatchedAnimeCacheName),
				new ConcurrentMapCache(matchedReferencesCacheName), new ConcurrentMapCache(currentlyUpdatedTitlesCacheName)));
		return cacheManager;
	}
}
