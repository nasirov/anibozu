package nasirov.yv.service.impl;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.service.CacheCleanerServiceI;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
public class CacheCleanerService implements CacheCleanerServiceI {

	@Override
	@CacheEvict(cacheNames = "sse", key = "T(java.lang.String).valueOf(#malUser.hashCode())")
	public void clearSseCache(MALUser malUser) {
		log.info("Received request for eviction sse cache for [{}] by key [{}]", malUser, malUser.hashCode());
	}
}
