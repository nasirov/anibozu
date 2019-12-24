package nasirov.yv.configuration;

import static java.time.Duration.ofSeconds;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.http.ResponseEntity;
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
		ConfigurableCacheProps animediaApiCache = cacheProps.getAnimediaApi();
		cacheManager.createCache(animediaApiCache.getName(), createCacheConfiguration(animediaApiCache, ResponseEntity.class));
		ConfigurableCacheProps animedia = cacheProps.getAnimedia();
		cacheManager.createCache(animedia.getName(), createCacheConfiguration(animedia, Set.class));
		ConfigurableCacheProps malCache = cacheProps.getMal();
		cacheManager.createCache(malCache.getName(), createCacheConfiguration(malCache, Set.class));
		ConfigurableCacheProps githubCache = cacheProps.getGithub();
		cacheManager.createCache(githubCache.getName(), createCacheConfiguration(githubCache, Set.class));
	}

	private <T> Configuration<String, T> createCacheConfiguration(ConfigurableCacheProps configurableCacheProps, Class<T> valueClass) {
		return fromEhcacheCacheConfiguration(newCacheConfigurationBuilder(String.class,
				valueClass,
				ResourcePoolsBuilder.heap(configurableCacheProps.getMaxEntityCount())).withExpiry(timeToLiveExpiration(ofSeconds(configurableCacheProps.getTtl())))
				.add(newEventListenerConfiguration(customCacheEventLogger,
						Sets.newHashSet(EventType.CREATED, EventType.EXPIRED, EventType.EVICTED, EventType.REMOVED, EventType.UPDATED)).asynchronous()
						.unordered()
						.build()));
	}
}
