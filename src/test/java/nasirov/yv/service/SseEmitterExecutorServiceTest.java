package nasirov.yv.service;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.properties.SseProps;
import nasirov.yv.data.task.SseAction;
import nasirov.yv.service.impl.CacheCleanerService;
import nasirov.yv.service.impl.SseEmitterExecutorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class SseEmitterExecutorServiceTest {

	@Mock
	private ForkJoinPool commonPool;

	@Spy
	private CacheCleanerService cacheCleanerService = new CacheCleanerService();

	@Mock
	private SseActionServiceI sseActionService;

	@Mock
	private SseProps sseProps;

	@Mock
	private SseAction sseAction;

	@InjectMocks
	private SseEmitterExecutorService sseEmitterExecutorService;

	@Test
	public void buildAndExecuteSseEmitterOk() {
		MALUser malUser = buildMalUser();
		mockServices(malUser);
		SseEmitter sseEmitter = sseEmitterExecutorService.buildAndExecuteSseEmitter(malUser);
		checkTimeout(sseEmitter);
		verify(commonPool).execute(sseAction);
	}

	@Test
	public void onErrorConsumerOk() {
		MALUser malUser = buildMalUser();
		Consumer<Throwable> onErrorConsumer = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onErrorConsumer", malUser);
		assertNotNull(onErrorConsumer);
		onErrorConsumer.accept(new RuntimeException("Exception Message"));
		verify(cacheCleanerService).clearSseCache(malUser);
	}

	@Test
	public void onTimeoutCallbackOk() {
		MALUser malUser = buildMalUser();
		Runnable onTimeoutCallback = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onTimeoutCallback", malUser);
		assertNotNull(onTimeoutCallback);
		onTimeoutCallback.run();
		verify(cacheCleanerService, never()).clearSseCache(malUser);
	}

	@Test
	public void onCompletionCallbackOk() {
		MALUser malUser = buildMalUser();
		Runnable onCompletionCallback = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onCompletionCallback", malUser);
		assertNotNull(onCompletionCallback);
		onCompletionCallback.run();
		verify(cacheCleanerService).clearSseCache(malUser);
	}

	private MALUser buildMalUser() {
		MALUser malUser = new MALUser();
		malUser.setUsername(TEST_ACC_FOR_DEV);
		malUser.setFanDubSources(Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME));
		return malUser;
	}

	private void mockServices(MALUser malUser) {
		doReturn(1).when(sseProps)
				.getTimeoutInMin();
		doReturn(sseAction).when(sseActionService)
				.buildSseAction(any(SseEmitter.class), eq(malUser));
	}

	private void checkTimeout(SseEmitter sseEmitter) {
		Long timeout = sseEmitter.getTimeout();
		assertNotNull(timeout);
		assertEquals(60000, timeout.longValue());
	}
}