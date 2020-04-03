package nasirov.yv.configuration;

import static java.time.Duration.ofSeconds;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.data.task.SseAction;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@RequiredArgsConstructor
public class CacheConfig implements JCacheManagerCustomizer {

	private final CacheProps cacheProps;

	private final CacheEventListener customCacheEventLogger;

	@Override
	public void customize(CacheManager cacheManager) {
		buildCache(cacheManager, cacheProps.getDataListInfo(), ArrayList.class);
		buildCache(cacheManager, cacheProps.getMal(), ArrayList.class);
		buildCache(cacheManager, cacheProps.getGithub(), HashMap.class);
		buildCache(cacheManager, cacheProps.getSse(), SseAction.class);
	}

	private void buildCache(CacheManager cacheManager, ConfigurableCacheProps configurableCacheProps, Class<?> valueClass) {
		cacheManager.createCache(configurableCacheProps.getName(), createCacheConfiguration(configurableCacheProps, valueClass));
	}

	private Configuration<String, ?> createCacheConfiguration(ConfigurableCacheProps configurableCacheProps, Class<?> valueClass) {
		return fromEhcacheCacheConfiguration(newCacheConfigurationBuilder(String.class,
				valueClass,
				ResourcePoolsBuilder.heap(configurableCacheProps.getMaxEntityCount())).withExpiry(timeToLiveExpiration(ofSeconds(configurableCacheProps.getTtl())))
				.add(newEventListenerConfiguration(customCacheEventLogger,
						Sets.newHashSet(EventType.CREATED, EventType.EXPIRED, EventType.EVICTED, EventType.REMOVED, EventType.UPDATED)).asynchronous()
						.unordered()
						.build()));
	}
}
