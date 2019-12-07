package nasirov.yv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Created by nasirov.yv
 */

@EnableRetry
@EnableFeignClients
@SpringBootApplication
public class AnimeCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimeCheckerApplication.class, args);
	}
}

