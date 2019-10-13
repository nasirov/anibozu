package nasirov.yv.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.service.scheduler.CacheCleanerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Created by nasirov.yv
 */

public class CacheCleanerServiceTest extends AbstractTest {

	@Autowired
	private CacheCleanerService cacheCleanerService;

	@Autowired
	private CacheManager cacheManager;

	private Cache userMALCache;

	private Cache userMatchedAnimeCache;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		clearCache();
	}

	@After
	public void tearDown() {
		clearCache();
	}

	@Test
	public void clearCacheTestOk() {
		String username = "test";
		userMALCache.put(username, new LinkedHashSet<UserMALTitleInfo>());
		userMatchedAnimeCache.put(username, new LinkedHashSet<AnimediaMALTitleReferences>());
		assertNotNull(userMALCache.get(username, LinkedHashSet.class));
		assertNotNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
		cacheCleanerService.clearCache();
		assertNull(userMALCache.get(username, LinkedHashSet.class));
		assertNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
	}

	private void clearCache() {
		userMALCache.clear();
		userMatchedAnimeCache.clear();
	}

}