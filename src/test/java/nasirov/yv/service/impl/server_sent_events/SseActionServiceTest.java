package nasirov.yv.service.impl.server_sent_events;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import nasirov.yv.data.mal.MalUser;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.data.task.SseAction;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.impl.common.AnimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class SseActionServiceTest {

	@Mock
	private AnimeService animeService;

	@Mock
	private MalServiceI malService;

	@Mock
	private CacheProps cacheProps;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private Cache sseCache;

	@InjectMocks
	private SseActionService sseActionService;

	private SseAction cachedSseAction;

	@Before
	public void setUp() {
		cachedSseAction = new SseAction(animeService, malService, new SseEmitter(), buildMalUser());
	}

	@Test
	public void buildSseActionOk() {
		MalUser malUser = buildMalUser();
		String cacheKey = String.valueOf(malUser.hashCode());
		mockServices(cacheKey);
		SseAction sseAction = sseActionService.buildSseAction(new SseEmitter(), malUser);
		assertNotEquals(cachedSseAction, sseAction);
		assertFalse(cachedSseAction.getIsRunning()
				.get());
		verify(sseCache).put(cacheKey, sseAction);
	}

	private void mockServices(String cacheKey) {
		ConfigurableCacheProps sseCacheProps = buildSseCacheProps();
		doReturn(sseCacheProps).when(cacheProps)
				.getSse();
		doReturn(sseCache).when(cacheManager)
				.getCache(sseCacheProps.getName());
		doReturn(cachedSseAction).when(sseCache)
				.get(cacheKey, SseAction.class);
	}

	private MalUser buildMalUser() {
		MalUser malUser = new MalUser();
		malUser.setUsername(TEST_ACC_FOR_DEV);
		malUser.setFanDubSources(Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME));
		return malUser;
	}

	private ConfigurableCacheProps buildSseCacheProps() {
		ConfigurableCacheProps sseCacheProps = new ConfigurableCacheProps();
		sseCacheProps.setName("sse");
		sseCacheProps.setTtl(5);
		sseCacheProps.setMaxEntityCount(10);
		return sseCacheProps;
	}
}