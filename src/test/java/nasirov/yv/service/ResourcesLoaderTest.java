package nasirov.yv.service;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashSet;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.service.context.LoadResourcesContextListener;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ResourcesLoader.class, AppConfiguration.class, LoadResourcesContextListener.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ResourcesLoaderTest extends AbstractTest {

	@MockBean
	private ReferencesManager referencesManager;

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void loadMultiSeasonsReferences() throws Exception {
		verify(referencesManager, times(1)).getMultiSeasonsReferences();
	}

	@Test
	public void loadAnimediaSearchInfoList() throws Exception {
		Set<AnimediaTitleSearchInfo> animediaSearchListFromResources = RoutinesIO
				.unmarshalFromResource(resourceAnimediaSearchList, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> animediaSearchListFromCache = cacheManager.getCache(animediaSearchListCacheName)
				.get(animediaSearchListCacheName, LinkedHashSet.class);
		assertEquals(animediaSearchListFromResources, animediaSearchListFromCache);
	}

}