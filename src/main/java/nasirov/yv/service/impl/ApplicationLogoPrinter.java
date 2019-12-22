package nasirov.yv.service.impl;

import lombok.RequiredArgsConstructor;
import nasirov.yv.service.ApplicationLogoPrinterI;
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
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.applicationLogoPrinter-enabled", havingValue = "true")
public class ApplicationLogoPrinter implements ApplicationLogoPrinterI {

	private final RoutinesIO routinesIO;

	@Value("classpath:${application.resources.applicationLogo}")
	private Resource applicationLogoResource;

	@Override
	@EventListener(classes = ApplicationReadyEvent.class)
	public void printApplicationLogo() {
		String logo = routinesIO.readFromResource(applicationLogoResource);
		System.out.println(logo);
	}
}
