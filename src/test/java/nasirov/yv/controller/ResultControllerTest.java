package nasirov.yv.controller;

import static nasirov.yv.data.constants.FanDubSource.ANIMEDIA;
import static nasirov.yv.data.constants.FanDubSource.NINEANIME;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Created by nasirov.yv
 */
public class ResultControllerTest extends AbstractTest {

	private static final String PATH = "/result";

	private static final String ERROR_VIEW = "error";

	private static final String RESULT_VIEW = "result";

	@Test
	public void checkResultInvalidUsername() throws Exception {
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		for (String invalidUsername : invalidUsernameArray) {
			mockMvc.perform(post(PATH).param("username", invalidUsername)
					.param("fanDubSources", "ANIMEDIA", "NINEANIME"))
					.andExpect(status().isBadRequest())
					.andReturn();
		}
	}

	@Test
	public void checkResultInvalidFanDubSources() throws Exception {
		String[] invalidUsernameArray = {"animedia", "nineanime", ""};
		for (String invalidUsername : invalidUsernameArray) {
			mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV)
					.param("fanDubSources", invalidUsername))
					.andExpect(status().isBadRequest())
					.andReturn();
		}
	}

	@Test
	public void checkResultUsernameIsNotFound() {
		String errorMsg = "MAL account " + TEST_ACC_FOR_DEV.toLowerCase() + " is not found";
		mockMalService(new MALUserAccountNotFoundException(errorMsg));
		checkResult(ERROR_VIEW);
	}

	@Test
	public void checkResultWatchingTitlesNotFound() {
		String errorMsg = "errorMsg";
		mockMalService(new WatchingTitlesNotFoundException(errorMsg));
		checkResult(ERROR_VIEW);
	}

	@Test
	public void checkResultForNewUser() {
		mockServicesOk();
		checkResult(RESULT_VIEW);
	}

	@SneakyThrows
	private void mockMalService(Exception toBeThrown) {
		doThrow(toBeThrown).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	@SneakyThrows
	private void mockServicesOk() {
		Set<UserMALTitleInfo> watchingTitles = Sets.newHashSet();
		doReturn(watchingTitles).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
		doReturn(Sets.newHashSet()).when(animeService)
				.getAnime(Sets.newHashSet(ANIMEDIA, NINEANIME), watchingTitles);
	}

	@SneakyThrows
	private void checkResult(String viewName) {
		MvcResult result = mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV.toLowerCase())
				.param("fanDubSources", "ANIMEDIA", "NINEANIME"))
				.andReturn();
		mockMvc.perform(asyncDispatch(result))
				.andExpect(view().name(viewName))
				.andExpect(status().isOk());
	}
}