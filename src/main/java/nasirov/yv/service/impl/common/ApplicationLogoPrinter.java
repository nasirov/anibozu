package nasirov.yv.service.impl.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StreamUtils.copyToString;

import lombok.SneakyThrows;
import nasirov.yv.service.ApplicationLogoPrinterI;
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

	@Value("classpath:${application.logo.name}")
	private Resource applicationLogoResource;

	@Override
	@SneakyThrows
	@EventListener(classes = ApplicationReadyEvent.class)
	public void printApplicationLogo() {
		String logo = copyToString(applicationLogoResource.getInputStream(), UTF_8);
		System.out.println(logo);
	}
}
