package nasirov.yv.ab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Nasirov Yuriy
 */
@EnableCaching
@SpringBootApplication
public class AnibozuApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnibozuApplication.class, args);
	}
}

