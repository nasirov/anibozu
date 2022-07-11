package nasirov.yv.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.ResultDto;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Nasirov Yuriy
 */
@TestPropertySource(properties = {"application.cache.result.ttl=2s", "application.cache.result.max-size=2"})
class CacheTest extends AbstractTest {

	@Test
	@SneakyThrows
	void shouldEvictCacheBySize() {
		//given
		Cache resultCache = cacheManager.getCache(cacheProps.getResult().getName());
		assertNotNull(resultCache);
		ResultDto firstCachedResultDto = mock(ResultDto.class);
		ResultDto secondCachedResultDto = mock(ResultDto.class);
		ResultDto thirdCachedResultDto = mock(ResultDto.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":" + FandubSource.ANIDUB.name();
		String thirdKey = MAL_USERNAME + ":" + FandubSource.ANILIBRIA.name();
		resultCache.put(firstKey, firstCachedResultDto);
		resultCache.put(secondKey, secondCachedResultDto);
		resultCache.put(thirdKey, thirdCachedResultDto);
		//when
		TimeUnit.MILLISECONDS.sleep(10);
		//then
		assertNull(resultCache.get(firstKey, ResultDto.class));
		assertEquals(secondCachedResultDto, resultCache.get(secondKey, ResultDto.class));
		assertEquals(thirdCachedResultDto, resultCache.get(thirdKey, ResultDto.class));
	}

	@Test
	@SneakyThrows
	void shouldEvictCacheByTtl() {
		//given
		ConfigurableCacheProps titlesCacheProps = cacheProps.getResult();
		Cache resultCache = cacheManager.getCache(titlesCacheProps.getName());
		assertNotNull(resultCache);
		ResultDto firstCachedResultDto = mock(ResultDto.class);
		ResultDto secondCachedResultDto = mock(ResultDto.class);
		String firstKey = MAL_USERNAME + buildCacheKeyForUser();
		String secondKey = MAL_USERNAME + ":" + FandubSource.ANIDUB.name();
		resultCache.put(firstKey, firstCachedResultDto);
		resultCache.put(secondKey, secondCachedResultDto);
		assertEquals(firstCachedResultDto, resultCache.get(firstKey, ResultDto.class));
		assertEquals(secondCachedResultDto, resultCache.get(secondKey, ResultDto.class));
		//when
		TimeUnit.MILLISECONDS.sleep(titlesCacheProps.getTtl().toMillis() + TimeUnit.SECONDS.toMillis(1));
		//then
		assertNull(resultCache.get(firstKey, ResultDto.class));
		assertNull(resultCache.get(secondKey, ResultDto.class));
	}
}