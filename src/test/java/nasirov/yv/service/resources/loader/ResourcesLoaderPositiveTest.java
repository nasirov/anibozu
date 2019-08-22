package nasirov.yv.service.resources.loader;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.ReferencesManager;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ResourcesLoaderConfiguration.class, CacheConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unchecked")
@TestPropertySource(properties = "service.resourcesLoader.enabled=true")
public class ResourcesLoaderPositiveTest extends AbstractTest {

	@MockBean
	protected ReferencesManager referencesManager;

	@MockBean
	protected AnimediaService animediaService;

	@Test
	public void loadMultiSeasonsReferencesEnabled() throws Exception {
		verify(referencesManager, times(1)).getMultiSeasonsReferences();
		verify(animediaService, times(1)).getAnimediaSearchListFromAnimedia();
		verify(animediaService, times(1)).getAnimediaSearchListFromGitHub();
	}

}