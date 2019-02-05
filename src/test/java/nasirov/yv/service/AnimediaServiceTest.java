package nasirov.yv.service;

import com.sun.research.ws.wadl.HTTPMethods;
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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;


/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AnimediaService.class,
		AnimediaHTMLParser.class,
		WrappedObjectMapper.class,
		CacheManager.class,
		AppConfiguration.class,
		URLBuilder.class,
		RoutinesIO.class,
		AnimediaRequestParametersBuilder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AnimediaServiceTest extends AbstractTest{
	
	@MockBean
	private HttpCaller httpCaller;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	@Autowired
	private AnimediaService animediaService;
	
	@Test
	public void testGetAnimediaSearchList() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaAnimeList), eq(HTTPMethods.GET), any(Map.class));
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
		assertNotNull(animediaSearchList);
		assertEquals(780,animediaSearchList.size());
		long count = animediaSearchList.stream().filter(set -> set.getUrl().startsWith(animediaOnlineTv)).count();
		assertEquals(count, 0);
	}
	
	@Test
	public void testGetCurrentlyUpdatedTitles() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HTTPMethods.GET), any(Map.class));
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		assertNotNull(currentlyUpdatedTitles);
		assertEquals(currentlyUpdatedTitles.size(), 10);
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFromCache = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertNotNull(animediaMALTitleReferencesFromCache);
		assertEquals(animediaMALTitleReferencesFromCache.size(), 10);
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
		String saoId = "9432";
		doReturn(new HttpResponse(routinesIO.readFromResource(singleSeasonHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + singleSeasonAnime.getUrl()), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(saoHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + multiSeasonsAnime.getUrl()), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(announcementHtml), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaOnlineTv + announcement.getUrl()), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(sao1), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + saoId + "/1"), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(sao2), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + saoId + "/2"), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(sao3), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + saoId + "/3"), eq(HTTPMethods.GET), any(Map.class));
		doReturn(new HttpResponse(routinesIO.readFromResource(sao7), HttpStatus.OK.value()))
				.when(httpCaller).call(eq(animediaEpisodesList + saoId + "/7"), eq(HTTPMethods.GET), any(Map.class));
		List<Set<Anime>> sortedForSeasonAnime = animediaService.getSortedForSeasonAnime(animediaTitleSearchInfo);
		assertNotNull(sortedForSeasonAnime);
		assertEquals(sortedForSeasonAnime.size(), 3);
		List<Anime> single = new ArrayList<>(sortedForSeasonAnime.get(0));
		List<Anime> multi = new ArrayList<>(sortedForSeasonAnime.get(1));
		List<Anime> announcements = new ArrayList<>(sortedForSeasonAnime.get(2));
		compareResults(single, multi, announcements, singleSeasonAnime, multiSeasonsAnime, announcement);
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		List<Anime> singleFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), LinkedHashSet.class));
		List<Anime> multiFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.MULTISEASONS.getDescription(), LinkedHashSet.class));
		List<Anime> announcementsFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), LinkedHashSet.class));
		compareResults(singleFromCache, multiFromCache, announcementsFromCache, singleSeasonAnime, multiSeasonsAnime, announcement);
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesDifferentValues() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HTTPMethods.GET), any(Map.class));
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFresh = new ArrayList<>();
		currentlyUpdatedTitles.forEach(list -> animediaMALTitleReferencesFresh.add(new AnimediaMALTitleReferences(list)));
		animediaMALTitleReferencesFresh.add(0, animediaMALTitleReferencesFresh.get(9));
		animediaMALTitleReferencesFresh.remove(9);
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(animediaMALTitleReferencesFresh, currentlyUpdatedTitles);
		assertNotNull(result);
		assertEquals(result.size(), 1);
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		currentlyUpdatedTitles = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertEquals(currentlyUpdatedTitles.size(), 1);
		assertEquals(animediaMALTitleReferencesFresh.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesNullValues() {
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(null, null);
		assertNotNull(result);
		assertEquals(result.size(), 0);
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmpty() {
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HTTPMethods.GET), any(Map.class));
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(currentlyUpdatedTitles,new ArrayList<>());
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
		doReturn(new HttpResponse(routinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv), eq(HTTPMethods.GET), any(Map.class));
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> result;
		result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(),currentlyUpdatedTitles);
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
	public void testGetAnime() throws Exception {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		sortedAnimediaSearchListCache.clear();
		doReturn(new HttpResponse(routinesIO.readFromResource(animediaSearchListForCheck), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaAnimeList), eq(HTTPMethods.GET), any(Map.class));
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
		assertNotNull(animediaSearchList);
		assertEquals(7, animediaSearchList.size());
		ReflectionTestUtils.setField(animediaService,"resourceAnnouncementsUrls",announcementsJson);
		ReflectionTestUtils.setField(animediaService,"resourceMultiSeasonsAnimeUrls",multiSeasonsAnimeUrls);
		ReflectionTestUtils.setField(animediaService,"resourceSingleSeasonsAnimeUrls",singleSeasonsAnimeUrls);
		List<Set<Anime>> sortedAnime = animediaService.getAnime(animediaSearchList);
		assertNotNull(sortedAnime);
		assertEquals(sortedAnime.size(), 3);
		List<Anime> single = new ArrayList<>(sortedAnime.get(0));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(1));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(2));
		assertEquals(2,single.size());
		assertEquals(6,multi.size());
		assertEquals(2, announcements.size());
	}
	
	@Test
	public void testCheckAnime() throws Exception {
		Set<Anime> single = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(singleSeasonsAnimeUrls), Anime.class, LinkedHashSet.class);
		Set<Anime> multi = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(multiSeasonsAnimeUrls), Anime.class, LinkedHashSet.class);
		Set<Anime> announcement = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(announcementsJson), Anime.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> searchListForCheck = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(animediaSearchListForCheck), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		List<AnimediaTitleSearchInfo> notFound = new ArrayList<>(animediaService.checkAnime(single, multi, announcement, searchListForCheck));
		assertNotNull(notFound);
		assertEquals(notFound.size(), 1);
		assertEquals(notFound.get(0).getUrl(), "anime/domekano");
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
		assertEquals(single.size(), 1);
		assertEquals(multi.size(), 4);
		assertEquals(announcements.size(), 1);
		Anime blackClover = single.get(0);
		Anime sao1 = multi.get(0);
		Anime sao2 = multi.get(1);
		Anime sao3 = multi.get(2);
		Anime sao7 = multi.get(3);
		Anime ingress = announcements.get(0);
		assertEquals(blackClover.getId(), "1");
		assertEquals(sao1.getId(), "1.1");
		assertEquals(sao2.getId(), "1.2");
		assertEquals(sao3.getId(), "1.3");
		assertEquals(sao7.getId(), "1.4");
		assertEquals(ingress.getId(), "1");
		assertEquals(singleSeasonAnime.getUrl(), blackClover.getRootUrl());
		assertEquals(multiSeasonsAnime.getUrl(), sao1.getRootUrl());
		assertEquals(multiSeasonsAnime.getUrl(), sao2.getRootUrl());
		assertEquals(multiSeasonsAnime.getUrl(), sao3.getRootUrl());
		assertEquals(multiSeasonsAnime.getUrl(), sao7.getRootUrl());
		assertEquals(announcement.getUrl(), ingress.getRootUrl());
		assertEquals(blackClover.getFullUrl(), animediaOnlineTv + singleSeasonAnime.getUrl() + "/1/1");
		assertEquals(sao1.getFullUrl(), animediaOnlineTv + multiSeasonsAnime.getUrl() + "/1/1");
		assertEquals(sao2.getFullUrl(), animediaOnlineTv + multiSeasonsAnime.getUrl() + "/2/1");
		assertEquals(sao3.getFullUrl(), animediaOnlineTv + multiSeasonsAnime.getUrl() + "/3/1");
		assertEquals(sao7.getFullUrl(), animediaOnlineTv + multiSeasonsAnime.getUrl() + "/7/1");
		assertEquals(ingress.getFullUrl(), animediaOnlineTv + announcement.getUrl());
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