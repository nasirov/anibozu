package nasirov.yv.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
@TestPropertySource(properties = {"application.cache.sse.ttl=2s", "application.cache.sse.max-size=2"})
class CacheCleanerServiceTest extends AbstractTest {

	@Test
	void shouldEvictSseCacheByKey() {
		//given
		Cache sseCache = cacheManager.getCache(cacheProps.getSse().getName());
		assertNotNull(sseCache);
		UserInputDto userInputDto = buildUserInputDto();
		Flux firstCachedFlux = mock(Flux.class);
		Flux secondCachedFlux = mock(Flux.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":" + FanDubSource.ANIDUB.name();
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

	@Test
	@SneakyThrows
	void shouldEvictSseCacheBySize() {
		//given
		Cache sseCache = cacheManager.getCache(cacheProps.getSse().getName());
		assertNotNull(sseCache);
		Flux firstCachedFlux = mock(Flux.class);
		Flux secondCachedFlux = mock(Flux.class);
		Flux thirdCachedFlux = mock(Flux.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":" + FanDubSource.ANIDUB.name();
		String thirdKey = MAL_USERNAME + ":" + FanDubSource.ANILIBRIA.name();
		sseCache.put(firstKey, firstCachedFlux);
		sseCache.put(secondKey, secondCachedFlux);
		sseCache.put(thirdKey, thirdCachedFlux);
		assertEquals(firstCachedFlux, sseCache.get(firstKey, Flux.class));
		assertEquals(secondCachedFlux, sseCache.get(secondKey, Flux.class));
		assertEquals(thirdCachedFlux, sseCache.get(thirdKey, Flux.class));
		//when
		TimeUnit.MILLISECONDS.sleep(10);
		//then
		assertNull(sseCache.get(firstKey, Flux.class));
		assertEquals(secondCachedFlux, sseCache.get(secondKey, Flux.class));
		assertEquals(thirdCachedFlux, sseCache.get(thirdKey, Flux.class));
	}

	@Test
	@SneakyThrows
	void shouldEvictSseCacheByTtl() {
		//given
		ConfigurableCacheProps cachePropsSse = cacheProps.getSse();
		Cache sseCache = cacheManager.getCache(cachePropsSse.getName());
		assertNotNull(sseCache);
		Flux firstCachedFlux = mock(Flux.class);
		Flux secondCachedFlux = mock(Flux.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":" + FanDubSource.ANIDUB.name();
		sseCache.put(firstKey, firstCachedFlux);
		sseCache.put(secondKey, secondCachedFlux);
		assertEquals(firstCachedFlux, sseCache.get(firstKey, Flux.class));
		assertEquals(secondCachedFlux, sseCache.get(secondKey, Flux.class));
		//when
		TimeUnit.MILLISECONDS.sleep(cachePropsSse.getTtl().toMillis() + TimeUnit.SECONDS.toMillis(1));
		//then
		assertNull(sseCache.get(firstKey, Flux.class));
		assertNull(sseCache.get(secondKey, Flux.class));
	}
}