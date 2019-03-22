package nasirov.yv.controller;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.exception.WatchingTitlesNotFoundException;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parameter.MALRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.MALParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.MALService;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nasirov.yv.enums.MALAnimeStatus.WATCHING;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by nasirov.yv
 */
@ContextConfiguration(classes = {
		ResultController.class,
		AnimediaHTMLParser.class,
		MALParser.class,
		CacheManager.class,
		AppConfiguration.class,
		URLBuilder.class,
		MALRequestParametersBuilder.class,
		AnimediaRequestParametersBuilder.class,
		MethodValidationPostProcessor.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebMvcTest(ResultController.class)
public class ResultControllerTest extends AbstractTest {
	@MockBean
	private MALService malService;
	
	@MockBean
	private AnimediaService animediaService;
	
	@MockBean
	private ReferencesManager referencesManager;
	
	@MockBean
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;
	
	private List<UserMALTitleInfo> repoMock;
	
	private static final String USERNAME = "test";
	
	private static final String PATH = "/result";
	
	@Before
	public void setUp() {
		repoMock = new ArrayList<>();
		doAnswer(answer -> {
			repoMock.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> repoMock.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0).when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
	}
	
	@Test(expected = NestedServletException.class)
	public void checkResultUsernameLengthLessThen2Chars() throws Exception {
		mockMvc.perform(post(PATH)
				.param("username", ""));
	}
	
	@Test(expected = NestedServletException.class)
	public void checkResultUsernameLengthMoreThan16Chars() throws Exception {
		mockMvc.perform(post(PATH)
				.param("username", "moreThan16Chars!!!"));
	}
	
	@Test
	public void checkResultUsernameIsNotFound() throws Exception {
		doThrow(new MALUserAccountNotFoundException()).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains("MAL account " + USERNAME + " is not found"));
		checkTitleAndHeaderForError(content, USERNAME);
	}
	
	@Test
	public void checkResultWatchingTitlesNotFound() throws Exception {
		String errorMsg = "errorMsg";
		doThrow(new WatchingTitlesNotFoundException(errorMsg)).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains(errorMsg));
		checkTitleAndHeaderForError(content, USERNAME);
	}
	
	@Test
	public void checkResultUserAnimeListHasPrivateAccess() throws Exception {
		String errorMsg = "Anime list " + USERNAME + " has private access!";
		doThrow(new MALUserAnimeListAccessException()).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains(errorMsg));
		checkTitleAndHeaderForError(content, USERNAME);
	}
	
	@Test
	public void checkResultJsonAnimeListNotFound() throws Exception {
		String errorMsg = "The application supports only default mal anime list view with wrapped json data! Json anime list is not found for " + USERNAME;
		doThrow(new JSONNotFoundException()).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains(errorMsg));
		checkTitleAndHeaderForError(content, USERNAME);
	}
	
	@Test
	public void checkResultForNewUser() throws Exception {
		Set<UserMALTitleInfo> nofFound = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series",
				0, "testPoster", "testUrl");
		nofFound.add(notFoundAnime);
		doAnswer(invocation -> new ArrayList<>(nofFound)).when(notFoundAnimeOnAnimediaRepository).findAll();
		doReturn(nofFound).when(malService).getWatchingTitles(eq(USERNAME));
		doReturn(new ArrayList<>()).when(animediaService).getCurrentlyUpdatedTitles();
		cacheManager.getCache(animediaSearchListCacheName).put(animediaSearchListCacheName, new LinkedHashSet<>());
		doReturn(new LinkedHashSet<>()).when(referencesManager).getMultiSeasonsReferences();
		doReturn(new LinkedHashSet<>()).when(referencesManager).getMatchedReferences(anySet(), anySet());
		doReturn(getMatchedAnime()).when(seasonAndEpisodeChecker).getMatchedAnime(anySet(), anySet(), anySet(), eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		List<AnimediaMALTitleReferences> animediaMALTitleReferences = new ArrayList<>(getMatchedAnime());
		AnimediaMALTitleReferences fairyTail1 = animediaMALTitleReferences.get(1);
		AnimediaMALTitleReferences sao = animediaMALTitleReferences.get(0);
		checkFront(content, fairyTail1.getFinalUrl(), fairyTail1.getPosterUrl(), fairyTail1.getTitleOnMAL(),
				fairyTail1.getEpisodeNumberForWatch(), sao.getPosterUrl(), sao.getTitleOnMAL(), notFoundAnime.getAnimeUrl(), notFoundAnime.getPosterUrl(), notFoundAnime.getTitle(), USERNAME);
	}
	
	@Test
	public void checkResultForCachedUser() throws Exception {
		AnimediaMALTitleReferences sao1 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "1", "1", "sword art online", "1", "25",
				"10", "saoPosterUrl", "", "");
		AnimediaMALTitleReferences sao3 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "3", "1", "sword art online: alicization", "1", "25",
				"25", "saoPosterUrl", "saoFinalUrl", "25");
		AnimediaMALTitleReferences fairyTail1 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "1", "1", "fairy tail", "1", "175",
				"175", "ftPosterUrl", "ftFinalUrl", "2");
		Set<AnimediaMALTitleReferences> matchedAnime = new LinkedHashSet<>();
		matchedAnime.add(sao1);
		matchedAnime.add(fairyTail1);
		matchedAnime.add(sao3);
		Set<UserMALTitleInfo> nofFound = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series",
				0, "testPoster", "testUrl");
		nofFound.add(notFoundAnime);
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo sao = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "sword art online", 25, "", "");
		UserMALTitleInfo saoAlicization = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "sword art online: alicization", 25, "", "");
		watchingTitles.add(notFoundAnime);
		watchingTitles.add(sao);
		watchingTitles.add(saoAlicization);
		AnimediaMALTitleReferences sao1Updated = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "1", "1", "sword art online", "1", "25",
				"25", "saoPosterUrl", "", "");
		List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles = new ArrayList<>();
		checkCurrentlyUpdatedTitles.add(sao1Updated);
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		Cache matchedReferencesCache = cacheManager.getCache(matchedReferencesCacheName);
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		userMALCache.put(USERNAME, watchingTitles);//Set<UserMALTitleInfo>
		userMatchedAnimeCache.put(USERNAME, matchedAnime);//Set<AnimediaMALTitleReferences>
		currentlyUpdatedTitlesCache.put(currentlyUpdatedTitlesCacheName, new ArrayList<>());//List<AnimediaMALTitleReferences>
		matchedReferencesCache.put(USERNAME, new LinkedHashSet<>());//Set<AnimediaMALTitleReferences>
		doReturn(new ArrayList<>()).when(animediaService).getCurrentlyUpdatedTitles();
		doReturn(checkCurrentlyUpdatedTitles).when(animediaService).checkCurrentlyUpdatedTitles(anyList(), anyList());
		doReturn(true).when(malService).isWatchingTitlesUpdated(anySet(), anySet());
		doReturn(new LinkedHashSet<>()).when(malService).getWatchingTitles(eq(USERNAME));
		doAnswer(invocation -> new ArrayList<>(nofFound)).when(notFoundAnimeOnAnimediaRepository).findAll();
		MockHttpServletResponse response = mockMvc.perform(post(PATH)
				.param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertEquals(2, matchedAnime.size());
		assertEquals(0, matchedAnime.stream().filter(set -> set.getTitleOnMAL().equals(fairyTail1.getTitleOnMAL())).count());
		verify(referencesManager, times(1)).updateCurrentMax(anySet(), eq(sao1Updated));
		verify(seasonAndEpisodeChecker, times(1)).updateEpisodeNumberForWatchAndFinalUrl(eq(watchingTitles), eq(sao1Updated), anySet());
		checkFront(content, sao3.getFinalUrl(), sao3.getPosterUrl(), sao3.getTitleOnMAL(), sao3.getEpisodeNumberForWatch(),
				sao1.getPosterUrl(), sao1.getTitleOnMAL(), notFoundAnime.getAnimeUrl(), notFoundAnime.getPosterUrl(), notFoundAnime.getTitle(), USERNAME);
	}
	
	private void checkFront(String content,
							String availableFinalUrl,
							String availablePosterUrl,
							String availableTitle,
							String availableEpisodeNumberForWatch,
							String notAvailablePosterUrl,
							String notAvailableTitle,
							String notFoundMALUrl,
							String notFoundPosterUrl,
							String notFoundTitle,
							String username) {
		Pattern pattern = Pattern.compile("<title>Result for " + username + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + username + "</h1>\\R</header>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<p class=\"title\">New Episode Available</p>\\R\\s*<ul>\\R\\s*<a href=\"" + availableFinalUrl + "\" target=\"_blank\"><img src=\"" + availablePosterUrl +
				"\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + availableTitle + "\"\\R\\s+title=\"" + availableTitle + " episode " + availableEpisodeNumberForWatch + "\"");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<p class=\"title\">New Episode Not Available</p>\\R\\s*<ul>\\R\\s*<img src=\"" + notAvailablePosterUrl + "\" height=\"318\" width=\"225\" alt=\""
				+ notAvailableTitle + "\"\\R\\s+title=\"" + notAvailableTitle + "\" class=\"fade\"/>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<p class=\"title\">Not Found on Animedia</p>\\R\\s*<ul>\\R\\s*<a href=\"" + notFoundMALUrl + "\" target=\"_blank\"><img src=\""
				+ notFoundPosterUrl + "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + notFoundTitle + "\"\\R\\s+title=\"" + notFoundTitle + "\" class=\"fade\"/></a>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
	}
	
	private void checkTitleAndHeaderForError(String content, String username) {
		Pattern pattern = Pattern.compile("<title>Result for " + username + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + username + "</h1>\\R</header>");
		matcher = pattern.matcher(content);
		assertFalse(matcher.find());
	}
	
	private Set<AnimediaMALTitleReferences> getMatchedAnime() {
		Set<AnimediaMALTitleReferences> matchedAnime = new LinkedHashSet<>();
		AnimediaMALTitleReferences sao1 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "1", "1", "sword art online", "1", "25",
				"25", "saoPosterUrl", "", "");
		AnimediaMALTitleReferences fairyTail1 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "1", "1", "fairy tail", "1", "175",
				"175", "ftPosterUrl", "ftFinalUrl", "2");
		matchedAnime.add(sao1);
		matchedAnime.add(fairyTail1);
		return matchedAnime;
	}
}