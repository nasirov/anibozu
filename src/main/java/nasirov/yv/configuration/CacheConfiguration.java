package nasirov.yv.configuration;

import java.util.Arrays;
import nasirov.yv.data.constants.CacheNamesConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

	/**
	 * sortedAnimediaSearchListCache-for sorted animedia titles(multi,single,announcements)
	 * {@link nasirov.yv.service.ResourcesService#getAnimeSortedByType}
	 * {@link nasirov.yv.service.ResourcesService#getAnimeSortedByTypeFromCache()}
	 * <p>
	 * userMALCache-for user watching titles(username-watching titles)
	 * {@link nasirov.yv.service.MALService#getWatchingTitles}
	 * <p>
	 * userMatchedAnimeCache-for user matched anime (single,multi) (after matchedReferencesCacheName)
	 * {@link nasirov.yv.service.SeasonsAndEpisodesService#getMatchedAnime}
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
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.USER_MAL_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE)));
		return cacheManager;
	}
}
