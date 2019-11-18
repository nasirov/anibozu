package nasirov.yv.service;

import static nasirov.yv.TestUtils.getMultiSeasonsReferencesList;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ALL_TYPES;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Created by nasirov.yv
 */

public class ResourcesServiceTest extends AbstractTest {

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
	private ResourcesServiceI resourcesService;


	@Test
	public void testGetSortedBySeasonAnime() throws Exception {
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = getAnimediaTitleSearchInfoForTest();
		AnimediaTitleSearchInfo singleSeasonAnime = getSingleSeasonAnime();
		AnimediaTitleSearchInfo multiSeasonsAnime = getMultiSeasonsAnime();
		AnimediaTitleSearchInfo announcement = getAnnouncement();
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
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = resourcesService.getAnimeSortedByType(animediaTitleSearchInfo);
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		List<Anime> single = new ArrayList<>(sortedAnime.get(SINGLESEASON));
		List<Anime> multi = new ArrayList<>(sortedAnime.get(MULTISEASONS));
		List<Anime> announcements = new ArrayList<>(sortedAnime.get(ANNOUNCEMENT));
		compareResults(single, multi, announcements, singleSeasonAnime, multiSeasonsAnime, announcement);
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = sortedAnimediaSearchListCache.get(ALL_TYPES.getDescription(), EnumMap.class);
		List<Anime> singleFromCache = new ArrayList<>(allTypes.get(SINGLESEASON));
		List<Anime> multiFromCache = new ArrayList<>(allTypes.get(MULTISEASONS));
		List<Anime> announcementsFromCache = new ArrayList<>(allTypes.get(ANNOUNCEMENT));
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
	public void testGetAnimeFromEmptyCache() {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		sortedAnimediaSearchListCache.clear();
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = resourcesService.getAnimeSortedByTypeFromCache();
		assertNotNull(sortedAnime);
		assertTrue(sortedAnime.isEmpty());
	}
	@Test
	public void testGetAnimeFromCacheNotEmpty() {
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		EnumMap<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = new EnumMap<>(AnimeTypeOnAnimedia.class);
		allSeasons.put(SINGLESEASON, RoutinesIO.unmarshalFromResource(singleSeasonsAnimeUrls, Anime.class, LinkedHashSet.class));
		allSeasons.put(MULTISEASONS, RoutinesIO.unmarshalFromResource(multiSeasonsAnimeUrls, Anime.class, LinkedHashSet.class));
		allSeasons.put(ANNOUNCEMENT, RoutinesIO.unmarshalFromResource(announcementsJson, Anime.class, LinkedHashSet.class));
		sortedAnimediaSearchListCache.put(ALL_TYPES.getDescription(), allSeasons);
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = resourcesService.getAnimeSortedByTypeFromCache();
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
	public void testCheckAnime() {
		Set<Anime> single = RoutinesIO.unmarshalFromResource(singleSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
		Set<Anime> multi = RoutinesIO.unmarshalFromResource(multiSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
		Set<Anime> announcement = RoutinesIO.unmarshalFromResource(announcementsJson, Anime.class, LinkedHashSet.class);
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = new EnumMap(AnimeTypeOnAnimedia.class);
		allTypes.put(SINGLESEASON, single);
		allTypes.put(MULTISEASONS, multi);
		allTypes.put(ANNOUNCEMENT, announcement);
		Set<AnimediaTitleSearchInfo> searchListForCheck = RoutinesIO
				.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		List<AnimediaTitleSearchInfo> notFound = new ArrayList<>(resourcesService.checkSortedAnime(allTypes, searchListForCheck));
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
		File tempRemovedTitles = new File(prefix + resourcesNames.getTempRemovedTitlesFromAnimediaSearchList());
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
		File tempNewTitles = new File(prefix + resourcesNames.getTempNewTitlesInAnimediaSearchList());
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
		assertFalse(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
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
		File tempDuplicates = new File(prefix + resourcesNames.getTempDuplicatedUrlsInAnimediaSearchList());
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
		assertTrue(resourcesService.isAllSingleSeasonAnimeHasConcretizedMALTitleName(singleSeason, fromResources));
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		AnimediaTitleSearchInfo titleWithRusKeywords = fromResources.stream().filter(title -> title.getUrl().equals(ingress.getRootUrl())).findFirst()
				.get();
		titleWithRusKeywords.setKeywords(titleWithRusKeywords.getKeywords() + " ингресс");
		assertFalse(resourcesService.isAllSingleSeasonAnimeHasConcretizedMALTitleName(singleSeason, fromResources));
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		File tempRusKeywords = new File(prefix + resourcesNames.getTempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList());
		assertTrue(tempRusKeywords.exists());
		assertEquals(titleWithRusKeywords, RoutinesIO.unmarshalFromFile(tempRusKeywords, AnimediaTitleSearchInfo.class, ArrayList.class).get(0));
		RoutinesIO.removeDir(tempFolderName);
	}
	@Test
	public void checkReferences() throws Exception {
		RoutinesIO.removeDir(tempFolderName);
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		Set<Anime> multiSeasonsFromSearch = new LinkedHashSet<>();
		String fairyUrl = animediaOnlineTv + FAIRY_TAIL_ROOT_URL;
		String saoUrl = animediaOnlineTv + SAO_ROOT_URL;
		multiSeasonsFromSearch.add(new Anime("1.1", fairyUrl + "/1/1", FAIRY_TAIL_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("1.2", fairyUrl + "/2/176", FAIRY_TAIL_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("1.3", fairyUrl + "/3/278", FAIRY_TAIL_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("1.4", fairyUrl + "/7/1", FAIRY_TAIL_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("2.1", saoUrl + "/1/1", SAO_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("2.2", saoUrl + "/2/1", SAO_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("2.3", saoUrl + "/3/1", SAO_ROOT_URL));
		multiSeasonsFromSearch.add(new Anime("2.4", saoUrl + "/7/1", SAO_ROOT_URL));
		boolean compareResult = resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertTrue(compareResult);
		Anime missingReference = new Anime("2.5", saoUrl + "/8/1", SAO_ROOT_URL);
		multiSeasonsFromSearch.add(missingReference);
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		compareResult = resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertFalse(compareResult);
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		assertTrue(RoutinesIO.unmarshalFromFile(prefix + resourcesNames.getTempRawReferences(), Anime.class, ArrayList.class).get(0)
				.equals(missingReference));
		RoutinesIO.removeDir(tempFolderName);
		resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
	}
	private void checkListsNotEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertNotEquals(fresh, fromResources);
		assertFalse(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
	}
	private void checkListsEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		assertEquals(fresh, fromResources);
		assertTrue(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
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

	private Set<AnimediaTitleSearchInfo> getAnimediaTitleSearchInfoForTest() {
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		animediaTitleSearchInfo.add(getSingleSeasonAnime());
		animediaTitleSearchInfo.add(getMultiSeasonsAnime());
		animediaTitleSearchInfo.add(getAnnouncement());
		return animediaTitleSearchInfo;
	}

	private AnimediaTitleSearchInfo getMultiSeasonsAnime() {
		return new AnimediaTitleSearchInfo("мастера меча онлайн",
				"мастера меча онлайн sword art online",
				"anime/mastera-mecha-onlayn",
				"http://static.animedia.tv/uploads/%D0%9C%D0%90%D0%A1%D0%A2%D0%95%D0%A0%D0%90.jpg?h=350&q=100");
	}

	private AnimediaTitleSearchInfo getSingleSeasonAnime() {
		return new AnimediaTitleSearchInfo("чёрный клевер",
				"black clover",
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