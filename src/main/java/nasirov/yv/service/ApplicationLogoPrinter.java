package nasirov.yv.service;

import nasirov.yv.service.annotation.PrintApplicationLogo;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private RoutinesIO routinesIO;
	
	@Autowired
	public ApplicationLogoPrinter(RoutinesIO routinesIO) {
		this.routinesIO = routinesIO;
	}
	
	@PrintApplicationLogo
	public void printApplicationLogo() {
		String logo = routinesIO.readFromResource(resourcesApplicationLogo);
		System.out.println(logo);
	}
}
