package nasirov.yv.configuration;

import static java.time.Duration.ofSeconds;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

import com.google.common.collect.Sets;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.data.task.ServerSentEventThread;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Component
@RequiredArgsConstructor
public class CacheConfig implements JCacheManagerCustomizer {

	private final CacheProps cacheProps;

	private final CacheEventListener customCacheEventLogger;

	@Override
	public void customize(CacheManager cacheManager) {
		buildCache(cacheManager, cacheProps.getSse(), ServerSentEventThread.class);
	}

	private void buildCache(CacheManager cacheManager, ConfigurableCacheProps configurableCacheProps, Class<?> valueClass) {
		cacheManager.createCache(configurableCacheProps.getName(), createCacheConfiguration(configurableCacheProps, valueClass));
	}

	private Configuration<String, ?> createCacheConfiguration(ConfigurableCacheProps configurableCacheProps, Class<?> valueClass) {
		return fromEhcacheCacheConfiguration(newCacheConfigurationBuilder(String.class,
				valueClass,
				ResourcePoolsBuilder.heap(configurableCacheProps.getMaxEntityCount())).withExpiry(timeToLiveExpiration(ofSeconds(configurableCacheProps.getTtl())))
				.withService(newEventListenerConfiguration(customCacheEventLogger,
						Sets.newHashSet(EventType.CREATED, EventType.EXPIRED, EventType.EVICTED, EventType.REMOVED, EventType.UPDATED)).asynchronous()
						.unordered()
						.build()));
	}
}
