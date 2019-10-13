package nasirov.yv.service.resources.loader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class ResourcesLoaderTest {

	private ResourcesLoaderI resourcesLoader;

	private ReferencesServiceI referencesManager;

	private AnimediaServiceI animediaService;

	@Before
	public void setUp() {
		referencesManager = mock(ReferencesServiceI.class);
		animediaService = mock(AnimediaServiceI.class);
		resourcesLoader = new ResourcesLoader(referencesManager, animediaService);
	}

	@Test
	public void loadMultiSeasonsReferencesEnabled() {
		resourcesLoader.loadAll();
		verify(referencesManager, times(1)).getMultiSeasonsReferences();
		verify(animediaService, times(1)).getAnimediaSearchListFromAnimedia();
		verify(animediaService, times(1)).getAnimediaSearchListFromGitHub();
	}
}
