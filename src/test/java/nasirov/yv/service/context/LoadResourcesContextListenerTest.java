package nasirov.yv.service.context;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.ResourcesLoader;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ResourcesLoader.class, AppConfiguration.class, LoadResourcesContextListener.class,
		LoadResourcesContextListenerTest.ReferencesManagerPreTestConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:testSystem.properties")
public class LoadResourcesContextListenerTest extends AbstractTest {

	@Value("${service.resourcesLoader.enabled}")
	private Boolean isResourcesLoaderEnabled;

	@Autowired
	private ReferencesManager referencesManager;

	@MockBean
	private AnimediaService animediaService;

	@Test(expected = RuntimeException.class)
	public void onApplicationEventTestException() {
		assertNull(referencesManager.getMultiSeasonsReferences());
	}

	public static class ReferencesManagerPreTestConfig {

		@Bean
		public ReferencesManager getMockedReferencesManager() {
			ReferencesManager mock = mock(ReferencesManager.class);
			doThrow(RuntimeException.class).when(mock).getMultiSeasonsReferences();
			return mock;
		}
	}
}
