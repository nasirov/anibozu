package nasirov.yv.service.resources.loader;

import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.ReferencesManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class ResourcesLoaderConfiguration {

	@Bean("resourcesLoader")
	@ConditionalOnProperty(name = "service.resourcesLoader.enabled", havingValue = "true")
	public ResourcesLoader getResourcesLoader(ReferencesManager referencesManager, AnimediaService animediaService) {
		return new ResourcesLoader(referencesManager, animediaService);
	}
}
