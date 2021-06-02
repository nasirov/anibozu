package nasirov.yv.service.impl.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashSet;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
public class CacheCleanerServiceTest extends AbstractTest {

	@Test
	public void shouldClearSseCache() {
		//given
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		Set<FanDubSource> fanDubSources = new LinkedHashSet<>();
		fanDubSources.add(FanDubSource.ANIMEDIA);
		fanDubSources.add(FanDubSource.NINEANIME);
		UserInputDto userInputDto = UserInputDto.builder()
				.username("foobar")
				.fanDubSources(fanDubSources)
				.build();
		Flux firstCachedFlux = mock(Flux.class);
		Flux secondCachedFlux = mock(Flux.class);
		String firstKey = "foobar:ANIMEDIA,NINEANIME";
		String secondKey = "foobar:ANIMEDIA";
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