package nasirov.yv.service;

import nasirov.yv.service.annotation.PrintApplicationLogo;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@PrintApplicationLogo
public class ApplicationLogoPrinter {

	@Value("classpath:${resources.applicationLogo.name}")
	private Resource resourcesApplicationLogo;

	@PrintApplicationLogo
	public void printApplicationLogo() {
		String logo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		System.out.println(logo);
	}
}
