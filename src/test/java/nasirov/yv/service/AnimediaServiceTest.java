package nasirov.yv.service;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.enums.AnimeTypeOnAnimedia;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.*;

import static nasirov.yv.enums.AnimeTypeOnAnimedia.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AnimediaService.class,
		AnimediaHTMLParser.class,
		WrappedObjectMapper.class,
		URLBuilder.class,
		RoutinesIO.class,
		AnimediaRequestParametersBuilder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AnimediaServiceTest extends AbstractTest {
	@MockBean
	private HttpCaller httpCaller;
	
	@MockBean
	private CacheManager cacheManager;
	
	@Autowired
	private WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	@Autowired
	private AnimediaService animediaService;
	
	private Cache animediaSearchListCacheStub;
	
	private Cache currentlyUpdatedTitlesCacheStub;
	
	private Cache sortedAnimediaSearchListCacheStub;
	
	@Before
	public void setUp() {
		animediaSearchListCacheStub = new ConcurrentMapCache(animediaSearchListCacheName);
		currentlyUpdatedTitlesCacheStub = new ConcurrentMapCache(currentlyUpdatedTitlesCacheName);
		sortedAnimediaSearchListCacheStub = new ConcurrentMapCache(sortedAnimediaSearchListCacheName);
		doAnswer(answer -> animediaSearchListCacheStub).when(cacheManager).getCache(animediaSearchListCacheName);
		doAnswer(answer -> currentlyUpdatedTitlesCacheStub).when(cacheManager).getCache(currentlyUpdatedTitlesCacheName);
		doAnswer(answer -> sortedAnimediaSearchListCacheStub).when(cacheManager).getCache(sortedAnimediaSearchListCacheName);
	}
	
	@Test
	public void testGetAnimediaSearchList() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaAnimeList), eq(HttpMethod.GET), anyMap());
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
		int fullSize = 780;
		assertNotNull(animediaSearchList);
		assertEquals(fullSize, animediaSearchList.size());
		long count = animediaSearchList.stream().filter(set -> set.getUrl().startsWith(animediaOnlineTv)).count();
		assertEquals(0, count);
		Cache cache = cacheManager.getCache(animediaSearchListCacheName);
		assertNotNull(cache);
		Set<AnimediaTitleSearchInfo> animediaSearchListFromCache = cache.get(animediaSearchListCacheName, LinkedHashSet.class);
		assertNotNull(cache);
		assertEquals(fullSize, animediaSearchListFromCache.size());
		cache.clear();
	}
	
	@Test
	public void testGetCurrentlyUpdatedTitles() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		int currentlyUpdatedSize = 10;
		assertNotNull(currentlyUpdatedTitles);
		assertEquals(currentlyUpdatedSize, currentlyUpdatedTitles.size());
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFromCache = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertNotNull(animediaMALTitleReferencesFromCache);
		assertEquals(animediaMALTitleReferencesFromCache.size(), currentlyUpdatedSize);
		cache.clear();
	}
	
	@Test
	public void testGetSortedForSeasonAnime() throws Exception {
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		AnimediaTitleSearchInfo singleSeasonAnime = getSingleSeasonAnime();
		AnimediaTitleSearchInfo multiSeasonsAnime = getMultiSeasonsAnime();
		AnimediaTitleSearchInfo announcement = getAnnouncement();
		animediaTitleSearchInfo.add(singleSeasonAnime);
		animediaTitleSearchInfo.add(multiSeasonsAnime);
		animediaTitleSearchInfo.add(announcement);
		doReturn(new HttpResponse(routinesIO.readFromResource(blackCloverHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + singleSeasonAnime.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(saoHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + multiSeasonsAnime.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(announcementHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + announcement.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(saoDataList1), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/1"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(saoDataList2), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/2"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(saoDataList3), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/3"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(saoDataList7), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/7"), eq(HttpMethod.GET), anyMap());
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedForType(animediaTitleSearchInfo);
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		compareResults(single, multi, announcements, singleSeasonAnime, multiSeasonsAnime, announcement);
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		List<Anime> singleFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), LinkedHashSet.class));
		List<Anime> multiFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(MULTISEASONS.getDescription(), LinkedHashSet.class));
		List<Anime> announcementsFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), LinkedHashSet.class));
		compareResults(singleFromCache, multiFromCache, announcementsFromCache, singleSeasonAnime, multiSeasonsAnime, announcement);
		String prefix = tempFolderName + File.separator;
		assertTrue(routinesIO.isDirectoryExists(tempFolderName));
		assertTrue(new File(prefix + singleSeasonsAnimeUrls.getFilename()).exists());
		assertTrue(new File(prefix + multiSeasonsAnimeUrls.getFilename()).exists());
		assertTrue(new File(prefix + announcementsJson.getFilename()).exists());
		routinesIO.removeDir(tempFolderName);
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesDifferentValues() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFresh = new ArrayList<>();
		currentlyUpdatedTitles.forEach(list -> animediaMALTitleReferencesFresh.add(new AnimediaMALTitleReferences(list)));
		animediaMALTitleReferencesFresh.add(0, animediaMALTitleReferencesFresh.get(9));
		animediaMALTitleReferencesFresh.remove(9);
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(animediaMALTitleReferencesFresh, currentlyUpdatedTitles);
		assertNotNull(result);
		assertEquals(1, result.size());
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		currentlyUpdatedTitles = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertEquals(1, currentlyUpdatedTitles.size());
		assertEquals(animediaMALTitleReferencesFresh.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmpty() {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(currentlyUpdatedTitles, new ArrayList<>());
		assertNotNull(result);
		assertEquals(10, result.size());
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> resultFromCache = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertNotNull(resultFromCache);
		assertEquals(10, resultFromCache.size());
		assertEquals(result.get(0), currentlyUpdatedTitles.get(0));
		assertEquals(result.get(0), resultFromCache.get(0));
		assertEquals(resultFromCache.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesFreshEmpty() {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(), currentlyUpdatedTitles);
		assertNotNull(result);
		assertEquals(0, result.size());
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> resultFromCache = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertNotNull(resultFromCache);
		assertEquals(10, resultFromCache.size());
		assertEquals(resultFromCache.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}
	
	@Test
	public void testGetAnimeFromClasspath() throws Exception {
		ReflectionTestUtils.setField(animediaService, "resourceAnnouncementsUrls", announcementsJson);
		ReflectionTestUtils.setField(animediaService, "resourceMultiSeasonsAnimeUrls", multiSeasonsAnimeUrls);
		ReflectionTestUtils.setField(animediaService, "resourceSingleSeasonsAnimeUrls", singleSeasonsAnimeUrls);
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedForTypeFromResources();
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		assertEquals(2, single.size());
		assertEquals(6, multi.size());
		assertEquals(2, announcements.size());
		Cache cache = cacheManager.getCache(animediaSearchListCacheName);
		assertNotNull(cache);
		cache.clear();
	}
	
	@Test
	public void testGetAnimeFromTempFolder() throws Exception {
		ReflectionTestUtils.setField(animediaService, "resourceAnnouncementsUrls", announcementsJson);
		ReflectionTestUtils.setField(animediaService, "resourceMultiSeasonsAnimeUrls", multiSeasonsAnimeUrls);
		ReflectionTestUtils.setField(animediaService, "resourceSingleSeasonsAnimeUrls", singleSeasonsAnimeUrls);
		routinesIO.mkDir(tempFolderName);
		String prefix = tempFolderName + File.separator;
		routinesIO.writeToFile(prefix + announcementsJson.getFilename(), routinesIO.readFromResource(announcementsJson), false);
		routinesIO.writeToFile(prefix + multiSeasonsAnimeUrls.getFilename(), routinesIO.readFromResource(multiSeasonsAnimeUrls), false);
		routinesIO.writeToFile(prefix + singleSeasonsAnimeUrls.getFilename(), routinesIO.readFromResource(singleSeasonsAnimeUrls), false);
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedForTypeFromResources();
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		assertEquals(2, single.size());
		assertEquals(6, multi.size());
		assertEquals(2, announcements.size());
		Cache cache = cacheManager.getCache(animediaSearchListCacheName);
		assertNotNull(cache);
		cache.clear();
		routinesIO.removeDir(tempFolderName);
	}
	
	@Test
	public void testGetAnimeResourcesAreNull() throws Exception {
		ClassPathResource classPathResource = new ClassPathResource("resourcesNotFound");
		ReflectionTestUtils.setField(animediaService, "resourceAnnouncementsUrls", classPathResource);
		ReflectionTestUtils.setField(animediaService, "resourceMultiSeasonsAnimeUrls", classPathResource);
		ReflectionTestUtils.setField(animediaService, "resourceSingleSeasonsAnimeUrls", classPathResource);
		ReflectionTestUtils.setField(animediaService, "tempFolderName", "classpath:notFound");
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedForTypeFromResources();
		assertNotNull(sortedAnime);
		assertTrue(sortedAnime.isEmpty());
	}
	
	@Test
	public void testCheckAnime() throws Exception {
		Set<Anime> single = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(singleSeasonsAnimeUrls), Anime.class, LinkedHashSet.class);
		Set<Anime> multi = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(multiSeasonsAnimeUrls), Anime.class, LinkedHashSet.class);
		Set<Anime> announcement = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(announcementsJson), Anime.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> searchListForCheck = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(animediaSearchListForCheck), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		List<AnimediaTitleSearchInfo> notFound = new ArrayList<>(animediaService.checkSortedAnime(single, multi, announcement, searchListForCheck));
		assertNotNull(notFound);
		assertEquals(1, notFound.size());
		assertEquals("anime/domekano", notFound.get(0).getUrl());
	}
	
	@Test
	public void testIsAnimediaSearchListUpToDate() {
		Set<AnimediaTitleSearchInfo> fresh = routinesIO.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Iterator<AnimediaTitleSearchInfo> iterator = fresh.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			break;
		}
		Set<AnimediaTitleSearchInfo> fromResources = routinesIO.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		assertNotEquals(fresh.size(), fromResources.size());
		assertFalse(animediaService.isAnimediaSearchListUpToDate(fromResources, fresh));
		assertFalse(animediaService.isAnimediaSearchListUpToDate(fresh, fromResources));
	}
	
	private void compareResults(List<Anime> single,
								List<Anime> multi,
								List<Anime> announcements,
								AnimediaTitleSearchInfo singleSeasonAnime,
								AnimediaTitleSearchInfo multiSeasonsAnime,
								AnimediaTitleSearchInfo announcement) {
		assertNotNull(single);
		assertNotNull(multi);
		assertNotNull(announcements);
		assertEquals(1, single.size());
		assertEquals(4, multi.size());
		assertEquals(1, announcements.size());
		Anime blackClover = single.get(0);
		Anime sao1 = multi.get(0);
		Anime sao2 = multi.get(1);
		Anime sao3 = multi.get(2);
		Anime sao7 = multi.get(3);
		Anime ingress = announcements.get(0);
		assertEquals("1", blackClover.getId());
		assertEquals("1.1", sao1.getId());
		assertEquals("1.2", sao2.getId());
		assertEquals("1.3", sao3.getId());
		assertEquals("1.4", sao7.getId());
		assertEquals("1", ingress.getId());
		assertEquals(blackClover.getRootUrl(), singleSeasonAnime.getUrl());
		assertEquals(sao1.getRootUrl(), multiSeasonsAnime.getUrl());
		assertEquals(sao2.getRootUrl(), multiSeasonsAnime.getUrl());
		assertEquals(sao3.getRootUrl(), multiSeasonsAnime.getUrl());
		assertEquals(sao7.getRootUrl(), multiSeasonsAnime.getUrl());
		assertEquals(ingress.getRootUrl(), announcement.getUrl());
		assertEquals(animediaOnlineTv + singleSeasonAnime.getUrl() + "/1/1", blackClover.getFullUrl());
		assertEquals(animediaOnlineTv + multiSeasonsAnime.getUrl() + "/1/1", sao1.getFullUrl());
		assertEquals(animediaOnlineTv + multiSeasonsAnime.getUrl() + "/2/1", sao2.getFullUrl());
		assertEquals(animediaOnlineTv + multiSeasonsAnime.getUrl() + "/3/1", sao3.getFullUrl());
		assertEquals(animediaOnlineTv + multiSeasonsAnime.getUrl() + "/7/1", sao7.getFullUrl());
		assertEquals(animediaOnlineTv + announcement.getUrl(), ingress.getFullUrl());
	}
	
	private AnimediaTitleSearchInfo getMultiSeasonsAnime() {
		return new AnimediaTitleSearchInfo("мастера меча онлайн",
				"мастера меча онлайн sword art online",
				"anime/mastera-mecha-onlayn",
				"http://static.animedia.tv/uploads/%D0%9C%D0%90%D0%A1%D0%A2%D0%95%D0%A0%D0%90.jpg?h=350&q=100");
	}
	
	private AnimediaTitleSearchInfo getSingleSeasonAnime() {
		return new AnimediaTitleSearchInfo("чёрный клевер",
				"чёрный клевер black clover черный клевер",
				"anime/chyornyj-klever",
				"http://static.animedia.tv/uploads/KLEVER.jpg?h=350&q=100");
	}
	
	private AnimediaTitleSearchInfo getAnnouncement() {
		return new AnimediaTitleSearchInfo("ингресс",
				"ингресс ingress the animation ingress the animation",
				"anime/ingress",
				"http://static.animedia.tv/uploads/450.jpg?h=350&q=100");
	}
}