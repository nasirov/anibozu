package nasirov.yv.service.logo.printer;

import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.util.RoutinesIO;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public class ApplicationLogoPrinter {

	private final ResourcesNames resourcesNames;

	@EventListener(classes = ContextRefreshedEvent.class)
	public void printApplicationLogo() {
		String applicationLogo = "classpath:" + resourcesNames.getApplicationLogo();
		String logo = RoutinesIO.readFromFile(applicationLogo);
		System.out.println(logo);
	}
}
