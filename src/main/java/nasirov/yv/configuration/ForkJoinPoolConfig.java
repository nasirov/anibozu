package nasirov.yv.configuration;

import java.util.concurrent.ForkJoinPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class ForkJoinPoolConfig {

	@Bean
	public ForkJoinPool commonPool() {
		return ForkJoinPool.commonPool();
	}
}
