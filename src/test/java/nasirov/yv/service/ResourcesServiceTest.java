package nasirov.yv.service;

import static java.util.Collections.emptyMap;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ALL_TYPES;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getAnnouncement;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getMultiSeasonsAnime;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getSingleSeasonAnime;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.FAIRY_TAIL_ROOT_URL;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_ID;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_URL;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_ANIME_URL;
import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static nasirov.yv.utils.TestUtils.getMultiSeasonsReferencesList;
import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
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
import nasirov.yv.utils.AnimediaSearchListBuilder;
import org.apache.groovy.util.Maps;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * Created by nasirov.yv
 */

public class ResourcesServiceTest extends AbstractTest {

	@Test
	public void testGetSortedBySeasonAnime() throws Exception {
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = getAnimediaTitleSearchInfoForTest();
		AnimediaTitleSearchInfo singleSeasonAnime = getSingleSeasonAnime();
		AnimediaTitleSearchInfo multiSeasonsAnime = getMultiSeasonsAnime();
		AnimediaTitleSearchInfo announcement = getAnnouncement();
		stubAnimedia();
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = resourcesService.getAnimeSortedByType(animediaTitleSearchInfo);
		assertNotNull(sortedAnime);
		assertEquals(3, sortedAnime.size());
		compareResults(new ArrayList<>(sortedAnime.get(SINGLESEASON)),
				new ArrayList<>(sortedAnime.get(MULTISEASONS)),
				new ArrayList<>(sortedAnime.get(ANNOUNCEMENT)),
				singleSeasonAnime,
				multiSeasonsAnime,
				announcement);
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = sortedAnimediaSearchListCache.get(ALL_TYPES.getDescription(), EnumMap.class);
		compareResults(new ArrayList<>(allTypes.get(SINGLESEASON)),
				new ArrayList<>(allTypes.get(MULTISEASONS)),
				new ArrayList<>(allTypes.get(ANNOUNCEMENT)),
				singleSeasonAnime,
				multiSeasonsAnime,
				announcement);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		assertTrue(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		File singleFile = new File(prefix + singleSeasonsAnimeUrls.getFilename());
		File multiFile = new File(prefix + multiSeasonsAnimeUrls.getFilename());
		File announcementsFile = new File(prefix + announcementsUrls.getFilename());
		assertTrue(singleFile.exists());
		assertTrue(multiFile.exists());
		assertTrue(announcementsFile.exists());
		compareResults(routinesIO.unmarshalFromFile(singleFile, Anime.class, ArrayList.class),
				routinesIO.unmarshalFromFile(multiFile, Anime.class, ArrayList.class),
				routinesIO.unmarshalFromFile(announcementsFile, Anime.class, ArrayList.class),
				singleSeasonAnime,
				multiSeasonsAnime,
				announcement);
		routinesIO.removeDir(TEMP_FOLDER_NAME);
	}

	@Test
	public void testGetAnimeFromEmptyCache() {
		Map<AnimeTypeOnAnimedia, Set<Anime>> sortedAnime = resourcesService.getAnimeSortedByTypeFromCache();
		assertNotNull(sortedAnime);
		assertTrue(sortedAnime.isEmpty());
	}

	@Test
	public void testGetAnimeFromCacheNotEmpty() {
		Map<AnimeTypeOnAnimedia, Set<Anime>> map = Maps.of(SINGLESEASON,
				getAnime(singleSeasonsAnimeUrls),
				MULTISEASONS,
				getAnime(multiSeasonsAnimeUrls),
				ANNOUNCEMENT,
				getAnime(announcementsUrls));
		Map<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = new EnumMap<>(map);
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
	}

	@Test
	public void testCheckAnime() {
		Set<Anime> single = getAnime(singleSeasonsAnimeUrls);
		Set<Anime> multi = getAnime(multiSeasonsAnimeUrls);
		Set<Anime> announcement = getAnime(announcementsUrls);
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = Maps.of(SINGLESEASON, single, MULTISEASONS, multi, ANNOUNCEMENT, announcement);
		Set<AnimediaTitleSearchInfo> searchListForCheck = getAnimediaSearchList();
		List<AnimediaTitleSearchInfo> notFound = new ArrayList<>(resourcesService.checkSortedAnime(allTypes, searchListForCheck));
		assertNotNull(notFound);
		assertEquals(1, notFound.size());
		assertEquals("anime/domekano",
				notFound.get(0)
						.getUrl());
	}

	@Test
	public void testIsAnimediaSearchListUpToDateRemovedTitle() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = getAnimediaSearchList();
		Iterator<AnimediaTitleSearchInfo> iterator = fresh.iterator();
		AnimediaTitleSearchInfo removedTitle = null;
		if (iterator.hasNext()) {
			removedTitle = new AnimediaTitleSearchInfo(iterator.next());
			iterator.remove();
		}
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		checkListsNotEquals(fresh, fromResources);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		File tempRemovedTitles = new File(prefix + resourcesNames.getTempRemovedTitlesFromAnimediaSearchList());
		assertTrue(tempRemovedTitles.exists());
		assertEquals(removedTitle,
				routinesIO.unmarshalFromFile(tempRemovedTitles, AnimediaTitleSearchInfo.class, ArrayList.class)
						.get(0));
		routinesIO.removeDir(TEMP_FOLDER_NAME);
	}

	@Test
	public void testIsAnimediaSearchListUpToDateNewTitle() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = getAnimediaSearchList();
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		Iterator<AnimediaTitleSearchInfo> iterator = fromResources.iterator();
		AnimediaTitleSearchInfo newTitle = null;
		if (iterator.hasNext()) {
			newTitle = new AnimediaTitleSearchInfo(iterator.next());
			iterator.remove();
		}
		String prefix = TEMP_FOLDER_NAME + File.separator;
		checkListsNotEquals(fresh, fromResources);
		File tempNewTitles = new File(prefix + resourcesNames.getTempNewTitlesInAnimediaSearchList());
		assertTrue(tempNewTitles.exists());
		assertEquals(newTitle,
				routinesIO.unmarshalFromFile(tempNewTitles, AnimediaTitleSearchInfo.class, ArrayList.class)
						.get(0));
		routinesIO.removeDir(TEMP_FOLDER_NAME);
	}

	@Test
	public void testIsAnimediaSearchListUpToDate() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = getAnimediaSearchList();
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		checkListsEquals(fresh, fromResources);
	}

	@Test
	public void testIsAnimediaSearchListUpToDateTempDirNotDir() {
		Set<AnimediaTitleSearchInfo> fresh = getAnimediaSearchList();
		Iterator<AnimediaTitleSearchInfo> iterator = fresh.iterator();
		if (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		assertNotEquals(fresh, fromResources);
		assertFalse(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
	}

	@Test
	public void testIsAnimediaSearchListDuplicatedUrlsInAnimediaSearchListFromResources() throws NotDirectoryException {
		Set<AnimediaTitleSearchInfo> fresh = getAnimediaSearchList();
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		AnimediaTitleSearchInfo duplicate = new AnimediaTitleSearchInfo(fromResources.stream()
				.findFirst()
				.get());
		duplicate.setTitle(duplicate.getTitle() + "test");
		fromResources.add(duplicate);
		checkListsNotEquals(fresh, fromResources);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		File tempDuplicates = new File(prefix + resourcesNames.getTempDuplicatedUrlsInAnimediaSearchList());
		assertTrue(tempDuplicates.exists());
		List<AnimediaTitleSearchInfo> titlesWithEqualsUrls = routinesIO.unmarshalFromFile(tempDuplicates, AnimediaTitleSearchInfo.class,
				ArrayList.class);
		assertEquals(2, titlesWithEqualsUrls.size());
		assertTrue(titlesWithEqualsUrls.contains(duplicate));
		assertTrue(titlesWithEqualsUrls.contains(fromResources.stream()
				.filter(x -> x.getUrl()
						.equals(duplicate.getUrl()))
				.findFirst()
				.get()));
		routinesIO.removeDir(TEMP_FOLDER_NAME);
	}

	@Test
	public void testIsAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources() throws IOException {
		routinesIO.removeDir(TEMP_FOLDER_NAME);
		Set<AnimediaTitleSearchInfo> fromResources = getAnimediaSearchList();
		Anime ingress = new Anime("1.1", ANIMEDIA_ONLINE_TV + "anime/ingress/1/1", "anime/ingress");
		Set<Anime> singleSeason = new LinkedHashSet<>();
		singleSeason.add(ingress);
		assertTrue(resourcesService.isAllSingleSeasonAnimeHasConcretizedMALTitleName(singleSeason, fromResources));
		assertFalse(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		AnimediaTitleSearchInfo titleWithRusKeywords = fromResources.stream()
				.filter(title -> title.getUrl()
						.equals(ingress.getRootUrl()))
				.findFirst()
				.get();
		titleWithRusKeywords.setKeywords(titleWithRusKeywords.getKeywords() + " ингресс");
		assertFalse(resourcesService.isAllSingleSeasonAnimeHasConcretizedMALTitleName(singleSeason, fromResources));
		assertTrue(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		String prefix = TEMP_FOLDER_NAME + File.separator;
		File tempRusKeywords = new File(prefix + resourcesNames.getTempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList());
		assertTrue(tempRusKeywords.exists());
		assertEquals(titleWithRusKeywords,
				routinesIO.unmarshalFromFile(tempRusKeywords, AnimediaTitleSearchInfo.class, ArrayList.class)
						.get(0));
		routinesIO.removeDir(TEMP_FOLDER_NAME);
	}

	@Test
	public void checkReferences() throws Exception {
		routinesIO.removeDir(TEMP_FOLDER_NAME);
		String fairyUrl = ANIMEDIA_ONLINE_TV + FAIRY_TAIL_ROOT_URL;
		String saoUrl = ANIMEDIA_ONLINE_TV + MULTI_SEASONS_TITLE_URL;
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		Set<Anime> multiSeasonsFromSearch = Sets.newHashSet(new Anime("1.1", fairyUrl + "/1/1", FAIRY_TAIL_ROOT_URL),
				new Anime("1.2", fairyUrl + "/2" + "/176", FAIRY_TAIL_ROOT_URL),
				new Anime("1.3", fairyUrl + "/3/278", FAIRY_TAIL_ROOT_URL),
				new Anime("1.4", fairyUrl + "/7/1", FAIRY_TAIL_ROOT_URL),
				new Anime("2.1", saoUrl + "/1/1", MULTI_SEASONS_TITLE_URL),
				new Anime("2.2", saoUrl + "/2/1", MULTI_SEASONS_TITLE_URL),
				new Anime("2.3", saoUrl + "/3/1", MULTI_SEASONS_TITLE_URL),
				new Anime("2.4", saoUrl + "/7/1", MULTI_SEASONS_TITLE_URL));
		boolean compareResult = resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertTrue(compareResult);
		Anime missingReference = new Anime("2.5", saoUrl + "/8/1", MULTI_SEASONS_TITLE_URL);
		multiSeasonsFromSearch.add(missingReference);
		assertFalse(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		compareResult = resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertFalse(compareResult);
		assertTrue(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		String prefix = TEMP_FOLDER_NAME + File.separator;
		assertEquals(routinesIO.unmarshalFromFile(prefix + resourcesNames.getTempRawReferences(), Anime.class, ArrayList.class)
				.get(0), missingReference);
		routinesIO.removeDir(TEMP_FOLDER_NAME);
		resourcesService.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
	}

	private void checkListsNotEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertNotEquals(fresh, fromResources);
		assertFalse(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
		assertTrue(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
	}

	private void checkListsEquals(Set<AnimediaTitleSearchInfo> fresh, Set<AnimediaTitleSearchInfo> fromResources) throws NotDirectoryException {
		assertFalse(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
		assertEquals(fresh, fromResources);
		assertTrue(resourcesService.isAnimediaSearchListFromGitHubUpToDate(fromResources, fresh));
		assertFalse(routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
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
		assertEquals(ANIMEDIA_ONLINE_TV + singleSeasonAnime.getUrl() + "/1/1", blackClover.getFullUrl());
		assertEquals(ANIMEDIA_ONLINE_TV + multiSeasonsAnime.getUrl() + "/1/1", sao1.getFullUrl());
		assertEquals(ANIMEDIA_ONLINE_TV + multiSeasonsAnime.getUrl() + "/2/1", sao2.getFullUrl());
		assertEquals(ANIMEDIA_ONLINE_TV + multiSeasonsAnime.getUrl() + "/3/1", sao3.getFullUrl());
		assertEquals(ANIMEDIA_ONLINE_TV + multiSeasonsAnime.getUrl() + "/7/1", sao7.getFullUrl());
		assertEquals(ANIMEDIA_ONLINE_TV + announcement.getUrl(), ingress.getFullUrl());
	}

	private void stubAnimedia() {
		stubAnimeMainPageAndDataLists(SINGLE_SEASON_ANIME_URL, "animedia/blackClover/blackCloverHtml.txt", null, emptyMap());
		stubAnimeMainPageAndDataLists(MULTI_SEASONS_TITLE_URL,
				"animedia/sao/saoHtml.txt",
				MULTI_SEASONS_TITLE_ID,
				of("1", "animedia/sao/sao1.txt", "2", "animedia/sao/sao2.txt", "3", "animedia/sao/sao3.txt", "7", "animedia/sao/sao7.txt"));
		stubAnimeMainPageAndDataLists("anime/ingress", "animedia/ingressHtml.txt", null, emptyMap());
	}

	private Set<Anime> getAnime(Resource resource) {
		return routinesIO.unmarshalFromResource(resource, Anime.class, LinkedHashSet.class);
	}

	private Set<AnimediaTitleSearchInfo> getAnimediaTitleSearchInfoForTest() {
		return AnimediaSearchListBuilder.getAnimediaSearchList();
	}

	private Set<AnimediaTitleSearchInfo> getAnimediaSearchList() {
		return routinesIO.unmarshalFromResource(animediaSearchListForCheck, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}
}