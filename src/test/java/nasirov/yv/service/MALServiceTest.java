package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.MALRequestParametersBuilder;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.MALParser;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {MALService.class, MALParser.class, CacheManager.class, AppConfiguration.class, MALRequestParametersBuilder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MALServiceTest extends AbstractTest {

	private static final String LOAD_JSON = "/load.json";

	private static final String SEARCH_PREFIX_JSON = "search/prefix.json";

	private static final String PROFILE = "profile/";

	private static final String ANIME_LIST = "animelist/";

	private static final String STATUS = "status";

	private static final String TEST_ACC_FOR_DEV = "testAccForDev";

	/**
	 * Max number of rows on html page
	 */
	private static final Integer MAX_NUMBER_OF_TITLE_IN_HTML = 300;

	private static final Integer INITIAL_OFFSET_FOR_LOAD_JSON = 0;


	private static final String CACHED_ANIME_LIST = "cached";

	private static final String NEW_TITLE_IN_ANIME_LIST = "newTitleInAnimeList";

	private static final String TITLE_REMOVED_FROM_ANIME_LIST = "titleRemovedFromAnimeList";

	private static final String NUMBER_OF_WATCHED_EPISODES_INCREASED = "numberOfWatchedEpisodesIncreased";

	private static final String NUMBER_OF_WATCHED_EPISODES_DECREASED = "numberOfWatchedEpisodesDecreased";

	@MockBean
	private HttpCaller httpCaller;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private MALService malService;

	@Autowired
	@Qualifier("malRequestParametersBuilder")
	private RequestParametersBuilder parametersBuilder;

	@Test
	public void getWatchingTitles() throws Exception {
		Map<String, Map<String, String>> params = parametersBuilder.build();
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(TEST_ACC_FOR_DEV, LinkedHashSet.class);
		assertNull(watchingTitlesFromCache);
		String profileUrl = myAnimeListNet + PROFILE + TEST_ACC_FOR_DEV;
		String firstJsonUrl300Titles =
				myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + LOAD_JSON + "?" + "offset=" + INITIAL_OFFSET_FOR_LOAD_JSON + "&" + STATUS + "=" + WATCHING
						.getCode().toString();
		String additionalJsonUrlMoreThan300 =
				myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + LOAD_JSON + "?" + "offset=" + MAX_NUMBER_OF_TITLE_IN_HTML + "&" + STATUS + "=" + WATCHING
						.getCode().toString();
		String additionalJsonUrlMore600 =
				myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + LOAD_JSON + "?" + "offset=" + (MAX_NUMBER_OF_TITLE_IN_HTML * 2) + "&" + STATUS + "="
						+ WATCHING.getCode().toString();
		String additionalJsonUrlMore900 =
				myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + LOAD_JSON + "?" + "offset=" + (MAX_NUMBER_OF_TITLE_IN_HTML * 3) + "&" + STATUS + "="
						+ WATCHING.getCode().toString();
		doReturn(new HttpResponse(RoutinesIO.readFromResource(testAccForDevProfile), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(profileUrl), eq(HttpMethod.GET), eq(params));
		doReturn(new HttpResponse(RoutinesIO.readFromResource(testAccForDevFirstJson300), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(firstJsonUrl300Titles), eq(HttpMethod.GET), eq(params));
		doReturn(new HttpResponse(RoutinesIO.readFromResource(testAccForDevAdditionalJsonMoreThan300), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(additionalJsonUrlMoreThan300), eq(HttpMethod.GET), eq(params));
		doReturn(new HttpResponse(RoutinesIO.readFromResource(testAccForDevAdditionalJsonMoreThan600), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(additionalJsonUrlMore600), eq(HttpMethod.GET), eq(params));
		doReturn(new HttpResponse(RoutinesIO.readFromResource(testAccForDevAdditionalJsonMoreThan900), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(additionalJsonUrlMore900), eq(HttpMethod.GET), eq(params));
		Set<UserMALTitleInfo> watchingTitles = malService.getWatchingTitles(TEST_ACC_FOR_DEV);
		assertNotNull(watchingTitles);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitles.size());
		watchingTitlesFromCache = userMALCache.get(TEST_ACC_FOR_DEV, LinkedHashSet.class);
		assertNotNull(watchingTitlesFromCache);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitlesFromCache.size());
		verify(httpCaller, times(5)).call(any(String.class), eq(HttpMethod.GET), eq(params));
		verify(httpCaller, times(1)).call(eq(profileUrl), eq(HttpMethod.GET), eq(params));
		verify(httpCaller, times(1)).call(eq(firstJsonUrl300Titles), eq(HttpMethod.GET), eq(params));
		verify(httpCaller, times(1)).call(eq(additionalJsonUrlMoreThan300), eq(HttpMethod.GET), eq(params));
		verify(httpCaller, times(1)).call(eq(additionalJsonUrlMore600), eq(HttpMethod.GET), eq(params));
		verify(httpCaller, times(1)).call(eq(additionalJsonUrlMore900), eq(HttpMethod.GET), eq(params));
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesNotFound() throws Exception {
		Map<String, Map<String, String>> params = parametersBuilder.build();
		String profileUrl = myAnimeListNet + PROFILE + TEST_ACC_FOR_DEV;
		doReturn(new HttpResponse("", HttpStatus.OK.value())).when(httpCaller).call(eq(profileUrl), eq(HttpMethod.GET), eq(params));
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesEqualsZero() throws Exception {
		Map<String, Map<String, String>> params = parametersBuilder.build();
		String profileUrl = myAnimeListNet + PROFILE + TEST_ACC_FOR_DEV;
		doReturn(new HttpResponse("Watching</a><span class=\"di-ib fl-r lh10\">0</span>", HttpStatus.OK.value())).when(httpCaller)
				.call(eq(profileUrl), eq(HttpMethod.GET), eq(params));
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALUserAccountNotFoundException.class)
	public void getWatchingTitlesMALUserAccountNotFound() throws Exception {
		Map<String, Map<String, String>> params = parametersBuilder.build();
		String profileUrl = myAnimeListNet + PROFILE + TEST_ACC_FOR_DEV;
		doReturn(new HttpResponse("", HttpStatus.NOT_FOUND.value())).when(httpCaller).call(eq(profileUrl), eq(HttpMethod.GET), eq(params));
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test
	public void isWatchingTitlesUpdatedEquals() throws Exception {
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		assertFalse(malService.isWatchingTitlesUpdated(cached, cached));
		assertEquals(cached, new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST)));
	}

	@Test
	public void isWatchingTitlesUpdatedNumberOfWatchedEpisodesIncreased() throws Exception {
		checkTitlesWithUpdatedNumberOfWatchedEpisodes(NUMBER_OF_WATCHED_EPISODES_INCREASED);
	}

	@Test
	public void isWatchingTitlesUpdatedNumberOfWatchedEpisodesDecreased() throws Exception {
		checkTitlesWithUpdatedNumberOfWatchedEpisodes(NUMBER_OF_WATCHED_EPISODES_DECREASED);
	}

	@Test
	public void isWatchingTitlesUpdatedNewTitleInAnimeList() throws Exception {
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		Set<UserMALTitleInfo> animeListWithNewTitle = new LinkedHashSet<>(getWatchingTitlesForTest(NEW_TITLE_IN_ANIME_LIST));
		assertNotEquals(cached, animeListWithNewTitle);
		UserMALTitleInfo newTitle = getWatchingTitlesForTest(NEW_TITLE_IN_ANIME_LIST).get(3);
		assertFalse(cached.contains(newTitle));
		assertTrue(malService.isWatchingTitlesUpdated(animeListWithNewTitle, cached));
		assertEquals(cached, animeListWithNewTitle);
		assertTrue(cached.contains(newTitle));
		assertEquals(new ArrayList<>(cached).get(3), newTitle);
	}

	@Test
	public void isWatchingTitlesUpdatedTitleRemovedFromAnimeList() throws Exception {
		List<UserMALTitleInfo> cachedList = getWatchingTitlesForTest(CACHED_ANIME_LIST);
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(cachedList);
		Set<UserMALTitleInfo> animeListWithRemovedTitle = new LinkedHashSet<>(getWatchingTitlesForTest(TITLE_REMOVED_FROM_ANIME_LIST));
		assertNotEquals(cached, animeListWithRemovedTitle);
		UserMALTitleInfo removedTitle = cachedList.get(0);
		assertFalse(animeListWithRemovedTitle.contains(removedTitle));
		assertTrue(malService.isWatchingTitlesUpdated(animeListWithRemovedTitle, cached));
		assertEquals(cached, animeListWithRemovedTitle);
		assertFalse(cached.contains(removedTitle));
		assertFalse(animeListWithRemovedTitle.contains(removedTitle));
	}

	@Test
	public void getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes() {
		UserMALTitleInfo accelWorld = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"accel world",
				24,
				"https://myanimelist.cdn-dena.com/images/anime/8/38155.webp",
				"https://myanimelist.net//anime/11759/Accel_World");
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>();
		cached.add(accelWorld);
		Set<UserMALTitleInfo> fresh = new LinkedHashSet<>();
		fresh.add(accelWorld);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes = malService
				.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(fresh, cached);
		assertEquals(0, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.size());
		int freshNumWatcheEpisodes = 10;
		UserMALTitleInfo increasedAccelWorld = new UserMALTitleInfo(accelWorld);
		increasedAccelWorld.setNumWatchedEpisodes(freshNumWatcheEpisodes);
		fresh.add(increasedAccelWorld);
		watchingTitlesWithUpdatedNumberOfWatchedEpisodes = malService.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(fresh, cached);
		assertEquals(1, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.size());
		assertEquals(1, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.stream().filter(title -> title.equals(increasedAccelWorld)).count());
		fresh.clear();
		accelWorld.setNumWatchedEpisodes(freshNumWatcheEpisodes);
		freshNumWatcheEpisodes = 5;
		UserMALTitleInfo decreasedAccelWorld = new UserMALTitleInfo(accelWorld);
		decreasedAccelWorld.setNumWatchedEpisodes(freshNumWatcheEpisodes);
		fresh.add(decreasedAccelWorld);
		watchingTitlesWithUpdatedNumberOfWatchedEpisodes = malService.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(fresh, cached);
		assertEquals(1, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.size());
		assertEquals(1, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.stream().filter(title -> title.equals(decreasedAccelWorld)).count());
	}

	@Test
	public void testIsTitleExistResultFound() {
		String url = myAnimeListNet + SEARCH_PREFIX_JSON + "?type=all&v=1&keyword=fairy%20tail";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(searchTitleFairyTail), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(url), eq(HttpMethod.GET), eq(parametersBuilder.build()));
		assertTrue(malService.isTitleExist("fairy tail"));
	}

	@Test
	public void testIsTitleExistResultNotFound() {
		String url = myAnimeListNet + SEARCH_PREFIX_JSON + "?type=all&v=1&keyword=notFairyTail";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(searchTitleNotFound), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(url), eq(HttpMethod.GET), eq(parametersBuilder.build()));
		assertFalse(malService.isTitleExist("notFairyTail"));
	}

	@Test
	public void testIsTitleExistBadRequest() {
		String url = myAnimeListNet + SEARCH_PREFIX_JSON + "?type=all&v=1&keyword=notFairyTail";
		doReturn(new HttpResponse("{\"errors\":[{\"message\":\"Your keyword length must save less than or equal to 100.\"}]}",
				HttpStatus.BAD_REQUEST.value())).when(httpCaller).call(eq(url), eq(HttpMethod.GET), eq(parametersBuilder.build()));
		assertFalse(malService.isTitleExist("notFairyTail"));
	}

	private void checkTitlesWithUpdatedNumberOfWatchedEpisodes(String typeOfChange) {
		Set<UserMALTitleInfo> increasedWatchedEpisodes = new LinkedHashSet<>(getWatchingTitlesForTest(typeOfChange));
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		UserMALTitleInfo updatedTitle = getWatchingTitlesForTest(typeOfChange).get(0);
		assertNotEquals(increasedWatchedEpisodes, cached);
		assertNotEquals(updatedTitle.getNumWatchedEpisodes(), new ArrayList<>(cached).get(0).getNumWatchedEpisodes());
		assertTrue(malService.isWatchingTitlesUpdated(increasedWatchedEpisodes, cached));
		assertEquals(updatedTitle.getNumWatchedEpisodes(), new ArrayList<>(cached).get(0).getNumWatchedEpisodes());
		assertEquals(increasedWatchedEpisodes, cached);
	}

	private List<UserMALTitleInfo> getWatchingTitlesForTest(String type) {
		List<UserMALTitleInfo> watchingTitles = new ArrayList<>();
		UserMALTitleInfo realGirl = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				5,
				"3d kanojo: real girl",
				12,
				"https://myanimelist.cdn-dena.com/images/anime/1327/93616.webp",
				"https://myanimelist.net//anime/36793/3D_Kanojo__Real_Girl");
		UserMALTitleInfo accelWorld = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"accel world",
				24,
				"https://myanimelist.cdn-dena.com/images/anime/8/38155.webp",
				"https://myanimelist.net//anime/11759/Accel_World");
		UserMALTitleInfo angelBeats = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"angel beats!",
				13,
				"https://myanimelist.cdn-dena.com/images/anime/10/22061.webp",
				"https://myanimelist.net//anime/6547/Angel_Beats");
		UserMALTitleInfo noragami = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"noragami",
				12,
				"https://myanimelist.cdn-dena.com/images/anime/9/77809.webp",
				"https://myanimelist.net//anime/20507/Noragami");
		switch (type) {
			case CACHED_ANIME_LIST:
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NUMBER_OF_WATCHED_EPISODES_INCREASED:
				realGirl.setNumWatchedEpisodes(10);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NUMBER_OF_WATCHED_EPISODES_DECREASED:
				realGirl.setNumWatchedEpisodes(0);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NEW_TITLE_IN_ANIME_LIST:
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				watchingTitles.add(noragami);
				break;
			case TITLE_REMOVED_FROM_ANIME_LIST:
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
		}
		return watchingTitles;
	}
}