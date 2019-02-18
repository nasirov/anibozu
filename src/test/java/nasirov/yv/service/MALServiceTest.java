package nasirov.yv.service;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.MALRequestParametersBuilder;
import nasirov.yv.parser.MALParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static nasirov.yv.enums.MALAnimeStatus.WATCHING;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {
		MALService.class,
		MALParser.class,
		WrappedObjectMapper.class,
		CacheManager.class,
		AppConfiguration.class,
		URLBuilder.class,
		MALRequestParametersBuilder.class,
		RoutinesIO.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MALServiceTest extends AbstractTest {
	private static final String LOAD_JSON = "load.json";
	
	private static final String PROFILE = "profile/";
	
	private static final String ANIME_LIST = "animelist/";
	
	private static final String STATUS = "status";
	
	private static final String TEST_ACC_FOR_DEV = "testAccForDev";
	
	/**
	 * Max number of rows on html page
	 */
	private static final Integer MAX_NUMBER_OF_TITLE_IN_HTML = 300;
	
	private static final String CACHED = "cached";
	
	private static final String FRESH = "fresh";
	
	private static final String DIFF_SIZE = "diffSize";
	
	@MockBean
	private HttpCaller httpCaller;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private MALService malService;
	
	@Test
	public void getWatchingTitles() throws Exception {
		int watchingTitlesNum = 351;
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(TEST_ACC_FOR_DEV, LinkedHashSet.class);
		assertNull(watchingTitlesFromCache);
		String profileUrl = myAnimeListNet + PROFILE + TEST_ACC_FOR_DEV;
		String firstJsonUrl = myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + "?" + STATUS + "=" + WATCHING.getCode().toString();
		String additionalJsonUrl = myAnimeListNet + ANIME_LIST + TEST_ACC_FOR_DEV + "/" + LOAD_JSON + "?" + "offset=" + MAX_NUMBER_OF_TITLE_IN_HTML + "&" + STATUS + "=" + WATCHING.getCode().toString();
		doReturn(new HttpResponse(routinesIO.readFromResource(testAccForDevProfile), HttpStatus.OK.value())).when(httpCaller).call(eq(profileUrl), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(testAccForDevWatchingTitles), HttpStatus.OK.value())).when(httpCaller).call(eq(firstJsonUrl), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(testAccForDevAdditionalJson), HttpStatus.OK.value())).when(httpCaller).call(eq(additionalJsonUrl), eq(HttpMethod.GET), anyMap());
		Set<UserMALTitleInfo> watchingTitles = malService.getWatchingTitles(TEST_ACC_FOR_DEV);
		assertNotNull(watchingTitles);
		assertEquals(watchingTitlesNum, watchingTitles.size());
		watchingTitlesFromCache = userMALCache.get(TEST_ACC_FOR_DEV, LinkedHashSet.class);
		assertNotNull(watchingTitlesFromCache);
		assertEquals(watchingTitlesNum, watchingTitlesFromCache.size());
		verify(httpCaller, times(3)).call(any(String.class), eq(HttpMethod.GET), anyMap());
	}
	
	@Test
	public void isWatchingTitlesUpdated() throws Exception {
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED));
		Set<UserMALTitleInfo> fresh = new LinkedHashSet<>(getWatchingTitlesForTest(FRESH));
		Set<UserMALTitleInfo> diffSize = new LinkedHashSet<>(getWatchingTitlesForTest(DIFF_SIZE));
		assertFalse(malService.isWatchingTitlesUpdated(cached, cached));
		assertFalse(malService.isWatchingTitlesUpdated(fresh, fresh));
		assertFalse(malService.isWatchingTitlesUpdated(diffSize, diffSize));
		assertTrue(malService.isWatchingTitlesUpdated(diffSize, cached));
		List<UserMALTitleInfo> changedCached = new ArrayList<>(cached);
		UserMALTitleInfo tempCached = getWatchingTitlesForTest(CACHED).get(2);
		assertEquals(getWatchingTitlesForTest(DIFF_SIZE).size(), changedCached.size());
		assertEquals(getWatchingTitlesForTest(DIFF_SIZE).get(0).getNumWatchedEpisodes(), changedCached.get(0).getNumWatchedEpisodes());
		assertEquals(getWatchingTitlesForTest(DIFF_SIZE).get(0).getTitle(), changedCached.get(0).getTitle());
		assertEquals(0, changedCached.stream().filter(list -> list.getTitle().equals(tempCached.getTitle())).count());
		cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED));
		changedCached = new ArrayList<>(cached);
		assertTrue(malService.isWatchingTitlesUpdated(fresh, cached));
		assertEquals(getWatchingTitlesForTest(FRESH).size(), changedCached.size());
		assertEquals(getWatchingTitlesForTest(FRESH).get(0).getNumWatchedEpisodes(), changedCached.get(0).getNumWatchedEpisodes());
		assertEquals(getWatchingTitlesForTest(FRESH).get(0).getTitle(), changedCached.get(0).getTitle());
	}
	
	private List<UserMALTitleInfo> getWatchingTitlesForTest(String type) {
		List<UserMALTitleInfo> watchingTitles = new ArrayList<>();
		UserMALTitleInfo realGirl = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "3d kanojo: real girl", 12,
				"https://myanimelist.cdn-dena.com/images/anime/1327/93616.webp", "https://myanimelist.net//anime/36793/3D_Kanojo__Real_Girl");
		UserMALTitleInfo accelWorld = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "accel world", 24,
				"https://myanimelist.cdn-dena.com/images/anime/8/38155.webp", "https://myanimelist.net//anime/11759/Accel_World");
		UserMALTitleInfo angelBeats = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "angel beats!", 13,
				"https://myanimelist.cdn-dena.com/images/anime/10/22061.webp", "https://myanimelist.net//anime/6547/Angel_Beats");
		switch (type) {
			case CACHED:
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case FRESH:
				realGirl.setNumWatchedEpisodes(10);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case DIFF_SIZE:
				realGirl.setNumWatchedEpisodes(10);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				break;
		}
		return watchingTitles;
	}
}