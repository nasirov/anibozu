package nasirov.yv.service.resources.loader;

import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class ResourcesLoaderConfiguration {

	@Bean("resourcesLoader")
	@ConditionalOnProperty(name = "application.services.resourcesLoader-enabled", havingValue = "true")
	public ResourcesLoader getResourcesLoader(ReferencesServiceI referencesManager, AnimediaServiceI animediaService) {
		return new ResourcesLoader(referencesManager, animediaService);
	}
}
