package nasirov.yv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by nasirov.yv
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class AnimeCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimeCheckerApplication.class, args);
	}
}

