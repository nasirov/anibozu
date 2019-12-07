package nasirov.yv.configuration;

import com.google.common.collect.Lists;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.service.impl.AnimediaService;
import nasirov.yv.service.impl.MALService;
import nasirov.yv.service.impl.ResourcesService;
import nasirov.yv.service.impl.SeasonsAndEpisodesService;
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
public class CacheConfig {

	/**
	 * sortedAnimediaSearchListCache-for sorted animedia titles(multi,single,announcements)
	 * {@link ResourcesService#getAnimeSortedByType}
	 * {@link ResourcesService#getAnimeSortedByTypeFromCache()}
	 * <p>
	 * userMALCache-for user watching titles(username-watching titles)
	 * {@link MALService#getWatchingTitles}
	 * <p>
	 * userMatchedAnimeCache-for user matched anime (single,multi) (after matchedReferencesCacheName)
	 * {@link SeasonsAndEpisodesService#getMatchedAnime}
	 * <p>
	 * currentlyUpdatedTitlesCache-for currently updated titles on animedia
	 * {@link AnimediaService#getCurrentlyUpdatedTitles}
	 * <p>
	 *
	 * @return the cache manager
	 */
	@Bean("cacheManager")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Lists.newArrayList(new ConcurrentMapCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.USER_MAL_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE),
				new ConcurrentMapCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE)));
		return cacheManager;
	}
}
