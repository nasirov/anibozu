package nasirov.yv.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */

public class CacheCleanerServiceTest extends AbstractTest {

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
}