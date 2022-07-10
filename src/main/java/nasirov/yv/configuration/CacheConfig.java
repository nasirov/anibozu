package nasirov.yv.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.service.impl.common.CacheEventLogger;
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
	public Caffeine<Object, Object> sseCaffeineCache(CacheProps cacheProps, CacheEventLogger cacheEventLogger) {
		ConfigurableCacheProps cachePropsSse = cacheProps.getSse();
		return Caffeine.newBuilder()
				.maximumSize(cachePropsSse.getMaxSize())
				.expireAfterWrite(cachePropsSse.getTtl())
				.removalListener(cacheEventLogger);
	}

	@Bean
	public CacheManager cacheManager(Caffeine<Object, Object> sseCaffeineCache) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(sseCaffeineCache);
		return cacheManager;
	}
}
