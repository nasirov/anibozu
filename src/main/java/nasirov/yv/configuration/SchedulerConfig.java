package nasirov.yv.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Created by nasirov.yv
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
	private static final int POOL_SIZE = 2;
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler overrideThreadPool = new ThreadPoolTaskScheduler();
		overrideThreadPool.setPoolSize(POOL_SIZE);
		overrideThreadPool.initialize();
		taskRegistrar.setTaskScheduler(overrideThreadPool);
	}
}
