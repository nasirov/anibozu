package nasirov.yv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AnimediaService.class, AnimediaHTMLParser.class, CacheManager.class, CacheConfiguration.class,
		AnimediaRequestParametersBuilder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unchecked")
public class AnimediaServiceTest extends AbstractTest {

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";

	@Value("classpath:animedia/search/singleSeasonsAnimeUrls.json")
	private Resource singleSeasonsAnimeUrls;

	@Value("classpath:animedia/search/animediaSearchListForCheck.json")
	private Resource animediaSearchListForCheck;

	@Value("classpath:animedia/search/animediaSearchListFull.json")
	private Resource animediaSearchListFull;

	@Value("classpath:animedia/search/announcements.json")
	private Resource announcementsJson;

	@Value("classpath:animedia/search/multiSeasonsAnimeUrls.json")
	private Resource multiSeasonsAnimeUrls;

	@Value("classpath:animedia/ingressHtml.txt")
	private Resource announcementHtml;

	@MockBean
	private HttpCaller httpCaller;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private AnimediaServiceI animediaService;

	@Test
	public void testGetAnimediaSearchListFromAnimedia() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeList()), eq(HttpMethod.GET), anyMap());
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromAnimedia();
		int fullSize = 787;
		assertNotNull(animediaSearchList);
		assertEquals(fullSize, animediaSearchList.size());
		assertEquals(fullSize, animediaSearchList.stream().filter(set -> set.getUrl().matches("^anime/.+")).count());
		assertEquals(fullSize,
				animediaSearchList.stream()
						.filter(set -> set.getPosterUrl().matches("https://static\\.animedia\\.tv/uploads/.+\\?" + POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER))
						.count());
		verify(httpCaller, times(1)).call(eq(urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeList()), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void testGetAnimediaSearchListFromGitHub() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(urlsNames.getGitHubUrls().getRawGithubusercontentComAnimediaSearchList()), eq(HttpMethod.GET), anyMap());
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		int fullSize = 787;
		assertNotNull(animediaSearchList);
		assertEquals(fullSize, animediaSearchList.size());
		verify(httpCaller, times(1)).call(eq(urlsNames.getGitHubUrls().getRawGithubusercontentComAnimediaSearchList()), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void testGetCurrentlyUpdatedTitles() throws Exception {
		Cache cache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFromCache = cache
				.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNull(animediaMALTitleReferencesFromCache);
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		int currentlyUpdatedSize = 10;
		assertNotNull(currentlyUpdatedTitles);
		assertEquals(currentlyUpdatedSize, currentlyUpdatedTitles.size());
		animediaMALTitleReferencesFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNotNull(animediaMALTitleReferencesFromCache);
		assertEquals(animediaMALTitleReferencesFromCache.size(), currentlyUpdatedSize);
		cache.clear();
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesDifferentValues() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFresh = new ArrayList<>();
		currentlyUpdatedTitles.forEach(list -> animediaMALTitleReferencesFresh.add(new AnimediaMALTitleReferences(list)));
		animediaMALTitleReferencesFresh.add(0, animediaMALTitleReferencesFresh.get(9));
		animediaMALTitleReferencesFresh.remove(9);
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(animediaMALTitleReferencesFresh, currentlyUpdatedTitles);
		assertNotNull(result);
		assertEquals(1, result.size());
		Cache cache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		assertNotNull(cache);
		currentlyUpdatedTitles = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertEquals(1, currentlyUpdatedTitles.size());
		assertEquals(animediaMALTitleReferencesFresh.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmpty() {
		Cache cache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		List<AnimediaMALTitleReferences> resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNull(resultFromCache);
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFresh = animediaService.getCurrentlyUpdatedTitles();
		resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertEquals(10, resultFromCache.size());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesCached = new ArrayList<>();
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(currentlyUpdatedTitlesFresh, currentlyUpdatedTitlesCached);
		assertEquals(0, result.size());
		resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertEquals(10, resultFromCache.size());
		cache.clear();
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesFreshEmpty() {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		Cache cache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNotNull(resultFromCache);
		assertEquals(10, resultFromCache.size());
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(), currentlyUpdatedTitles);
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(resultFromCache.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmptyFreshEmpty() {
		Cache cache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNull(resultFromCache);
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(), new ArrayList<>());
		assertEquals(0, result.size());
		resultFromCache = cache.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		assertNull(resultFromCache);
		cache.clear();
	}

}