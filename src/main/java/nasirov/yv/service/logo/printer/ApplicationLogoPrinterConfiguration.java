package nasirov.yv.service.logo.printer;

import nasirov.yv.data.properties.ResourcesNames;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class ApplicationLogoPrinterConfiguration {

	@Bean("applicationLogoPrinter")
	@ConditionalOnProperty(name = "application.services.applicationLogoPrinter-enabled", havingValue = "true")
	public ApplicationLogoPrinter getApplicationLogoPrinter(ResourcesNames resourcesNames) {
		return new ApplicationLogoPrinter(resourcesNames);
	}
}
