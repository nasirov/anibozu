package nasirov.yv.configuration;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class CustomCacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent cacheEvent) {
		log.trace("CACHE EVENT type [{}], cache key[{}], old value is null:[{}], new value is null:[{}] ",
				cacheEvent.getType(),
				cacheEvent.getKey(),
				Objects.isNull(cacheEvent.getOldValue()),
				Objects.isNull(cacheEvent.getNewValue()));
	}
}