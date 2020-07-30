package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.exception.mal.MalUserAccountNotFoundException;
import nasirov.yv.exception.mal.MalUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.fandub.dto.mal.MalTitle;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by nasirov.yv
 */
public class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String ERROR_VIEW = "error";

	private static final String RESULT_VIEW = "result";

	private static final String[] VALID_FANDUBS = {"ANIMEDIA", "NINEANIME"};

	@Test
	public void shouldReturnResultView() {
		//given
		mockMalServiceOk();
		//when
		MvcResult result = getMvcResult(TEST_ACC_FOR_DEV, VALID_FANDUBS);
		//then
		assertEquals(HttpStatus.OK.value(),
				result.getResponse()
						.getStatus());
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(RESULT_VIEW, modelAndView.getViewName());
		Map<String, Object> model = modelAndView.getModel();
		assertEquals(TEST_ACC_FOR_DEV, model.get("username"));
		assertEquals(1, model.get("watchingTitlesSize"));
		assertEquals("ANIMEDIA,NINEANIME", model.get("fandubList"));
	}

	@Test
	public void shouldReturn400ForInvalidUsernames() {
		//given
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		//when
		List<MvcResult> result = Arrays.stream(invalidUsernameArray)
				.map(x -> getMvcResult(x, VALID_FANDUBS))
				.collect(Collectors.toList());
		//then
		result.forEach(x -> assertEquals(HttpStatus.BAD_REQUEST.value(),
				x.getResponse()
						.getStatus()));
	}

	@Test
	public void shouldReturn400ForInvalidFandubSources() {
		//given
		String[] invalidFandubSourceArray = {"animedia", "nineanime", ""};
		//when
		List<MvcResult> result = Arrays.stream(invalidFandubSourceArray)
				.map(x -> getMvcResult(TEST_ACC_FOR_DEV, x))
				.collect(Collectors.toList());
		//then
		result.forEach(x -> assertEquals(HttpStatus.BAD_REQUEST.value(),
				x.getResponse()
						.getStatus()));
	}

	@Test
	public void shouldReturnErrorViewForMalUserAccountNotFoundException() {
		//given
		String errorMsg = "MAL account " + TEST_ACC_FOR_DEV + " is not found";
		mockMalService(new MalUserAccountNotFoundException(errorMsg));
		//when
		MvcResult result = getMvcResult(TEST_ACC_FOR_DEV, VALID_FANDUBS);
		//then
		assertEquals(HttpStatus.OK.value(),
				result.getResponse()
						.getStatus());
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(ERROR_VIEW, modelAndView.getViewName());
		assertEquals(errorMsg,
				modelAndView.getModel()
						.get("errorMsg"));
	}

	@Test
	public void shouldReturnErrorViewForWatchingTitlesNotFoundException() {
		//given
		String errorMsg = "Not found watching titles for " + TEST_ACC_FOR_DEV + " !";
		mockMalService(new WatchingTitlesNotFoundException(errorMsg));
		//when
		MvcResult result = getMvcResult(TEST_ACC_FOR_DEV, VALID_FANDUBS);
		//then
		assertEquals(HttpStatus.OK.value(),
				result.getResponse()
						.getStatus());
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(ERROR_VIEW, modelAndView.getViewName());
		assertEquals(errorMsg,
				modelAndView.getModel()
						.get("errorMsg"));
	}

	@Test
	public void shouldReturnErrorViewForMalUserAnimeListAccessException() {
		//given
		String errorMsg = "Anime list " + TEST_ACC_FOR_DEV + " has private access!";
		mockMalService(new MalUserAnimeListAccessException(errorMsg));
		//when
		MvcResult result = getMvcResult(TEST_ACC_FOR_DEV, VALID_FANDUBS);
		//then
		assertEquals(HttpStatus.OK.value(),
				result.getResponse()
						.getStatus());
		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals(ERROR_VIEW, modelAndView.getViewName());
		assertEquals(errorMsg,
				modelAndView.getModel()
						.get("errorMsg"));
	}

	@SneakyThrows
	private void mockMalService(Exception toBeThrown) {
		doThrow(toBeThrown).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@SneakyThrows
	private void mockMalServiceOk() {
		doReturn(Lists.newArrayList(new MalTitle())).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@SneakyThrows
	private MvcResult getMvcResult(String username, String... fanDubSources) {
		return mockMvc.perform(post(RESULT_VIEW_PATH).param("username", username)
				.param("fanDubSources", fanDubSources))
				.andReturn();
	}
}