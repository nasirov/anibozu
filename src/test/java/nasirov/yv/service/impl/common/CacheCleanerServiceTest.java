package nasirov.yv.service.impl.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Sets;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.task.ServerSentEventThread;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.junit.Test;
import org.springframework.cache.Cache;

/**
 * @author Nasirov Yuriy
 */
public class CacheCleanerServiceTest extends AbstractTest {

	@Test
	public void shouldClearSseCache() {
		//given
		Cache sseCache = cacheManager.getCache("sse");
		assertNotNull(sseCache);
		String username = "foobar";
		UserInputDto userInputDto = UserInputDto.builder()
				.username(username)
				.fanDubSources(Sets.newHashSet(FanDubSource.ANIMEDIA))
				.build();
		ServerSentEventThread cachedSse = mock(ServerSentEventThread.class);
		sseCache.put(username, cachedSse);
		assertEquals(cachedSse, sseCache.get(username, ServerSentEventThread.class));
		//when
		cacheCleanerService.clearSseCache(userInputDto);
		//then
		assertNull(sseCache.get(username, ServerSentEventThread.class));
	}
}