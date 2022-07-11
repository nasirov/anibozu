package nasirov.yv.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.service.impl.CacheEventLogger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nasirov Yuriy
 */
@Configuration
public class CacheConfig {

	@Bean
	public Caffeine<Object, Object> resultCache(CacheProps cacheProps, CacheEventLogger cacheEventLogger) {
		ConfigurableCacheProps cachePropsResult = cacheProps.getResult();
		return Caffeine.newBuilder()
				.maximumSize(cachePropsResult.getMaxSize())
				.expireAfterWrite(cachePropsResult.getTtl())
				.removalListener(cacheEventLogger);
	}

	@Bean
	public CacheManager cacheManager(Caffeine<Object, Object> resultCache) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(resultCache);
		return cacheManager;
	}
}
