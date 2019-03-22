package nasirov.yv.service;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.UserMALTitleInfo;
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

import java.io.File;
import java.util.*;

import static nasirov.yv.enums.MALAnimeStatus.WATCHING;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ReferencesManager.class,
		AnimediaRequestParametersBuilder.class,
		AnimediaHTMLParser.class,
		AppConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReferencesManagerTest extends AbstractTest {
	@MockBean
	private HttpCaller httpCaller;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private ReferencesManager referencesManager;
	
	@Test
	public void getMultiSeasonsReferences() throws Exception {
		ReflectionTestUtils.setField(referencesManager, "rawReferencesResource", rawReferencesForTestResource);
		List<AnimediaMALTitleReferences> multiSeasonsReferences = new ArrayList<>(referencesManager.getMultiSeasonsReferences());
		assertNotNull(multiSeasonsReferences);
		assertEquals(8, multiSeasonsReferences.size());
		List<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(ArrayList.class, false);
		assertEquals(multiSeasonsReferencesList.size() - 1, multiSeasonsReferences.size());
		for (int i = 0; i < multiSeasonsReferences.size(); i++) {
			assertEquals(multiSeasonsReferences.get(0), multiSeasonsReferencesList.get(0));
		}
		Cache cache = cacheManager.getCache("multiSeasonsReferencesCache");
		assertNotNull(cache);
		cache.clear();
	}
	
	@Test
	public void updateReferences() throws Exception {
		String fairyTailId = "9480";
		String fairyTailUrl = "anime/skazka-o-hvoste-fei-TV1";
		String saoUrl = "anime/mastera-mecha-onlayn";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailHtml), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv + fairyTailUrl), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv + saoUrl), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList1), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + fairyTailId + "/" + "1"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList2), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + fairyTailId + "/" + "2"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList3), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + fairyTailId + "/" + "3"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList7), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + fairyTailId + "/" + "7"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/" + "1"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList2), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/" + "2"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList3), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/" + "3"), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + SAO_ID + "/" + "7"), eq(HttpMethod.GET), anyMap());
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, false);
		referencesManager.updateReferences(multiSeasonsReferencesList);
		List<AnimediaMALTitleReferences> updatedMultiSeasonsReferencesList = new ArrayList<>(multiSeasonsReferencesList);
		List<AnimediaMALTitleReferences> updatedForCheck = getMultiSeasonsReferencesList(ArrayList.class, true);
		assertEquals(updatedMultiSeasonsReferencesList.size(), updatedForCheck.size());
		for (int i = 0; i < updatedMultiSeasonsReferencesList.size(); i++) {
			assertEquals(updatedMultiSeasonsReferencesList.get(i), updatedForCheck.get(i));
		}
		verify(httpCaller, never()).call(eq(animediaOnlineTv + "none"), eq(HttpMethod.GET), anyMap());
	}
	
	@Test
	public void getMatchedReferences() throws Exception {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		multiSeasonsReferencesList.forEach(set -> assertNull(set.getPosterUrl()));
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles();
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager.getMatchedReferences(multiSeasonsReferencesList, watchingTitles);
		assertNotNull(matchedReferences);
		assertEquals(2, matchedReferences.size());
		matchedReferences.forEach(list -> assertEquals("testPoster", list.getPosterUrl()));
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("fairy tail: final series")).count());
		assertEquals(1, matchedReferences.stream().filter(set -> set.getTitleOnMAL().equals("sword art online: alicization")).count());
	}
	
	@Test
	public void checkReferences() throws Exception {
		RoutinesIO.removeDir(tempFolderName);
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		Set<Anime> multiSeasonsFromSearch = new LinkedHashSet<>();
		String fairyUrl = animediaOnlineTv + "anime/skazka-o-hvoste-fei-TV1/";
		String saoUrl = animediaOnlineTv + "anime/mastera-mecha-onlayn/";
		multiSeasonsFromSearch.add(new Anime("", fairyUrl + "1/1", ""));
		multiSeasonsFromSearch.add(new Anime("", fairyUrl + "2/176", ""));
		multiSeasonsFromSearch.add(new Anime("", fairyUrl + "3/278", ""));
		multiSeasonsFromSearch.add(new Anime("", fairyUrl + "7/1", ""));
		multiSeasonsFromSearch.add(new Anime("", saoUrl + "1/1", ""));
		multiSeasonsFromSearch.add(new Anime("", saoUrl + "2/1", ""));
		multiSeasonsFromSearch.add(new Anime("", saoUrl + "3/1", ""));
		multiSeasonsFromSearch.add(new Anime("", saoUrl + "7/1", ""));
		boolean compareResult = referencesManager.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertTrue(compareResult);
		String fullUrl = saoUrl + "8/1";
		multiSeasonsFromSearch.add(new Anime("", fullUrl, ""));
		assertFalse(RoutinesIO.isDirectoryExists(tempFolderName));
		compareResult = referencesManager.isReferencesAreFull(multiSeasonsFromSearch, multiSeasonsReferencesList);
		assertFalse(compareResult);
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		assertTrue(RoutinesIO.readFromFile(prefix + tempRawReferencesName).equals(fullUrl + System.lineSeparator()));
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
		String fairyUrl = "anime/skazka-o-hvoste-fei-TV1";
		String fairyDataList = "3";
		String currentMax = "300";
		AnimediaMALTitleReferences animediaMALTitleReferences = new AnimediaMALTitleReferences(fairyUrl, fairyDataList, "",
				"", "", "", currentMax, "", "", "");
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		assertEquals(1, multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(fairyUrl) && set.getDataList().equals(fairyDataList)).count());
		assertEquals(0, multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(fairyUrl) && set.getDataList().equals(fairyDataList) && set.getCurrentMax().equals(currentMax)).count());
		referencesManager.updateCurrentMax(multiSeasonsReferencesList, animediaMALTitleReferences);
		assertEquals(1, multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(fairyUrl) && set.getDataList().equals(fairyDataList)).count());
		assertEquals(1, multiSeasonsReferencesList.stream().filter(set -> set.getUrl().equals(fairyUrl) && set.getDataList().equals(fairyDataList) && set.getCurrentMax().equals(currentMax)).count());
	}
	
	private Set<UserMALTitleInfo> getWatchingTitles() {
		Set<UserMALTitleInfo> userMALTitleInfo = new LinkedHashSet<>();
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail: final series",
				0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "sword art online: alicization",
				0, "testPoster", "testUrl"));
		return userMALTitleInfo;
	}
	
	private <T extends Collection> T getMultiSeasonsReferencesList(Class<T> collection, boolean updated) throws IllegalAccessException, InstantiationException {
		T refs = collection.newInstance();
		AnimediaMALTitleReferences fairyTail1 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "1", "1", "fairy tail", "1", "175",
				null, null, null, null);
		AnimediaMALTitleReferences fairyTail2 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "2", "176", "fairy tail (2014)", "176", "277",
				null, null, null, null);
		AnimediaMALTitleReferences fairyTail3 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "3", "278", "fairy tail: final series", "278", "xxx",
				null, null, null, null);
		AnimediaMALTitleReferences fairyTail7 = new AnimediaMALTitleReferences("anime/skazka-o-hvoste-fei-TV1", "7", "1", "fairy tail ova", null, null,
				null, null, null, null);
		AnimediaMALTitleReferences sao1 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "1", "1", "sword art online", null, null,
				null, null, null, null);
		AnimediaMALTitleReferences sao2 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "2", "1", "sword art online ii", null, null,
				null, null, null, null);
		AnimediaMALTitleReferences sao3 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "3", "1", "sword art online: alicization", null, null,
				null, null, null, null);
		AnimediaMALTitleReferences sao7 = new AnimediaMALTitleReferences("anime/mastera-mecha-onlayn", "7", "1", "sword art online: extra edition", null, null,
				null, null, null, null);
		AnimediaMALTitleReferences none = new AnimediaMALTitleReferences("none", "", "", "none", null, null,
				null, null, null, null);
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
			fairyTail1.setMin("1");
			fairyTail2.setMin("176");
			fairyTail3.setMin("278");
			fairyTail7.setMin("1");
			sao1.setMin("1");
			sao2.setMin("1");
			sao3.setMin("1");
			sao7.setMin("1");
			fairyTail1.setMax("175");
			fairyTail2.setMax("277");
			fairyTail3.setMax("xxx");
			fairyTail7.setMax("4");
			sao1.setMax("25");
			sao2.setMax("24");
			sao3.setMax("24");
			sao7.setMax("1");
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
		return refs;
	}
}