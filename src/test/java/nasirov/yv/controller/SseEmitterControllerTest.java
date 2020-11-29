package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Nasirov Yuriy
 */
public class SseEmitterControllerTest extends AbstractTest {

	private static final String SSE_PATH = "/sse";

	@Test
	public void shouldReturn200() {
		//given
		mockSseEmitterExecutorServiceOk();
		//when
		MvcResult mvcResult = call();
		//then
		assertEquals(HttpStatus.OK.value(),
				mvcResult.getResponse()
						.getStatus());
	}

	private void mockSseEmitterExecutorServiceOk() {
		doReturn(new SseEmitter()).when(sseEmitterExecutorService)
				.buildAndExecuteSseEmitter(any(UserInputDto.class));
	}

	@SneakyThrows
	private MvcResult call() {
		return mockMvc.perform(get(SSE_PATH).param("username", TEST_ACC_FOR_DEV)
				.param("fanDubSources", "ANIMEDIA", "NINEANIME"))
				.andReturn();
	}
}