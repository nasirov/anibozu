package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import wiremock.com.google.common.collect.Lists;

/**
 * Created by nasirov.yv
 */
public class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String ERROR_VIEW = "error";

	private static final String RESULT_VIEW = "result";

	private static final String[] VALID_FANDUBS = {"ANIMEDIA", "NINEANIME"};

	@Test
	public void checkResultInvalidUsername() {
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		for (String invalidUsername : invalidUsernameArray) {
			call(HttpStatus.BAD_REQUEST, invalidUsername, VALID_FANDUBS);
		}
	}

	@Test
	public void checkResultInvalidFanDubSources() {
		String[] invalidFandubSourceArray = {"animedia", "nineanime", ""};
		for (String invalidFandubSource : invalidFandubSourceArray) {
			call(HttpStatus.BAD_REQUEST, TEST_ACC_FOR_DEV.toLowerCase(), invalidFandubSource);
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

	@SneakyThrows
	private void mockMalService(Exception toBeThrown) {
		doThrow(toBeThrown).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	@SneakyThrows
	private void mockMalServiceOk() {
		doReturn(Lists.newArrayList(new MalTitle())).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	private void checkErrorView(String errorMsg) {
		MvcResult result = call(HttpStatus.OK, TEST_ACC_FOR_DEV.toLowerCase(), VALID_FANDUBS);
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(ERROR_VIEW, modelAndView.getViewName());
		assertEquals(errorMsg,
				modelAndView.getModel()
						.get("errorMsg"));
	}

	private void checkResultView() {
		MvcResult result = call(HttpStatus.OK, TEST_ACC_FOR_DEV.toLowerCase(), VALID_FANDUBS);
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(RESULT_VIEW, modelAndView.getViewName());
		Map<String, Object> model = modelAndView.getModel();
		assertEquals(TEST_ACC_FOR_DEV.toLowerCase(), model.get("username"));
		assertEquals(1, model.get("watchingTitlesSize"));
		assertEquals("ANIMEDIA,NINEANIME", model.get("fandubList"));
	}

	@SneakyThrows
	private MvcResult call(HttpStatus expectedStatus, String username, String... fanDubSources) {
		return mockMvc.perform(post(RESULT_VIEW_PATH).param("username", username)
				.param("fanDubSources", fanDubSources))
				.andExpect(status().is(expectedStatus.value()))
				.andReturn();
	}
}