package nasirov.yv.controller;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.TestConstants.FAIRY_TAIL_ROOT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * Created by nasirov.yv
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ResultControllerTest {

	private static final String USERNAME = "test";

	private static final String PATH = "/result";

	private static final String ERROR_VIEW = "error";

	@MockBean
	private MALServiceI malService;

	@MockBean
	private AnimediaServiceI animediaService;

	@MockBean
	private ReferencesServiceI referencesService;

	@MockBean
	private SeasonsAndEpisodesServiceI seasonsAndEpisodesService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@Autowired
	private MockMvc mockMvc;

	private Cache userMALCache;

	private Cache userMatchedAnimeCache;

	private Cache currentlyUpdatedTitlesCache;

	@Before
	public void setUp() {
		userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		currentlyUpdatedTitlesCache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		notFoundAnimeOnAnimediaRepository.deleteAll();
	}

	@After
	public void tearDown() {
		userMALCache.clear();
		userMatchedAnimeCache.clear();
		currentlyUpdatedTitlesCache.clear();
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
		String errorMsg = "MAL account " + USERNAME + " is not found";
		doThrow(new MALUserAccountNotFoundException(errorMsg)).when(malService)
				.getWatchingTitles(eq(USERNAME));
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultWatchingTitlesNotFound() throws Exception {
		String errorMsg = "errorMsg";
		doThrow(new WatchingTitlesNotFoundException(errorMsg)).when(malService)
				.getWatchingTitles(eq(USERNAME));
		checkErrorResult(errorMsg);
	}

	@Test
	public void checkResultForNewUser() throws Exception {
		Set<UserMALTitleInfo> nofFound = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series", 0, "testPoster", "testUrl");
		nofFound.add(notFoundAnime);
		notFoundAnimeOnAnimediaRepository.saveAll(nofFound);
		doReturn(nofFound).when(malService)
				.getWatchingTitles(USERNAME);
		doReturn(new ArrayList<>()).when(animediaService)
				.getCurrentlyUpdatedTitles();
		doReturn(new LinkedHashSet<>()).when(animediaService)
				.getAnimediaSearchListFromGitHub();
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getMultiSeasonsReferences();
		doReturn(new LinkedHashSet<>()).when(referencesService)
				.getMatchedReferences(anySet(), anySet());
		doReturn(getMatchedAnime()).when(seasonsAndEpisodesService)
				.getMatchedAnime(anySet(), anySet(), anySet(), eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME))
				.andReturn()
				.getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		List<AnimediaMALTitleReferences> animediaMALTitleReferences = new ArrayList<>(getMatchedAnime());
		AnimediaMALTitleReferences fairyTail1 = animediaMALTitleReferences.get(1);
		AnimediaMALTitleReferences sao = animediaMALTitleReferences.get(0);
		checkFront(content, fairyTail1, null, sao, notFoundAnime, USERNAME);
	}

	@Test
	public void checkResultForCachedUser() throws Exception {
		String sao1CurrentMax = "10";
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder()
				.url("anime/mastera-mecha-onlayn")
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL("sword art online")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("25")
				.currentMax(sao1CurrentMax)
				.posterUrl("saoPosterUrl")
				.finalUrl(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
		AnimediaMALTitleReferences sao3 = AnimediaMALTitleReferences.builder()
				.url("anime/mastera-mecha-onlayn")
				.dataList("3")
				.firstEpisode("1")
				.titleOnMAL("sword art online: alicization")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("25")
				.currentMax("25")
				.posterUrl("saoPosterUrl")
				.finalUrl("saoFinalUrl")
				.episodeNumberForWatch("25")
				.build();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder()
				.url(FAIRY_TAIL_ROOT_URL)
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL("fairy tail")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("175")
				.currentMax("175")
				.posterUrl("ftPosterUrl")
				.finalUrl("ftFinalUrl")
				.episodeNumberForWatch("2")
				.build();
		Set<AnimediaMALTitleReferences> matchedAnime = new LinkedHashSet<>();
		matchedAnime.add(sao1);
		matchedAnime.add(fairyTail1);
		matchedAnime.add(sao3);
		Set<UserMALTitleInfo> nofFoundOnAnimedia = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "not found on animedia", 0, "testPoster", "testUrl");
		nofFoundOnAnimedia.add(notFoundAnime);
		Set<UserMALTitleInfo> watchingTitlesFromCache = new LinkedHashSet<>();
		UserMALTitleInfo sao = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "sword art online", 25, "saoPosterUrl", "saoAnimeUrl");
		UserMALTitleInfo saoAlicization = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"sword art online: alicization",
				25,
				"saoPosterUrl",
				"saoAnimeUrl");
		UserMALTitleInfo onePunchManSpecials = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"one punch man specials", 6, MY_ANIME_LIST_STATIC_CONTENT_URL + "onePunchManPosterUrl",
				"onePunchManAnimeUrl");
		watchingTitlesFromCache.add(notFoundAnime);
		watchingTitlesFromCache.add(sao);
		watchingTitlesFromCache.add(saoAlicization);
		watchingTitlesFromCache.add(onePunchManSpecials);
		String sao1CurrentMaxUpdated = "25";
		AnimediaMALTitleReferences sao1Updated = AnimediaMALTitleReferences.builder()
				.url("anime/mastera-mecha-onlayn")
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL("sword art online")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("25")
				.currentMax(sao1CurrentMaxUpdated)
				.posterUrl("saoPosterUrl")
				.finalUrl(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesOnAnimedia = new ArrayList<>();
		currentlyUpdatedTitlesOnAnimedia.add(sao1Updated);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes = new LinkedHashSet<>();
		int saoAlicizationUpdatedNumWatchedEpisodes = 10;
		UserMALTitleInfo saoAlicizationUpdated = new UserMALTitleInfo(saoAlicization);
		saoAlicizationUpdated.setNumWatchedEpisodes(saoAlicizationUpdatedNumWatchedEpisodes);
		watchingTitlesWithUpdatedNumberOfWatchedEpisodes.add(saoAlicizationUpdated);
		Set<UserMALTitleInfo> freshWatchingTitles = new LinkedHashSet<>();
		freshWatchingTitles.add(saoAlicizationUpdated);
		AnimediaMALTitleReferences onePunchManSpecialsReference = AnimediaMALTitleReferences.builder()
				.url("anime/vanpanchmen")
				.dataList("7")
				.minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL("one punch man specials")
				.firstEpisode("1")
				.maxConcretizedEpisodeOnAnimedia("6")
				.currentMax("6")
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + "onePunchManPosterUrl")
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6")
				.episodeNumberForWatch("1")
				.finalUrl("onePunchManSpecials")
				.build();
		Set<AnimediaMALTitleReferences> newReference = new LinkedHashSet<>();
		newReference.add(onePunchManSpecialsReference);
		userMALCache.put(USERNAME, watchingTitlesFromCache);
		userMatchedAnimeCache.put(USERNAME, matchedAnime);
		currentlyUpdatedTitlesCache.put(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, new ArrayList<>());
		Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub = new LinkedHashSet<>();
		doReturn(animediaSearchListFromGitHub).when(animediaService)
				.getAnimediaSearchListFromGitHub();
		doReturn(new ArrayList<>()).when(animediaService)
				.getCurrentlyUpdatedTitles();
		doReturn(currentlyUpdatedTitlesOnAnimedia).when(animediaService)
				.checkCurrentlyUpdatedTitles(anyList(), anyList());
		doReturn(true).when(malService)
				.isWatchingTitlesUpdated(eq(freshWatchingTitles), eq(watchingTitlesFromCache));
		notFoundAnimeOnAnimediaRepository.saveAll(nofFoundOnAnimedia);
		doReturn(freshWatchingTitles).when(malService)
				.getWatchingTitles(eq(USERNAME));
		doReturn(watchingTitlesWithUpdatedNumberOfWatchedEpisodes).when(malService)
				.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(eq(freshWatchingTitles), eq(watchingTitlesFromCache));
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		doReturn(animediaTitleSearchInfo).when(animediaService)
				.getAnimediaSearchListFromAnimedia();
		Set<AnimediaMALTitleReferences> allMultiRefs = new LinkedHashSet<>();
		doReturn(allMultiRefs).when(referencesService)
				.getMultiSeasonsReferences();
		doReturn(newReference).when(referencesService)
				.getMatchedReferences(eq(allMultiRefs), argThat(x -> x.size() == 1 && x.contains(onePunchManSpecials)));
		doReturn(newReference).when(seasonsAndEpisodesService)
				.getMatchedAnime(argThat(x -> x.size() == 1 && x.contains(onePunchManSpecials)),
						eq(newReference),
						eq(animediaSearchListFromGitHub),
						eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME))
				.andReturn()
				.getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertEquals(3, matchedAnime.size());
		assertEquals(0,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL()
								.equals(fairyTail1.getTitleOnMAL()))
						.count());
		verify(referencesService, times(1)).updateCurrentMaxAndEpisodesRange(eq(matchedAnime), eq(sao1Updated));
		verify(referencesService, times(1)).getMatchedReferences(eq(allMultiRefs), argThat(x -> x.size() == 1 && x.contains(onePunchManSpecials)));
		verify(referencesService, times(1)).updateReferences(eq(newReference));
		verify(seasonsAndEpisodesService, times(1)).updateEpisodeNumberForWatchAndFinalUrl(eq(watchingTitlesFromCache),
				eq(sao1Updated),
				eq(matchedAnime));
		verify(seasonsAndEpisodesService, times(1)).updateEpisodeNumberForWatchAndFinalUrl(eq(watchingTitlesWithUpdatedNumberOfWatchedEpisodes),
				eq(matchedAnime),
				eq(animediaTitleSearchInfo),
				eq(USERNAME));
		verify(seasonsAndEpisodesService, times(1)).getMatchedAnime(argThat(x -> x.size() == 1 && x.contains(onePunchManSpecials)),
				eq(newReference),
				eq(animediaSearchListFromGitHub),
				eq(USERNAME));
		checkFront(content, sao3, onePunchManSpecialsReference, sao1, notFoundAnime, USERNAME);
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

	private void checkFront(String content, AnimediaMALTitleReferences available, AnimediaMALTitleReferences availableSecond,
			AnimediaMALTitleReferences notAvailable, UserMALTitleInfo notFound, String username) {
		Pattern pattern = Pattern.compile("<title>Result for " + username + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + username + "</h1>\\R</header>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile(
				"<p class=\"title\">New Episode Available</p>\\R\\s*<ul>\\R\\s*<a href=\"" + available.getFinalUrl() + "\" target=\"_blank\"><img src=\""
						+ available.getPosterUrl() + "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + available.getTitleOnMAL() + "\"\\R\\s+title=\""
						+ available.getTitleOnMAL() + " episode " + available.getEpisodeNumberForWatch() + "\"");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		if (availableSecond != null) {
			pattern = Pattern.compile("<a href=\"" + availableSecond.getFinalUrl() + "\" target=\"_blank\"><img src=\"" + availableSecond.getPosterUrl()
					+ "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + availableSecond.getTitleOnMAL() + "\"\\R\\s+title=\"" + availableSecond.getTitleOnMAL()
					+ " episode " + availableSecond.getEpisodeNumberForWatch() + "\"");
			matcher = pattern.matcher(content);
			assertTrue(matcher.find());
		}
		pattern = Pattern.compile("<p class=\"title\">New Episode Not Available</p>\\R\\s*<ul>\\R\\s*<img src=\"" + notAvailable.getPosterUrl()
				+ "\" height=\"318\" width=\"225\" alt=\"" + notAvailable.getTitleOnMAL() + "\"\\R\\s+title=\"" + notAvailable.getTitleOnMAL()
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
		MvcResult result = mockMvc.perform(post(PATH).param("username", USERNAME))
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

	private Set<AnimediaMALTitleReferences> getMatchedAnime() {
		Set<AnimediaMALTitleReferences> matchedAnime = new LinkedHashSet<>();
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder()
				.url("anime/mastera-mecha-onlayn")
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL("sword art online")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("25")
				.currentMax("25")
				.posterUrl("saoPosterUrl")
				.finalUrl(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.episodeNumberForWatch(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.build();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder()
				.url(FAIRY_TAIL_ROOT_URL)
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL("fairy tail")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("175")
				.currentMax("175")
				.posterUrl("ftPosterUrl")
				.finalUrl("ftFinalUrl")
				.episodeNumberForWatch("2")
				.build();
		matchedAnime.add(sao1);
		matchedAnime.add(fairyTail1);
		return matchedAnime;
	}
}