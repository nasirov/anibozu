package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ReferencesManager.class, AnimediaRequestParametersBuilder.class, AnimediaHTMLParser.class, AppConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReferencesManagerTest extends AbstractTest {

	private static final String FAIRY_TAIL_ROOT_URL = "anime/skazka-o-hvoste-fei-TV1";

	private static final String SAO_ROOT_URL = "anime/mastera-mecha-onlayn";

	private static final String TITANS_ROOT_URL = "anime/vtorjenie-gigantov";

	@MockBean
	private HttpCaller httpCaller;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private ReferencesManager referencesManager;

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(referencesForTestResource), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(referencesFromGitHubUrl), eq(HttpMethod.GET), anyMap());
		List<AnimediaMALTitleReferences> multiSeasonsReferences = new ArrayList<>(referencesManager.getMultiSeasonsReferences());
		assertNotNull(multiSeasonsReferences);
		assertEquals(12, multiSeasonsReferences.size());
		List<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(ArrayList.class, false);
		assertEquals(multiSeasonsReferencesList.size(), multiSeasonsReferences.size());
		for (int i = 0; i < multiSeasonsReferences.size(); i++) {
			assertEquals(multiSeasonsReferences.get(i), multiSeasonsReferencesList.get(i));
		}
	}

	@Test
	public void updateReferences() throws Exception {
		String fairyTailId = "9480";
		String titansId = "9302";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + FAIRY_TAIL_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + SAO_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(titansHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + TITANS_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList1), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "1" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList2), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "2" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList3), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList7), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "7" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "1" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList2), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "2" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList3), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "7" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(titans3ConcretizedAndOngoing), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + titansId + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, false);
		referencesManager.updateReferences(multiSeasonsReferencesList);
		List<AnimediaMALTitleReferences> updatedMultiSeasonsReferencesList = new ArrayList<>(multiSeasonsReferencesList);
		List<AnimediaMALTitleReferences> updatedForCheck = getMultiSeasonsReferencesList(ArrayList.class, true);
		assertEquals(updatedMultiSeasonsReferencesList.size(), updatedForCheck.size());
		for (int i = 0; i < updatedMultiSeasonsReferencesList.size(); i++) {
			assertEquals(updatedMultiSeasonsReferencesList.get(i), updatedForCheck.get(i));
		}
		verify(httpCaller, never()).call(eq(animediaOnlineTv + "anime/velikiy-uchitel-onidzuka"), eq(HttpMethod.GET), anyMap());
		verify(httpCaller, never()).call(eq(animediaOnlineTv + "anime/vanpanchmen"), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void getMatchedReferences() throws Exception {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		multiSeasonsReferencesList.forEach(set -> assertNull(set.getPosterUrl()));
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles();
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager.getMatchedReferences(multiSeasonsReferencesList, watchingTitles);
		assertNotNull(matchedReferences);
		assertEquals(5, matchedReferences.size());
		matchedReferences.forEach(list -> assertEquals("testPoster", list.getPosterUrl()));
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("fairy tail: final series")).count());
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("sword art online: alicization")).count());
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("one punch man: road to hero")).count());
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("one punch man specials")).count());
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("shingeki no kyojin season 3 part 2")).count());
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
		boolean compareResult = referencesManager.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertTrue(compareResult);
		Anime missingReference = new Anime("2.5", saoUrl + "/8/1", SAO_ROOT_URL);
		multiSeasonsFromSearch.add(missingReference);
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		compareResult = referencesManager.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertFalse(compareResult);
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		assertTrue(RoutinesIO.unmarshalFromFile(prefix + tempRawReferencesName, Anime.class, ArrayList.class).get(0).equals(missingReference));
		RoutinesIO.removeDir(tempFolderName);
		String tempFileName = "test.txt";
		File tempFile = new File(tempFileName);
		tempFile.createNewFile();
		assertTrue(tempFile.exists());
		assertFalse(tempFile.isDirectory());
		ReflectionTestUtils.setField(referencesManager, "tempFolderName", tempFileName);
		referencesManager.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		FileSystemUtils.deleteRecursively(tempFile);
		assertFalse(tempFile.exists());
	}

	@Test
	public void updateReference() throws Exception {
		String fairyDataList = "3";
		String currentMax = "300";
		AnimediaMALTitleReferences animediaMALTitleReferences = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList(fairyDataList)
				.firstEpisode("").minConcretizedEpisodeOnAnimedia("").maxConcretizedEpisodeOnAnimedia("").currentMax(currentMax).posterUrl("").finalUrl("")
				.episodeNumberForWatch("").build();
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		assertEquals(1,
				multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(FAIRY_TAIL_ROOT_URL) && set.getDataList().equals(fairyDataList))
						.count());
		assertEquals(0,
				multiSeasonsReferencesList.stream()
						.filter(set -> set.getUrl().equals(FAIRY_TAIL_ROOT_URL) && set.getDataList().equals(fairyDataList) && set.getCurrentMax()
								.equals(currentMax)).count());
		referencesManager.updateCurrentMax(multiSeasonsReferencesList, animediaMALTitleReferences);
		assertEquals(1,
				multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(FAIRY_TAIL_ROOT_URL) && set.getDataList().equals(fairyDataList))
						.count());
		assertEquals(1,
				multiSeasonsReferencesList.stream()
						.filter(set -> set.getUrl().equals(FAIRY_TAIL_ROOT_URL) && set.getDataList().equals(fairyDataList) && set.getCurrentMax()
								.equals(currentMax)).count());
	}

	private Set<UserMALTitleInfo> getWatchingTitles() {
		Set<UserMALTitleInfo> userMALTitleInfo = new LinkedHashSet<>();
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series", 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "sword art online: alicization", 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "one punch man: road to hero", 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "one punch man specials", 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "shingeki no kyojin season 3 part 2", 0, "testPoster", "testUrl"));
		return userMALTitleInfo;
	}

	private <T extends Collection> T getMultiSeasonsReferencesList(Class<T> collection, boolean updated)
			throws IllegalAccessException, InstantiationException {
		T refs = collection.newInstance();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("1").firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175").titleOnMAL("fairy tail").build();
		AnimediaMALTitleReferences fairyTail2 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("2").firstEpisode("176")
				.minConcretizedEpisodeOnAnimedia("176").maxConcretizedEpisodeOnAnimedia("277").currentMax("277").titleOnMAL("fairy tail (2014)").build();
		AnimediaMALTitleReferences fairyTail3 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("3").firstEpisode("278")
				.minConcretizedEpisodeOnAnimedia("278").maxConcretizedEpisodeOnAnimedia("xxx").titleOnMAL("fairy tail: final series").build();
		AnimediaMALTitleReferences fairyTail7 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("7").firstEpisode("1")
				.titleOnMAL("fairy tail ova").build();
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("1").firstEpisode("1")
				.titleOnMAL("sword art online").build();
		AnimediaMALTitleReferences sao2 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("2").firstEpisode("1")
				.titleOnMAL("sword art online ii").build();
		AnimediaMALTitleReferences sao3 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("3").firstEpisode("1")
				.titleOnMAL("sword art online: alicization").build();
		AnimediaMALTitleReferences sao7 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("7").firstEpisode("1")
				.titleOnMAL("sword art online: extra edition").build();
		AnimediaMALTitleReferences none = AnimediaMALTitleReferences.builder().url("anime/velikiy-uchitel-onidzuka").dataList("1").firstEpisode("1")
				.titleOnMAL("none").build();
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		AnimediaMALTitleReferences titans3ConcretizedAndOngoing = AnimediaMALTitleReferences.builder().url(TITANS_ROOT_URL).dataList("3")
				.firstEpisode("13").titleOnMAL("shingeki no kyojin season 3 part 2").minConcretizedEpisodeOnAnimedia("13")
				.maxConcretizedEpisodeOnAnimedia("22").minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("10").build();
		if (updated) {
			fairyTail1.setCurrentMax("175");
			fairyTail2.setCurrentMax("277");
			fairyTail3.setCurrentMax("294");
			fairyTail7.setCurrentMax("4");
			sao1.setCurrentMax("25");
			sao2.setCurrentMax("24");
			sao3.setCurrentMax("16");
			sao7.setCurrentMax("1");
			fairyTail1.setFirstEpisode("1");
			fairyTail2.setFirstEpisode("176");
			fairyTail3.setFirstEpisode("278");
			fairyTail7.setFirstEpisode("1");
			sao1.setFirstEpisode("1");
			sao2.setFirstEpisode("1");
			sao3.setFirstEpisode("1");
			sao7.setFirstEpisode("1");
			fairyTail1.setMinConcretizedEpisodeOnAnimedia("1");
			fairyTail2.setMinConcretizedEpisodeOnAnimedia("176");
			fairyTail3.setMinConcretizedEpisodeOnAnimedia("278");
			fairyTail7.setMinConcretizedEpisodeOnAnimedia("1");
			sao1.setMinConcretizedEpisodeOnAnimedia("1");
			sao2.setMinConcretizedEpisodeOnAnimedia("1");
			sao3.setMinConcretizedEpisodeOnAnimedia("1");
			sao7.setMinConcretizedEpisodeOnAnimedia("1");
			fairyTail1.setMaxConcretizedEpisodeOnAnimedia("175");
			fairyTail2.setMaxConcretizedEpisodeOnAnimedia("277");
			fairyTail3.setMaxConcretizedEpisodeOnAnimedia("xxx");
			fairyTail7.setMaxConcretizedEpisodeOnAnimedia("4");
			sao1.setMaxConcretizedEpisodeOnAnimedia("25");
			sao2.setMaxConcretizedEpisodeOnAnimedia("24");
			sao3.setMaxConcretizedEpisodeOnAnimedia("24");
			sao7.setMaxConcretizedEpisodeOnAnimedia("1");
			titans3ConcretizedAndOngoing.setCurrentMax("13");
		}
		refs.add(fairyTail1);
		refs.add(fairyTail2);
		refs.add(fairyTail3);
		refs.add(fairyTail7);
		refs.add(sao1);
		refs.add(sao2);
		refs.add(sao3);
		refs.add(sao7);
		refs.add(none);
		refs.add(onePunch7);
		refs.add(onePunch7_2);
		refs.add(titans3ConcretizedAndOngoing);
		return refs;
	}
}