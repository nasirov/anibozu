package nasirov.yv.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.service.scheduler.CacheCleaner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {CacheCleaner.class, CacheConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CacheCleanerTest extends AbstractTest {

	@Autowired
	private CacheCleaner cacheCleaner;

	@Autowired
	private CacheManager cacheManager;


	@Test
	public void clearCache() throws Exception {
		Cache userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		Cache userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		String username = "test";
		userMALCache.put(username, new LinkedHashSet<UserMALTitleInfo>());
		userMatchedAnimeCache.put(username, new LinkedHashSet<AnimediaMALTitleReferences>());
		assertNotNull(userMALCache.get(username, LinkedHashSet.class));
		assertNotNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
		cacheCleaner.clearCache();
		assertNull(userMALCache.get(username, LinkedHashSet.class));
		assertNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
	}

}