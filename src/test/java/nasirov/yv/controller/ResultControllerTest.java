package nasirov.yv.controller;

import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedAnnouncementReference;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedRegularReference;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * Created by nasirov.yv
 */
public class ResultControllerTest extends AbstractTest {

	private static final String PATH = "/result";

	private static final String ERROR_VIEW = "error";

	@Test
	public void checkResultInvalidUsername() throws Exception {
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		Pattern pattern = Pattern.compile("^[\\w-]{2,16}$");
		Matcher matcher;
		for (String invalidUsername : invalidUsernameArray) {
			matcher = pattern.matcher(invalidUsername);
			assertFalse(matcher.find());
			checkBindExceptionInMvcResult(mockMvc.perform(post(PATH).param("username", invalidUsername))
					.andExpect(status().isBadRequest())
					.andReturn());
		}
	}

	@Test
	public void checkResultUsernameIsNotFound() throws Exception {
		String errorMsg = "MAL account " + TEST_ACC_FOR_DEV.toLowerCase() + " is not found";
		mockMalService(new MALUserAccountNotFoundException(errorMsg));
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultWatchingTitlesNotFound() throws Exception {
		String errorMsg = "errorMsg";
		mockMalService(new WatchingTitlesNotFoundException(errorMsg));
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultForNewUser() throws Exception {
		mockServicesOk();
		MvcResult mvcResult = mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV.toLowerCase()))
				.andExpect(request().asyncStarted())
				.andReturn();
		MvcResult response = mockMvc.perform(asyncDispatch(mvcResult))
				.andReturn();
		String content = response.getResponse()
				.getContentAsString();
		checkFront(content, buildNextEpisodeAvailableReference(), buildNextEpisodeIsNotAvailableReference(), buildNotFoundOnAnimedia());
	}

	@SneakyThrows
	private void mockMalService(Exception toBeThrown) {
		doThrow(toBeThrown).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
	}

	@SneakyThrows
	private void mockServicesOk() {
		doReturn(getWatchingTitles()).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getReferences();
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getMatchedReferences(anySet(), anySet());
		doReturn(getMatchedAnime()).when(seasonsAndEpisodesService)
				.getMatchedAnime(anySet(), anySet(), eq(TEST_ACC_FOR_DEV.toLowerCase()));
	}

	private void checkBindExceptionInMvcResult(MvcResult mvcResult) {
		String validationErrorMsg = "Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)";
		assertTrue(mvcResult.getResolvedException() instanceof org.springframework.validation.BindException);
		BindException resolvedException = (BindException) mvcResult.getResolvedException();
		List<ObjectError> allErrors = resolvedException.getAllErrors();
		assertEquals(1, allErrors.size());
		assertEquals(validationErrorMsg,
				allErrors.stream()
						.findFirst()
						.get()
						.getDefaultMessage());
	}

	private void checkFront(String content, TitleReference available, TitleReference notAvailable, UserMALTitleInfo notFound) {
		Pattern pattern = Pattern.compile("<title>Result for " + TEST_ACC_FOR_DEV.toLowerCase() + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + TEST_ACC_FOR_DEV.toLowerCase() + "</h1>\\R\\s*</header>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<section class=\"nes-container with-title is-centered\">\\R\\s*<p class=\"title\">Available</p>\\R\\s*<div "
				+ "class=\"item\">\\R\\s*<img src=\"" + available.getPosterUrlOnMAL() + "\" alt=\"regular title " + "name\"\\R\\s*title=\""
				+ available.getTitleNameOnMAL() + " episode " + available.getEpisodeNumberForWatchForFront() + "\">\\R\\s*<div "
				+ "class=\"episode_overlay\">\\R\\s*<span>" + available.getEpisodeNumberForWatchForFront()
				+ "</span>\\R\\s*<span>episode</span>\\R\\s*</div>\\R\\s*<div class=\"overlay "
				+ "full_cover\">\\R\\s*<div class=\"link_holder\">\\R\\s*<a class=\"outbound_link animedia_background\" href=\""
				+ available.getFinalUrlForFront() + "\" target=\"_blank\"></a" + ">\\R\\s*</div>\\R\\s*</div>\\R\\s*</div>\\R\\s*</section>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<section class=\"nes-container with-title is-centered\">\\R\\s*<p class=\"title\">Not Available"
				+ "</p>\\R\\s*<div class=\"item\">\\R\\s*<img src=\"" + notAvailable.getPosterUrlOnMAL() + "\" alt=\""
				+ notAvailable.getTitleNameOnMAL() + "\" title=\"" + notAvailable.getTitleNameOnMAL() + "\">\\R\\s*</div>\\R\\s*</section>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<section class=\"nes-container with-title is-centered\">\\R\\s*<p class=\"title\">Not Found</p>\\R\\s*<div "
				+ "class=\"item\">\\R\\s*<img src=\"" + notFound.getPosterUrl() + "\" alt=\"" + notFound.getTitle() + "\" title=\"" + notFound.getTitle()
				+ "\"/>\\R\\s*<div " + "class=\"overlay full_cover\">\\R\\s*<a class=\"full_cover\" " + "href=\"" + notFound.getAnimeUrl() + "\" "
				+ "target=\"_blank\"></a>\\R\\s*</div>\\R\\s*</div>\\R\\s*</section>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
	}

	private void checkErrorResult(String errorMsg) throws Exception {
		MvcResult result = mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV.toLowerCase()))
				.andReturn();
		MockHttpServletResponse response = mockMvc.perform(asyncDispatch(result))
				.andExpect(view().name(ERROR_VIEW))
				.andReturn()
				.getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		Pattern pattern = Pattern.compile("<title>" + errorMsg + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<h2>" + errorMsg + "</h2>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
	}

	private Set<TitleReference> getMatchedAnime() {
		return Sets.newHashSet(buildNextEpisodeIsNotAvailableReference(), buildNextEpisodeAvailableReference());
	}

	private TitleReference buildNextEpisodeAvailableReference() {
		TitleReference regularReference = buildUpdatedRegularReference();
		regularReference.setEpisodeNumberForWatchForFront("2");
		regularReference.setFinalUrlForFront(
				ANIMEDIA_ONLINE_TV + regularReference.getUrlOnAnimedia() + "/" + regularReference.getDataListOnAnimedia() + "/"
						+ regularReference.getEpisodeNumberForWatchForFront());
		return regularReference;
	}

	private TitleReference buildNextEpisodeIsNotAvailableReference() {
		return buildUpdatedAnnouncementReference();
	}

	private Set<UserMALTitleInfo> getWatchingTitles() {
		Set<UserMALTitleInfo> userMALTitleInfo = new LinkedHashSet<>();
		userMALTitleInfo.add(buildUserMALTitleInfo(REGULAR_TITLE_NAME, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL));
		userMALTitleInfo.add(buildUserMALTitleInfo(ANNOUNCEMENT_TITLE_NAME, ANNOUNCEMENT_TITLE_URL, ANNOUNCEMENT_TITLE_MAL_ANIME_URL));
		userMALTitleInfo.add(buildNotFoundOnAnimedia());
		return userMALTitleInfo;
	}

	private UserMALTitleInfo buildNotFoundOnAnimedia() {
		return buildUserMALTitleInfo(NOT_FOUND_ON_ANIMEDIA_TITLE_NAME, NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL,
				NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL);
	}

	private UserMALTitleInfo buildUserMALTitleInfo(String titleName, String titlePosterUrl, String animeUrl) {
		return new UserMALTitleInfo(1, 0, titleName, MY_ANIME_LIST_STATIC_CONTENT_URL + titlePosterUrl, MY_ANIME_LIST_URL + animeUrl);
	}
}