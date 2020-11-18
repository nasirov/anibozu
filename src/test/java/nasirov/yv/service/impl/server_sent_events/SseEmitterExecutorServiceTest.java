package nasirov.yv.service.impl.server_sent_events;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.SseProps;
import nasirov.yv.data.task.ServerSentEventThread;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.ServerSentEventThreadServiceI;
import nasirov.yv.service.impl.common.CacheCleanerService;
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
	private ExecutorService executorService;

	@Spy
	private CacheCleanerService cacheCleanerService = new CacheCleanerService();

	@Mock
	private ServerSentEventThreadServiceI sseActionService;

	@Mock
	private SseProps sseProps;

	@Mock
	private ServerSentEventThread serverSentEventThread;

	@InjectMocks
	private SseEmitterExecutorService sseEmitterExecutorService;

	@Test
	public void shouldBuildAndExecuteSseEmitter() {
		//given
		UserInputDto userInputDto = buildMalUser();
		mockServices(userInputDto);
		//when
		SseEmitter sseEmitter = sseEmitterExecutorService.buildAndExecuteSseEmitter(userInputDto);
		//then
		checkTimeout(sseEmitter);
		verify(executorService).execute(serverSentEventThread);
	}

	@Test
	public void shouldReturnOnErrorConsumer() {
		//given
		UserInputDto userInputDto = buildMalUser();
		//when
		Consumer<Throwable> onErrorConsumer = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onErrorConsumer", userInputDto);
		//then
		assertNotNull(onErrorConsumer);
		onErrorConsumer.accept(new RuntimeException("Exception Message"));
		verify(cacheCleanerService).clearSseCache(userInputDto);
	}

	@Test
	public void shouldReturnOnTimeoutCallback() {
		//given
		UserInputDto userInputDto = buildMalUser();
		//when
		Runnable onTimeoutCallback = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onTimeoutCallback", userInputDto);
		//then
		assertNotNull(onTimeoutCallback);
		onTimeoutCallback.run();
		verify(cacheCleanerService, never()).clearSseCache(userInputDto);
	}

	@Test
	public void shouldReturnOnCompletionCallback() {
		//given
		UserInputDto userInputDto = buildMalUser();
		//when
		Runnable onCompletionCallback = ReflectionTestUtils.invokeMethod(sseEmitterExecutorService, "onCompletionCallback", userInputDto);
		//then
		assertNotNull(onCompletionCallback);
		onCompletionCallback.run();
		verify(cacheCleanerService).clearSseCache(userInputDto);
	}

	private UserInputDto buildMalUser() {
		UserInputDto userInputDto = new UserInputDto();
		userInputDto.setUsername(TEST_ACC_FOR_DEV);
		userInputDto.setFanDubSources(Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME));
		return userInputDto;
	}

	private void mockServices(UserInputDto userInputDto) {
		doReturn(1).when(sseProps)
				.getTimeoutInMin();
		doReturn(serverSentEventThread).when(sseActionService)
				.buildServerSentEventThread(any(SseEmitter.class), eq(userInputDto));
	}

	private void checkTimeout(SseEmitter sseEmitter) {
		Long timeout = sseEmitter.getTimeout();
		assertNotNull(timeout);
		assertEquals(60000, timeout.longValue());
	}
}