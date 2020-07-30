package nasirov.yv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Created by nasirov.yv
 */
@EnableCaching
@SpringBootApplication
public class AnimeCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimeCheckerApplication.class, args);
	}
}

