package nasirov.yv.service;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.service.context.LoadResourcesContextListener;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ResourcesLoader.class, AppConfiguration.class, LoadResourcesContextListener.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unchecked")
@TestPropertySource(locations = "classpath:testSystem.properties")
public class ResourcesLoaderTest extends AbstractTest {

	@MockBean
	private ReferencesManager referencesManager;

	@MockBean
	private AnimediaService animediaService;


	@Test
	public void loadMultiSeasonsReferences() throws Exception {
		verify(referencesManager, times(1)).getMultiSeasonsReferences();
	}

	@Test
	public void loadAnimediaSearchInfoList() throws Exception {
		verify(animediaService, times(1)).getAnimediaSearchListFromAnimedia();
		verify(animediaService, times(1)).getAnimediaSearchListFromGitHub();
	}

}