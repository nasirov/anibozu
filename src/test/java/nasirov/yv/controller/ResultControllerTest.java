package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import wiremock.com.google.common.collect.Lists;

/**
 * Created by nasirov.yv
 */
public class ResultControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String SSE_PATH = "/sse";

	private static final String ERROR_VIEW = "error";

	private static final String RESULT_VIEW = "result";

	@Test
	public void checkResultInvalidUsername() throws Exception {
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		for (String invalidUsername : invalidUsernameArray) {
			mockMvc.perform(post(RESULT_VIEW_PATH).param("username", invalidUsername)
					.param("fanDubSources", "ANIMEDIA", "NINEANIME"))
					.andExpect(status().isBadRequest())
					.andReturn();
		}
	}

	@Test
	public void checkResultInvalidFanDubSources() throws Exception {
		String[] invalidUsernameArray = {"animedia", "nineanime", ""};
		for (String invalidUsername : invalidUsernameArray) {
			mockMvc.perform(post(RESULT_VIEW_PATH).param("username", TEST_ACC_FOR_DEV)
					.param("fanDubSources", invalidUsername))
					.andExpect(status().isBadRequest())
					.andReturn();
		}
	}

	@Test
	public void checkResultUsernameIsNotFound() {
		String errorMsg = "MAL account " + TEST_ACC_FOR_DEV.toLowerCase() + " is not found";
		mockMalService(new MALUserAccountNotFoundException(errorMsg));
		checkErrorView(errorMsg);
	}

	@Test
	public void checkResultWatchingTitlesNotFound() {
		String errorMsg = "Not found watching titles for " + TEST_ACC_FOR_DEV.toLowerCase() + " !";
		mockMalService(new WatchingTitlesNotFoundException(errorMsg));
		checkErrorView(errorMsg);
	}

	@Test
	public void checkResultUserAnimeListPrivateAccess() {
		String errorMsg = "Anime list " + TEST_ACC_FOR_DEV.toLowerCase() + " has private access!";
		mockMalService(new MALUserAnimeListAccessException(errorMsg));
		checkErrorView(errorMsg);
	}

	@Test
	public void resultOk() {
		mockMalServiceOk();
		checkResultView();
	}

	@Test
	public void sseOk() {
		mockSseEmitterExecutorServiceOk();
		assertEquals(HttpStatus.OK.value(),
				call(get(SSE_PATH)).getResponse()
						.getStatus());
	}

	private void mockSseEmitterExecutorServiceOk() {
		doReturn(new SseEmitter()).when(sseEmitterExecutorService)
				.buildAndExecuteSseEmitter(any(MALUser.class));
	}

	@SneakyThrows
	private void mockMalService(Exception toBeThrown) {
		doThrow(toBeThrown).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	@SneakyThrows
	private void mockMalServiceOk() {
		doReturn(Lists.newArrayList(new UserMALTitleInfo())).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	private void checkErrorView(String errorMsg) {
		MvcResult result = call(post(RESULT_VIEW_PATH));
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(ERROR_VIEW, modelAndView.getViewName());
		assertEquals(errorMsg,
				modelAndView.getModel()
						.get("errorMsg"));
	}

	private void checkResultView() {
		MvcResult result = call(post(RESULT_VIEW_PATH));
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(RESULT_VIEW, modelAndView.getViewName());
		Map<String, Object> model = modelAndView.getModel();
		assertEquals(TEST_ACC_FOR_DEV.toLowerCase(), model.get("username"));
		assertEquals(1, model.get("watchingTitlesSize"));
		assertEquals("ANIMEDIA,NINEANIME", model.get("fandubList"));
	}

	@SneakyThrows
	private MvcResult call(MockHttpServletRequestBuilder request) {
		return mockMvc.perform(request.param("username", TEST_ACC_FOR_DEV.toLowerCase())
				.param("fanDubSources", "ANIMEDIA", "NINEANIME"))
				.andReturn();
	}
}