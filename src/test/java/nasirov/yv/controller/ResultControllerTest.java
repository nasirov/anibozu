package nasirov.yv.controller;

import static nasirov.yv.enums.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.MALService;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
import nasirov.yv.util.URLBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.util.NestedServletException;

/**
 * Created by nasirov.yv
 */
@ContextConfiguration(classes = {ResultController.class, AnimediaHTMLParser.class, MALParser.class, CacheManager.class, AppConfiguration.class,
		URLBuilder.class, MALRequestParametersBuilder.class, AnimediaRequestParametersBuilder.class, MethodValidationPostProcessor.class,
		ResourceUrlAdvice.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebMvcTest(ResultController.class)
public class ResultControllerTest extends AbstractTest {

	private static final String USERNAME = "test";

	private static final String PATH = "/result";

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

	@Before
	public void setUp() {
		repoMock = new ArrayList<>();
		doAnswer(answer -> {
			repoMock.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> repoMock.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0)
				.when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
	}

	@Test(expected = NestedServletException.class)
	public void checkResultUsernameLengthLessThen2Chars() throws Exception {
		mockMvc.perform(post(PATH).param("username", ""));
	}

	@Test(expected = NestedServletException.class)
	public void checkResultUsernameLengthMoreThan16Chars() throws Exception {
		mockMvc.perform(post(PATH).param("username", "moreThan16Chars!!!"));
	}

	@Test
	public void checkResultUsernameIsNotFound() throws Exception {
		doThrow(new MALUserAccountNotFoundException()).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
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
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
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
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains(errorMsg));
		checkTitleAndHeaderForError(content, USERNAME);
	}

	@Test
	public void checkResultJsonAnimeListNotFound() throws Exception {
		String errorMsg =
				"The application supports only default mal anime list view with wrapped json data! Json anime list is not found for " + USERNAME;
		doThrow(new JSONNotFoundException()).when(malService).getWatchingTitles(eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertTrue(content.contains(errorMsg));
		checkTitleAndHeaderForError(content, USERNAME);
	}

	@Test
	public void checkResultForNewUser() throws Exception {
		Set<UserMALTitleInfo> nofFound = new LinkedHashSet<>();
		UserMALTitleInfo notFoundAnime = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series", 0, "testPoster", "testUrl");
		nofFound.add(notFoundAnime);
		doAnswer(invocation -> new ArrayList<>(nofFound)).when(notFoundAnimeOnAnimediaRepository).findAll();
		doReturn(nofFound).when(malService).getWatchingTitles(eq(USERNAME));
		doReturn(new ArrayList<>()).when(animediaService).getCurrentlyUpdatedTitles();
		cacheManager.getCache(animediaSearchListCacheName).put(animediaSearchListCacheName, new LinkedHashSet<>());
		doReturn(new LinkedHashSet<>()).when(referencesManager).getMultiSeasonsReferences();
		doReturn(new LinkedHashSet<>()).when(referencesManager).getMatchedReferences(anySet(), anySet());
		doReturn(getMatchedAnime()).when(seasonAndEpisodeChecker).getMatchedAnime(anySet(), anySet(), anySet(), eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
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
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder().url("anime/mastera-mecha-onlayn").dataList("1").firstEpisode("1")
				.titleOnMAL("sword art online").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("25").currentMax(sao1CurrentMax)
				.posterUrl("saoPosterUrl").finalUrl("").episodeNumberForWatch("").build();
		AnimediaMALTitleReferences sao3 = AnimediaMALTitleReferences.builder().url("anime/mastera-mecha-onlayn").dataList("3").firstEpisode("1")
				.titleOnMAL("sword art online: alicization").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("25").currentMax("25")
				.posterUrl("saoPosterUrl").finalUrl("saoFinalUrl").episodeNumberForWatch("25").build();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder().url("anime/skazka-o-hvoste-fei-TV1").dataList("1").firstEpisode("1")
				.titleOnMAL("fairy tail").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175")
				.posterUrl("ftPosterUrl").finalUrl("ftFinalUrl").episodeNumberForWatch("2").build();
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
				"one punch man specials",
				6,
				myAnimeListStaticContentUrl + "onePunchManPosterUrl",
				"onePunchManAnimeUrl");
		watchingTitlesFromCache.add(notFoundAnime);
		watchingTitlesFromCache.add(sao);
		watchingTitlesFromCache.add(saoAlicization);
		watchingTitlesFromCache.add(onePunchManSpecials);
		String sao1CurrentMaxUpdated = "25";
		AnimediaMALTitleReferences sao1Updated = AnimediaMALTitleReferences.builder().url("anime/mastera-mecha-onlayn").dataList("1").firstEpisode("1")
				.titleOnMAL("sword art online").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("25").currentMax(sao1CurrentMaxUpdated)
				.posterUrl("saoPosterUrl").finalUrl("").episodeNumberForWatch("").build();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesOnAnimedia = new ArrayList<>();
		currentlyUpdatedTitlesOnAnimedia.add(sao1Updated);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes = new LinkedHashSet<>();
		int saoAlicizationUpdatedNumWatchedEpisodes = 10;
		UserMALTitleInfo saoAlicizationUpdated = new UserMALTitleInfo(saoAlicization);
		saoAlicizationUpdated.setNumWatchedEpisodes(saoAlicizationUpdatedNumWatchedEpisodes);
		watchingTitlesWithUpdatedNumberOfWatchedEpisodes.add(saoAlicizationUpdated);
		Set<UserMALTitleInfo> freshWatchingTitles = new LinkedHashSet<>();
		freshWatchingTitles.add(saoAlicizationUpdated);
		AnimediaMALTitleReferences onePunchManSpecialsReference = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7")
				.minConcretizedEpisodeOnAnimedia("1").titleOnMAL("one punch man specials").firstEpisode("1").maxConcretizedEpisodeOnAnimedia("6")
				.currentMax("6").posterUrl(myAnimeListStaticContentUrl + "onePunchManPosterUrl").minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6").episodeNumberForWatch("1").finalUrl("onePunchManSpecials").build();
		Set<AnimediaMALTitleReferences> newReference = new LinkedHashSet<>();
		newReference.add(onePunchManSpecialsReference);
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		Cache matchedReferencesCache = cacheManager.getCache(matchedReferencesCacheName);
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		userMALCache.put(USERNAME, watchingTitlesFromCache);
		userMatchedAnimeCache.put(USERNAME, matchedAnime);
		currentlyUpdatedTitlesCache.put(currentlyUpdatedTitlesCacheName, new ArrayList<>());
		Set<AnimediaMALTitleReferences> matchedTitles = new LinkedHashSet<>();
		matchedReferencesCache.put(USERNAME, matchedTitles);
		Set<AnimediaTitleSearchInfo> animediaSearchListCached = new LinkedHashSet<>();
		animediaSearchListCache.put(animediaSearchListCacheName, animediaSearchListCached);
		doReturn(new ArrayList<>()).when(animediaService).getCurrentlyUpdatedTitles();
		doReturn(currentlyUpdatedTitlesOnAnimedia).when(animediaService).checkCurrentlyUpdatedTitles(anyList(), anyList());
		doReturn(true).when(malService).isWatchingTitlesUpdated(eq(freshWatchingTitles), eq(watchingTitlesFromCache));
		doAnswer(invocation -> new ArrayList<>(nofFoundOnAnimedia)).when(notFoundAnimeOnAnimediaRepository).findAll();
		doAnswer(invocation -> true).when(notFoundAnimeOnAnimediaRepository).exitsByTitle(notFoundAnime.getTitle());
		doReturn(freshWatchingTitles).when(malService).getWatchingTitles(eq(USERNAME));
		doReturn(watchingTitlesWithUpdatedNumberOfWatchedEpisodes).when(malService)
				.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(eq(freshWatchingTitles), eq(watchingTitlesFromCache));
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		doReturn(animediaTitleSearchInfo).when(animediaService).getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> allMultiRefs = new LinkedHashSet<>();
		doReturn(allMultiRefs).when(referencesManager).getMultiSeasonsReferences();
		doReturn(newReference).when(seasonAndEpisodeChecker).getMatchedAnime(anySet(), eq(allMultiRefs), eq(animediaSearchListCached), eq(USERNAME));
		MockHttpServletResponse response = mockMvc.perform(post(PATH).param("username", USERNAME)).andReturn().getResponse();
		assertNotNull(response);
		String content = response.getContentAsString();
		assertNotNull(content);
		assertEquals(3, matchedAnime.size());
		assertEquals(0, matchedAnime.stream().filter(set -> set.getTitleOnMAL().equals(fairyTail1.getTitleOnMAL())).count());
		verify(referencesManager, times(1)).updateCurrentMax(eq(matchedAnime), eq(sao1Updated));
		verify(seasonAndEpisodeChecker, times(1)).updateEpisodeNumberForWatchAndFinalUrl(eq(watchingTitlesFromCache), eq(sao1Updated), eq(matchedAnime));
		verify(seasonAndEpisodeChecker, times(1)).updateEpisodeNumberForWatchAndFinalUrl(eq(watchingTitlesWithUpdatedNumberOfWatchedEpisodes),
				eq(matchedAnime),
				eq(animediaTitleSearchInfo),
				eq(USERNAME));
		verify(seasonAndEpisodeChecker, times(1)).getMatchedAnime(anySet(), anySet(), anySet(), eq(USERNAME));
		checkFront(content, sao3, onePunchManSpecialsReference, sao1, notFoundAnime, USERNAME);
	}

	private void checkFront(String content, AnimediaMALTitleReferences available, @Nullable AnimediaMALTitleReferences availableSecond,
			AnimediaMALTitleReferences notAvailable, UserMALTitleInfo notFound, String username) {
		Pattern pattern = Pattern.compile("<title>Result for " + username + "</title>");
		Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile("<header>\\R\\s*<h1>Result for " + username + "</h1>\\R</header>");
		matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		pattern = Pattern.compile(
				"<p class=\"title\">New Episode Available</p>\\R\\s*<ul>\\R\\s*<a href=\"" + available.getFinalUrl() + "\" target=\"_blank\"><img src=\""
						+ available.getPosterUrl() + "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + available.getTitleOnMAL() + "\"\\R\\s+title=\"" + available
						.getTitleOnMAL() + " episode " + available.getEpisodeNumberForWatch() + "\"");
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
						+ notFound.getPosterUrl() + "\" height=\"318\" width=\"225\"\\R\\s+alt=\"" + notFound.getTitle() + "\"\\R\\s+title=\"" + notFound
						.getTitle() + "\" class=\"fade\"/></a>");
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
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder().url("anime/mastera-mecha-onlayn").dataList("1").firstEpisode("1")
				.titleOnMAL("sword art online").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("25").currentMax("25")
				.posterUrl("saoPosterUrl").finalUrl("").episodeNumberForWatch("").build();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder().url("anime/skazka-o-hvoste-fei-TV1").dataList("1").firstEpisode("1")
				.titleOnMAL("fairy tail").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175")
				.posterUrl("ftPosterUrl").finalUrl("ftFinalUrl").episodeNumberForWatch("2").build();
		matchedAnime.add(sao1);
		matchedAnime.add(fairyTail1);
		return matchedAnime;
	}
}