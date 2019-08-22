package nasirov.yv.service.resources.loader;

import nasirov.yv.AbstractTest;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.ReferencesManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ResourcesLoaderConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class BaseResourcesLoaderTest extends AbstractTest {

	@MockBean
	protected ReferencesManager referencesManager;

	@MockBean
	protected AnimediaService animediaService;

}
