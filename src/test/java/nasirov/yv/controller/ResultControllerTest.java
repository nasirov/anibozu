package nasirov.yv.controller;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedAnnouncementReference;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedRegularReference;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Before;
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

	@Before
	public void setUp() {
		notFoundAnimeOnAnimediaRepository.deleteAll();
	}

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
		doThrow(new MALUserAccountNotFoundException(errorMsg)).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultWatchingTitlesNotFound() throws Exception {
		String errorMsg = "errorMsg";
		doThrow(new WatchingTitlesNotFoundException(errorMsg)).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultForNewUser() throws Exception {
		Set<UserMALTitleInfo> nofFound = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"nevermind",
				0,
				MY_ANIME_LIST_STATIC_CONTENT_URL + NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL,
				"testUrl");
		nofFound.add(notFoundAnime);
		notFoundAnimeOnAnimediaRepository.saveAll(nofFound);
		doReturn(nofFound).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV.toLowerCase());
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getReferences();
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getMatchedReferences(anySet(), anySet());
		doReturn(getMatchedAnime()).when(seasonsAndEpisodesService)
				.getMatchedAnime(anySet(), anySet(), eq(TEST_ACC_FOR_DEV.toLowerCase()));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV.toLowerCase()))
				.andReturn()
				.getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		checkFront(content, buildNextEpisodeAvailableReference(), buildNextEpisodeIsNotAvailableReference(), notFoundAnime);
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
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + TEST_ACC_FOR_DEV.toLowerCase() + "</h1>\\R</header>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<p class=\"title\">New Episode Available</p>\\R\\s*<ul>\\R\\s*<a href=\"" + available.getFinalUrlForFront()
				+ "\" target=\"_blank\"><img src=\"" + available.getPosterUrlOnMAL() + "\" height=\"318\" width=\"225\"\\R\\s+alt=\""
				+ available.getTitleNameOnMAL() + "\"\\R\\s+title=\"" + available.getTitleNameOnMAL() + " episode "
				+ available.getEpisodeNumberForWatchForFront() + "\"");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<p class=\"title\">New Episode Not Available</p>\\R\\s*<ul>\\R\\s*<img src=\"" + notAvailable.getPosterUrlOnMAL()
				+ "\" height=\"318\" width=\"225\" alt=\"" + notAvailable.getTitleNameOnMAL() + "\"\\R\\s+title=\"" + notAvailable.getTitleNameOnMAL()
				+ "\" class=\"fade\"/>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile(
				"<p class=\"title\">Not Found on Animedia</p>\\R\\s*<ul>\\R\\s*<a href=\"" + notFound.getAnimeUrl() + "\" target=\"_blank\"><img src=\""
						+ notFound.getPosterUrl() + "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + notFound.getTitle() + "\"\\R\\s+title=\""
						+ notFound.getTitle() + "\" class=\"fade\"/></a>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
	}

	private void checkErrorResult(String errorMsg) throws Exception {
		MvcResult result = mockMvc.perform(post(PATH).param("username", TEST_ACC_FOR_DEV.toLowerCase()))
				.andExpect(view().name(ERROR_VIEW))
				.andReturn();
		MockHttpServletResponse response = result.getResponse();
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
}