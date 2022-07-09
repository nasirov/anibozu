package nasirov.yv.service.impl.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
class CacheCleanerServiceTest extends AbstractTest {

	@Test
	void shouldClearSseCache() {
		//given
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		UserInputDto userInputDto = buildUserInputDto();
		Flux firstCachedFlux = mock(Flux.class);
		Flux secondCachedFlux = mock(Flux.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":ANIDUB";
		sseCache.put(firstKey, firstCachedFlux);
		sseCache.put(secondKey, secondCachedFlux);
		assertEquals(firstCachedFlux, sseCache.get(firstKey, Flux.class));
		assertEquals(secondCachedFlux, sseCache.get(secondKey, Flux.class));
		//when
		cacheCleanerService.clearSseCache(userInputDto);
		//then
		assertNull(sseCache.get(firstKey, Flux.class));
		assertEquals(secondCachedFlux, sseCache.get(secondKey, Flux.class));
	}
}