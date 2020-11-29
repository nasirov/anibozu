package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Nasirov Yuriy
 */
public class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String ERROR_VIEW = "error";

	private static final String RESULT_VIEW = "result";

	private static final String[] VALID_FANDUBS = {"ANIMEDIA", "NINEANIME"};

	@Test
	public void shouldReturnResultView() {
		//given
		mockMalService(buildMalUserInfo(Lists.newArrayList(new MalTitle()), null));
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
	public void shouldReturnErrorViewWithErrorMessage() {
		//given
		String errorMsg = "Foo Bar";
		mockMalService(buildMalUserInfo(Collections.emptyList(), errorMsg));
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

	private void mockMalService(MalUserInfo malUserInfo) {
		doReturn(malUserInfo).when(malService)
				.getMalUserInfo(TEST_ACC_FOR_DEV);
	}

	private MalUserInfo buildMalUserInfo(List<MalTitle> malTitles, String errorMessage) {
		return MalUserInfo.builder()
				.username(TEST_ACC_FOR_DEV)
				.malTitles(malTitles)
				.errorMessage(errorMessage)
				.build();
	}

	@SneakyThrows
	private MvcResult getMvcResult(String username, String... fanDubSources) {
		return mockMvc.perform(get(RESULT_VIEW_PATH).queryParam("username", username)
				.queryParam("fanDubSources", fanDubSources))
				.andReturn();
	}
}