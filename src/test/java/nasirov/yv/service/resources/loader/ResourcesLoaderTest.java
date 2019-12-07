package nasirov.yv.service.resources.loader;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */

@RunWith(MockitoJUnitRunner.class)
public class ResourcesLoaderTest {

	@Mock
	private ReferencesServiceI referencesManager;

	@Mock
	private AnimediaServiceI animediaService;

	@InjectMocks
	private ResourcesLoader resourcesLoader;


	@Test
	public void loadMultiSeasonsReferencesEnabled() {
		resourcesLoader.loadAll();
		verify(referencesManager, times(1)).getMultiSeasonsReferences();
		verify(animediaService, times(1)).getAnimediaSearchListFromAnimedia();
		verify(animediaService, times(1)).getAnimediaSearchListFromGitHub();
	}
}
