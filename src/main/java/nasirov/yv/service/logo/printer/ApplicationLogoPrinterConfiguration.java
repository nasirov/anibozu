package nasirov.yv.service.logo.printer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class ApplicationLogoPrinterConfiguration {

	@Bean("applicationLogoPrinter")
	@ConditionalOnProperty(name = "service.applicationLogoPrinter.enabled", havingValue = "true")
	public ApplicationLogoPrinter getApplicationLogoPrinter() {
		return new ApplicationLogoPrinter();
	}
}
