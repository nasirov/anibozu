package nasirov.yv.ac.service.impl;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Component
public class CacheEventLogger implements RemovalListener<Object, Object> {

	@Override
	public void onRemoval(@Nullable Object key, @Nullable Object value, @NonNull RemovalCause cause) {
		log.debug("Evicted a cache entry with key [{}] and cause [{}]", key, cause);
	}
}