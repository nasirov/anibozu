package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AnimediaService.class, AnimediaHTMLParser.class, CacheManager.class, CacheConfiguration.class,
		AnimediaRequestParametersBuilder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unchecked")
public class AnimediaServiceTest extends AbstractTest {

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";


	@MockBean
	private HttpCaller httpCaller;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private AnimediaService animediaService;

	@Test
	public void testGetAnimediaSearchListFromAnimedia() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaAnimeListFromAnimediaUrl), eq(HttpMethod.GET), anyMap());
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromAnimedia();
		int fullSize = 787;
		assertNotNull(animediaSearchList);
		assertEquals(fullSize, animediaSearchList.size());
		assertEquals(fullSize, animediaSearchList.stream().filter(set -> set.getUrl().matches("^anime/.+")).count());
		assertEquals(fullSize,
				animediaSearchList.stream()
						.filter(set -> set.getPosterUrl().matches("https://static\\.animedia\\.tv/uploads/.+\\?" + POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER))
						.count());
		verify(httpCaller, times(1)).call(eq(animediaAnimeListFromAnimediaUrl), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void testGetAnimediaSearchListFromGitHub() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(animediaSearchListFull), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaAnimeListFromGitHubUrl), eq(HttpMethod.GET), anyMap());
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		int fullSize = 787;
		assertNotNull(animediaSearchList);
		assertEquals(fullSize, animediaSearchList.size());
		verify(httpCaller, times(1)).call(eq(animediaAnimeListFromGitHubUrl), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void testGetCurrentlyUpdatedTitles() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
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
	public void testGetSortedBySeasonAnime() throws Exception {
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		AnimediaTitleSearchInfo singleSeasonAnime = getSingleSeasonAnime();
		AnimediaTitleSearchInfo multiSeasonsAnime = getMultiSeasonsAnime();
		AnimediaTitleSearchInfo announcement = getAnnouncement();
		animediaTitleSearchInfo.add(singleSeasonAnime);
		animediaTitleSearchInfo.add(multiSeasonsAnime);
		animediaTitleSearchInfo.add(announcement);
		doReturn(new HttpResponse(RoutinesIO.readFromResource(blackCloverHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + singleSeasonAnime.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + multiSeasonsAnime.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(announcementHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + announcement.getUrl()), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/1" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList2), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/2" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList3), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/7" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedByType(animediaTitleSearchInfo);
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		compareResults(single, multi, announcements, singleSeasonAnime, multiSeasonsAnime, announcement);
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		List<Anime> singleFromCache = new ArrayList<>(sortedAnimediaSearchListCache
				.get(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), LinkedHashSet.class));
		List<Anime> multiFromCache = new ArrayList<>(sortedAnimediaSearchListCache.get(MULTISEASONS.getDescription(), LinkedHashSet.class));
		List<Anime> announcementsFromCache = new ArrayList<>(sortedAnimediaSearchListCache
				.get(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), LinkedHashSet.class));
		compareResults(singleFromCache, multiFromCache, announcementsFromCache, singleSeasonAnime, multiSeasonsAnime, announcement);
		String prefix = tempFolderName + File.separator;
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		File singleFile = new File(prefix + singleSeasonsAnimeUrls.getFilename());
		File multiFile = new File(prefix + multiSeasonsAnimeUrls.getFilename());
		File announcementsFile = new File(prefix + announcementsJson.getFilename());
		assertTrue(singleFile.exists());
		assertTrue(multiFile.exists());
		assertTrue(announcementsFile.exists());
		compareResults(RoutinesIO.unmarshalFromFile(singleFile, Anime.class, ArrayList.class),
				RoutinesIO.unmarshalFromFile(multiFile, Anime.class, ArrayList.class),
				RoutinesIO.unmarshalFromFile(announcementsFile, Anime.class, ArrayList.class),
				singleSeasonAnime,
				multiSeasonsAnime,
				announcement);
		RoutinesIO.removeDir(tempFolderName);
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
		Cache cache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		assertNotNull(cache);
		currentlyUpdatedTitles = cache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		assertEquals(1, currentlyUpdatedTitles.size());
		assertEquals(animediaMALTitleReferencesFresh.get(0), currentlyUpdatedTitles.get(0));
		cache.clear();
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmpty() {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
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
		doReturn(new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv), eq(HttpMethod.GET), anyMap());
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
	public void testGetAnimeFromEmptyCache() throws Exception {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		sortedAnimediaSearchListCache.clear();
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedByTypeFromResources();
		assertNotNull(sortedAnime);
		assertTrue(sortedAnime.isEmpty());
	}

	@Test
	public void testGetAnimeFromCacheNotEmpty() throws Exception {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		sortedAnimediaSearchListCache
				.put(SINGLESEASON.getDescription(), RoutinesIO.unmarshalFromResource(singleSeasonsAnimeUrls, Anime.class, LinkedHashSet.class));
		sortedAnimediaSearchListCache
				.put(MULTISEASONS.getDescription(), RoutinesIO.unmarshalFromResource(multiSeasonsAnimeUrls, Anime.class, LinkedHashSet.class));
		sortedAnimediaSearchListCache
				.put(ANNOUNCEMENT.getDescription(), RoutinesIO.unmarshalFromResource(announcementsJson, Anime.class, LinkedHashSet.class));
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedByTypeFromResources();
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		assertEquals(2, single.size());
		assertEquals(6, multi.size());
		assertEquals(2, announcements.size());
		sortedAnimediaSearchListCache.clear();
	}

	@Test
	public void testGetAnimeResourcesAreNull() throws Exception {
		ReflectionTestUtils.setField(animediaService, "announcementsUrls", null);
		ReflectionTestUtils.setField(animediaService, "multiSeasonsAnimeUrls", null);
		ReflectionTestUtils.setField(animediaService, "singleSeasonsAnimeUrls", null);
		ReflectionTestUtils.setField(animediaService, "tempFolderName", "classpath:notFound");
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = animediaService.getAnimeSortedByTypeFromResources();
		assertNotNull(sortedAnime);
		assertTrue(sortedAnime.isEmpty());
	}

	@Test
	public void testCheckAnime() throws Exception {
		Set<Anime> single = RoutinesIO.unmarshalFromResource(singleSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
		Set<Anime> multi = RoutinesIO.unmarshalFromResource(multiSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
		Set<Anime> announcement = RoutinesIO.unmarshalFromResource(announcementsJson, Anime.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> searchListForCheck = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		List<AnimediaTitleSearchInfo> notFound = new ArrayList<>(animediaService.checkSortedAnime(single, multi, announcement, searchListForCheck));
		assertNotNull(notFound);
		assertEquals(1, notFound.size());
		assertEquals("anime/domekano", notFound.get(0).getUrl());
	}

	@Test
	public void testIsAnimediaSearchListUpToDateRemovedTitle() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Iterator<AnimediaTitleSearchInfo> iterator = fresh.iterator();
		AnimediaTitleSearchInfo removedTitle = null;
		if (iterator.hasNext()) {
			removedTitle = new AnimediaTitleSearchInfo(iterator.next());
			iterator.remove();
		}
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		checkListsNotEquals(fresh, fromResources);
		String prefix = tempFolderName + File.separator;
		File tempRemovedTitles = new File(prefix + tempRemovedTitlesFromAnimediaSearchList);
		assertTrue(tempRemovedTitles.exists());
		assertEquals(removedTitle, RoutinesIO.unmarshalFromFile(tempRemovedTitles, AnimediaTitleSearchInfo.class, ArrayList.class).get(0));
		RoutinesIO.removeDir(tempFolderName);
	}

	@Test
	public void testIsAnimediaSearchListUpToDateNewTitle() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Iterator<AnimediaTitleSearchInfo> iterator = fromResources.iterator();
		AnimediaTitleSearchInfo newTitle = null;
		if (iterator.hasNext()) {
			newTitle = new AnimediaTitleSearchInfo(iterator.next());
			iterator.remove();
		}
		String prefix = tempFolderName + File.separator;
		checkListsNotEquals(fresh, fromResources);
		File tempNewTitles = new File(prefix + tempNewTitlesInAnimediaSearchList);
		assertTrue(tempNewTitles.exists());
		assertEquals(newTitle, RoutinesIO.unmarshalFromFile(tempNewTitles, AnimediaTitleSearchInfo.class, ArrayList.class).get(0));
		RoutinesIO.removeDir(tempFolderName);
	}

	@Test
	public void testIsAnimediaSearchListUpToDate() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		checkListsEquals(fresh, fromResources);
	}

	@Test
	public void testIsAnimediaSearchListUpToDateTempDirNotDir() throws IOException {
		String tempFileName = "test123.txt";
		File tempFile = new File(tempFileName);
		tempFile.createNewFile();
		assertTrue(tempFile.exists());
		assertFalse(tempFile.isDirectory());
		ReflectionTestUtils.setField(animediaService, "tempFolderName", tempFileName);
		Set<AnimediaTitleSearchInfo> fresh = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Iterator<AnimediaTitleSearchInfo> iterator = fresh.iterator();
		if (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		assertNotEquals(fresh, fromResources);
		assertFalse(animediaService.isAnimediaSearchListUpToDate(fromResources, fresh));
		FileSystemUtils.deleteRecursively(tempFile);
		assertFalse(tempFile.exists());
	}

	@Test
	public void testIsAnimediaSearchListDuplicatedUrlsInAnimediaSearchListFromResources() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		AnimediaTitleSearchInfo duplicate = new AnimediaTitleSearchInfo(fromResources.stream().findFirst().get());
		duplicate.setTitle(duplicate.getTitle() + "test");
		fromResources.add(duplicate);
		checkListsNotEquals(fresh, fromResources);
		String prefix = tempFolderName + File.separator;
		File tempDuplicates = new File(prefix + tempDuplicatedUrlsInAnimediaSearchList);
		assertTrue(tempDuplicates.exists());
		List<AnimediaTitleSearchInfo> titlesWithEqualsUrls = RoutinesIO.unmarshalFromFile(tempDuplicates, AnimediaTitleSearchInfo.class, ArrayList
				.class);
		assertEquals(2, titlesWithEqualsUrls.size());
		assertTrue(titlesWithEqualsUrls.contains(duplicate));
		assertTrue(titlesWithEqualsUrls.contains(fromResources.stream().filter(x -> x.getUrl().equals(duplicate.getUrl())).findFirst().get()));
		RoutinesIO.removeDir(tempFolderName);
	}

	@Test
	public void testIsAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources() throws IOException {
		RoutinesIO.removeDir(tempFolderName);
		Set<AnimediaTitleSearchInfo> fromResources = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		Anime ingress = new Anime("1.1", animediaOnlineTv + "anime/ingress/1/1", "anime/ingress");
		Set<Anime> singleSeason = new LinkedHashSet<>();
		singleSeason.add(ingress);
		assertTrue(animediaService.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeason, fromResources));
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		AnimediaTitleSearchInfo titleWithRusKeywords = fromResources.stream().filter(title -> title.getUrl().equals(ingress.getRootUrl())).findFirst()
				.get();
		titleWithRusKeywords.setKeywords(titleWithRusKeywords.getKeywords() + " ингресс");
		assertFalse(animediaService.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeason,
				fromResources));
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		File tempRusKeywords = new File(prefix + tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList);
		assertTrue(tempRusKeywords.exists());
		assertEquals(titleWithRusKeywords, RoutinesIO.unmarshalFromFile(tempRusKeywords, AnimediaTitleSearchInfo.class, ArrayList.class).get(0));
		RoutinesIO.removeDir(tempFolderName);
		String tempFileName = "test123.txt";
		File tempFile = new File(tempFileName);
		tempFile.createNewFile();
		assertTrue(tempFile.exists());
		assertFalse(tempFile.isDirectory());
		ReflectionTestUtils.setField(animediaService, "tempFolderName", tempFileName);
		assertFalse(animediaService.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeason,
				fromResources));
		FileSystemUtils.deleteRecursively(tempFile);
		assertFalse(tempFile.exists());
	}

	private void checkListsNotEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertNotEquals(fresh, fromResources);
		assertFalse(animediaService.isAnimediaSearchListUpToDate(fromResources, fresh));
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
	}

	private void checkListsEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		assertEquals(fresh, fromResources);
		assertTrue(animediaService.isAnimediaSearchListUpToDate(fromResources, fresh));
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
	}

	private void compareResults(List<Anime> single, List<Anime> multi, List<Anime> announcements, AnimediaTitleSearchInfo singleSeasonAnime,
			AnimediaTitleSearchInfo multiSeasonsAnime, AnimediaTitleSearchInfo announcement) {
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
		return new AnimediaTitleSearchInfo("чёрный клевер", "black clover",
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