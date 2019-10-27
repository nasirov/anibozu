package nasirov.yv.service.logo.printer;

import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@ConditionalOnProperty(name = "application.services.applicationLogoPrinter-enabled", havingValue = "true")
public class ApplicationLogoPrinter implements ApplicationLogoPrinterI {

	@Value("classpath:${application.resources.applicationLogo}")
	private Resource applicationLogoResource;

	@Override
	@EventListener(classes = ApplicationReadyEvent.class)
	public void printApplicationLogo() {
		String logo = RoutinesIO.readFromResource(applicationLogoResource);
		System.out.println(logo);
	}
}
