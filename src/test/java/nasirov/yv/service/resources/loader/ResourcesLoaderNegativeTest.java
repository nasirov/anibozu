package nasirov.yv.service.resources.loader;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "service.resourcesLoader.enabled=false")
public class ResourcesLoaderNegativeTest extends BaseResourcesLoaderTest {

	@Test
	public void loadMultiSeasonsReferencesDisabled() throws Exception {
		verify(referencesManager, never()).getMultiSeasonsReferences();
		verify(animediaService, never()).getAnimediaSearchListFromAnimedia();
		verify(animediaService, never()).getAnimediaSearchListFromGitHub();
	}
}
