package nasirov.yv.service.logo.printer;

import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.util.RoutinesIO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.applicationLogoPrinter-enabled", havingValue = "true")
public class ApplicationLogoPrinter implements ApplicationLogoPrinterI {

	private final ResourcesNames resourcesNames;

	@Override
	@EventListener(classes = ApplicationReadyEvent.class)
	public void printApplicationLogo() {
		String applicationLogo = "classpath:" + resourcesNames.getApplicationLogo();
		String logo = RoutinesIO.readFromFile(applicationLogo);
		System.out.println(logo);
	}
}
