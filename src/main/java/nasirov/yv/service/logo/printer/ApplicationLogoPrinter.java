package nasirov.yv.service.logo.printer;

import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;

/**
 * Created by nasirov.yv
 */
public class ApplicationLogoPrinter {

	@Value("classpath:${resources.applicationLogo.name}")
	private Resource resourcesApplicationLogo;

	@EventListener(classes = ContextRefreshedEvent.class)
	public void printApplicationLogo() {
		String logo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		System.out.println(logo);
	}
}
