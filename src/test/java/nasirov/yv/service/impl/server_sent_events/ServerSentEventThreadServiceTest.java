package nasirov.yv.service.impl.server_sent_events;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.CacheProps.ConfigurableCacheProps;
import nasirov.yv.data.task.ServerSentEventThread;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
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
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerSentEventThreadServiceTest {

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
	private ServerSentEventThreadService sseActionService;

	private ServerSentEventThread cachedServerSentEventThread;

	@Before
	public void setUp() {
		cachedServerSentEventThread = new ServerSentEventThread(animeService, malService, new SseEmitter(), buildUserInputDto());
	}

	@Test
	public void shouldBuildSseAction() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		String cacheKey = userInputDto.getUsername();
		mockServices(cacheKey);
		SseEmitter sseEmitter = new SseEmitter();
		//when
		ServerSentEventThread serverSentEventThread = sseActionService.buildServerSentEventThread(sseEmitter, userInputDto);
		//then
		assertNotEquals(cachedServerSentEventThread, serverSentEventThread);
		assertFalse(cachedServerSentEventThread.getRunning()
				.get());
		verify(sseCache).put(cacheKey, serverSentEventThread);
	}

	private void mockServices(String cacheKey) {
		ConfigurableCacheProps sseCacheProps = buildSseCacheProps();
		doReturn(sseCacheProps).when(cacheProps)
				.getSse();
		doReturn(sseCache).when(cacheManager)
				.getCache(sseCacheProps.getName());
		doReturn(cachedServerSentEventThread).when(sseCache)
				.get(cacheKey, ServerSentEventThread.class);
	}

	private UserInputDto buildUserInputDto() {
		UserInputDto userInputDto = new UserInputDto();
		userInputDto.setUsername(TEST_ACC_FOR_DEV);
		userInputDto.setFanDubSources(Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME));
		return userInputDto;
	}

	private ConfigurableCacheProps buildSseCacheProps() {
		ConfigurableCacheProps sseCacheProps = new ConfigurableCacheProps();
		sseCacheProps.setName("sse");
		sseCacheProps.setTtl(5);
		sseCacheProps.setMaxEntityCount(10);
		return sseCacheProps;
	}
}