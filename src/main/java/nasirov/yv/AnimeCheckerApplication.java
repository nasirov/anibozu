package nasirov.yv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AnimeCheckerApplication {
	static {
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
	}
	
	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(AnimeCheckerApplication.class, args);
	}
}

