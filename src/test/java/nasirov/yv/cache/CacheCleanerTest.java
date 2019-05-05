package nasirov.yv.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.UserMALTitleInfo;
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
@SpringBootTest(classes = {CacheCleaner.class, AppConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CacheCleanerTest extends AbstractTest {

	@Autowired
	private CacheCleaner cacheCleaner;

	@Autowired
	private CacheManager cacheManager;


	@Test
	public void clearCache() throws Exception {
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		Cache matchedReferencesCache = cacheManager.getCache(matchedReferencesCacheName);
		String username = "test";
		userMALCache.put(username, new LinkedHashSet<UserMALTitleInfo>());
		userMatchedAnimeCache.put(username, new LinkedHashSet<AnimediaMALTitleReferences>());
		matchedReferencesCache.put(username, new LinkedHashSet<AnimediaMALTitleReferences>());
		assertNotNull(userMALCache.get(username, LinkedHashSet.class));
		assertNotNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
		assertNotNull(matchedReferencesCache.get(username, LinkedHashSet.class));
		cacheCleaner.clearCache();
		assertNull(userMALCache.get(username, LinkedHashSet.class));
		assertNull(userMatchedAnimeCache.get(username, LinkedHashSet.class));
		assertNull(matchedReferencesCache.get(username, LinkedHashSet.class));
	}

}