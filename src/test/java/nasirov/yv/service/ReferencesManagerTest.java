package nasirov.yv.service;

import static nasirov.yv.TestUtils.getEpisodesRange;
import static nasirov.yv.TestUtils.getMultiSeasonsReferencesList;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ReferencesService.class, AnimediaRequestParametersBuilder.class, CacheConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReferencesManagerTest extends AbstractTest {

	@Value("classpath:animedia/fairyTail/fairyTailHtml.txt")
	private Resource fairyTailHtml;

	@Value("classpath:animedia/fairyTail/fairyTail1.txt")
	private Resource fairyTailDataList1;

	@Value("classpath:animedia/fairyTail/fairyTail2.txt")
	private Resource fairyTailDataList2;

	@Value("classpath:animedia/fairyTail/fairyTail3.txt")
	private Resource fairyTailDataList3;

	@Value("classpath:animedia/fairyTail/fairyTail7.txt")
	private Resource fairyTailDataList7;

	@Value("classpath:animedia/titans/titans3ConcretizedAndOngoing.txt")
	private Resource titans3ConcretizedAndOngoing;

	@Value("classpath:animedia/titans/titansHtml.txt")
	private Resource titansHtml;

	@Value("classpath:referencesForTest.json")
	private Resource referencesForTestResource;

	@MockBean
	private HttpCaller httpCaller;

	@MockBean
	private AnimediaHTMLParser animediaHTMLParser;

	@Autowired
	private ReferencesServiceI referencesManager;

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		doReturn(new HttpResponse(RoutinesIO.readFromResource(referencesForTestResource), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(urlsNames.getGitHubUrls().getRawGithubusercontentComReferences()), eq(HttpMethod.GET), anyMap());
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
		HttpResponse fairyTailResponse = new HttpResponse(RoutinesIO.readFromResource(fairyTailHtml), HttpStatus.OK.value());
		HttpResponse saoHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value());
		HttpResponse titansHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(titansHtml), HttpStatus.OK.value());
		HttpResponse fairyTailDataList1Response = new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList1), HttpStatus.OK.value());
		HttpResponse fairyTailDataList2Response = new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList2), HttpStatus.OK.value());
		HttpResponse fairyTailDataList3Response = new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList3), HttpStatus.OK.value());
		HttpResponse fairyTailDataList7Response = new HttpResponse(RoutinesIO.readFromResource(fairyTailDataList7), HttpStatus.OK.value());
		HttpResponse saoDataList1Response = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		HttpResponse saoDataList2Response = new HttpResponse(RoutinesIO.readFromResource(saoDataList2), HttpStatus.OK.value());
		HttpResponse saoDataList3Response = new HttpResponse(RoutinesIO.readFromResource(saoDataList3), HttpStatus.OK.value());
		HttpResponse saoDataList7Response = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		HttpResponse titans3ConcretizedAndOngoingResponse = new HttpResponse(RoutinesIO.readFromResource(titans3ConcretizedAndOngoing),
				HttpStatus.OK.value());
		doReturn(fairyTailResponse).when(httpCaller).call(eq(animediaOnlineTv + FAIRY_TAIL_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(saoHtmlResponse).when(httpCaller).call(eq(animediaOnlineTv + SAO_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(titansHtmlResponse).when(httpCaller).call(eq(animediaOnlineTv + TITANS_ROOT_URL), eq(HttpMethod.GET), anyMap());
		doReturn(fairyTailDataList1Response).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "1" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(fairyTailDataList2Response).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "2" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(fairyTailDataList3Response).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(fairyTailDataList7Response).when(httpCaller)
				.call(eq(animediaEpisodesList + fairyTailId + "/" + "7" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(saoDataList1Response).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "1" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(saoDataList2Response).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "2" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(saoDataList3Response).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(saoDataList7Response).when(httpCaller)
				.call(eq(animediaEpisodesList + SAO_ID + "/" + "7" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(titans3ConcretizedAndOngoingResponse).when(httpCaller)
				.call(eq(animediaEpisodesList + titansId + "/" + "3" + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		Map<String, Map<String, String>> fairyTailEpisodesRangeAnimeIdSeasonsAndEpisodes = new HashMap<>();
		Map<String, Map<String, String>> saoHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes = new HashMap<>();
		Map<String, Map<String, String>> titansHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes = new HashMap<>();
		Map<String, String> fairyTailSeasonsAndEpisodes = new HashMap<>();
		Map<String, String> saoSeasonsAndEpisodes = new HashMap<>();
		Map<String, String> titansSeasonsAndEpisodes = new HashMap<>();
		Map<String, List<String>> fairyTailDataList1EpisodesRange = new HashMap<>();
		Map<String, List<String>> fairyTailDataList2EpisodesRange = new HashMap<>();
		Map<String, List<String>> fairyTailDataList3EpisodesRange = new HashMap<>();
		Map<String, List<String>> fairyTailDataList7EpisodesRange = new HashMap<>();
		Map<String, List<String>> saoDataList1EpisodesRange = new HashMap<>();
		Map<String, List<String>> saoDataList2EpisodesRange = new HashMap<>();
		Map<String, List<String>> saoDataList3EpisodesRange = new HashMap<>();
		Map<String, List<String>> saoDataList7EpisodesRange = new HashMap<>();
		Map<String, List<String>> titans3ConcretizedAndOngoingEpisodesRange = new HashMap<>();
		fairyTailSeasonsAndEpisodes.put("1", "175");
		fairyTailSeasonsAndEpisodes.put("2", "277");
		fairyTailSeasonsAndEpisodes.put("3", "xxx");
		fairyTailSeasonsAndEpisodes.put("7", "1");
		saoSeasonsAndEpisodes.put("1", "25");
		saoSeasonsAndEpisodes.put("2", "24");
		saoSeasonsAndEpisodes.put("3", "24");
		saoSeasonsAndEpisodes.put("7", "4");
		titansSeasonsAndEpisodes.put("1", "25");
		titansSeasonsAndEpisodes.put("2", "12");
		titansSeasonsAndEpisodes.put("3", "22");
		titansSeasonsAndEpisodes.put("7", "1");
		fairyTailEpisodesRangeAnimeIdSeasonsAndEpisodes.put(fairyTailId, fairyTailSeasonsAndEpisodes);
		saoHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes.put(SAO_ID, saoSeasonsAndEpisodes);
		titansHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes.put(titansId, titansSeasonsAndEpisodes);
		fairyTailDataList1EpisodesRange.put("175", getEpisodesRange("1", "175"));
		fairyTailDataList2EpisodesRange.put("277", getEpisodesRange("176", "277"));
		fairyTailDataList3EpisodesRange.put("xxx", getEpisodesRange("278", "294"));
		fairyTailDataList7EpisodesRange.put("4", getEpisodesRange("1", "4"));
		saoDataList1EpisodesRange.put("25", getEpisodesRange("1", "25"));
		saoDataList2EpisodesRange.put("24", getEpisodesRange("1", "24"));
		saoDataList3EpisodesRange.put("24", getEpisodesRange("1", "16"));
		saoDataList7EpisodesRange.put("1", getEpisodesRange("1", "1"));
		titans3ConcretizedAndOngoingEpisodesRange.put("13", getEpisodesRange("1", "13"));
		doReturn(fairyTailEpisodesRangeAnimeIdSeasonsAndEpisodes).when(animediaHTMLParser).getAnimeIdDataListsAndMaxEpisodesMap(eq(fairyTailResponse));
		doReturn(saoHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes).when(animediaHTMLParser).getAnimeIdDataListsAndMaxEpisodesMap(eq(saoHtmlResponse));
		doReturn(titansHtmlEpisodesRangeAnimeIdSeasonsAndEpisodes).when(animediaHTMLParser).getAnimeIdDataListsAndMaxEpisodesMap(eq(titansHtmlResponse));
		doReturn(fairyTailDataList1EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(fairyTailDataList1Response));
		doReturn(fairyTailDataList2EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(fairyTailDataList2Response));
		doReturn(fairyTailDataList3EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(fairyTailDataList3Response));
		doReturn(fairyTailDataList7EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(fairyTailDataList7Response));
		doReturn(saoDataList1EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(saoDataList1Response));
		doReturn(saoDataList2EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(saoDataList2Response));
		doReturn(saoDataList3EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(saoDataList3Response));
		doReturn(saoDataList7EpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(saoDataList7Response));
		doReturn(titans3ConcretizedAndOngoingEpisodesRange).when(animediaHTMLParser).getEpisodesRange(eq(titans3ConcretizedAndOngoingResponse));
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

}