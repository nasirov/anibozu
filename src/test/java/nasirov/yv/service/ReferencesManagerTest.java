package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_URL;
import static nasirov.yv.utils.TestConstants.FAIRY_TAIL_ID;
import static nasirov.yv.utils.TestConstants.FAIRY_TAIL_ROOT_URL;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_ID;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_URL;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static nasirov.yv.utils.TestUtils.getMultiSeasonsReferencesList;
import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */

public class ReferencesManagerTest extends AbstractTest {

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/references.json", TEXT_PLAIN_CHARSET_UTF_8, "github/referencesForTest.json");
		Set<AnimediaMALTitleReferences> multiSeasonsReferences = referencesManager.getMultiSeasonsReferences();
		List<AnimediaMALTitleReferences> expected = getMultiSeasonsReferencesList(ArrayList.class, false);
		assertEquals(expected.size(), multiSeasonsReferences.size());
		multiSeasonsReferences.forEach(x -> assertTrue(expected.contains(x)));
	}

	@Test
	public void updateReferences() throws Exception {
		stubAnimedia();
		Set<AnimediaMALTitleReferences> referencesForUpdate = getMultiSeasonsReferencesList(LinkedHashSet.class, false);
		referencesManager.updateReferences(referencesForUpdate);
		List<AnimediaMALTitleReferences> expectedUpdatedReferences = getMultiSeasonsReferencesList(ArrayList.class, true);
		assertEquals(expectedUpdatedReferences.size(), referencesForUpdate.size());
		referencesForUpdate.forEach(x -> assertTrue(expectedUpdatedReferences.contains(x)));
	}

	@Test
	public void getMatchedReferences() throws Exception {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		multiSeasonsReferencesList.forEach(set -> assertNull(set.getPosterUrl()));
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles();
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager.getMatchedReferences(multiSeasonsReferencesList, watchingTitles);
		assertEquals(5, matchedReferences.size());
		assertTrue(matchedReferences.stream()
				.allMatch(x -> x.getPosterUrl()
						.equals("testPoster")));
		checkMatchedReferences(matchedReferences, "fairy tail: final series");
		checkMatchedReferences(matchedReferences, "sword art online: alicization");
		checkMatchedReferences(matchedReferences, "one punch man: road to hero");
		checkMatchedReferences(matchedReferences, "one punch man specials");
		checkMatchedReferences(matchedReferences, "shingeki no kyojin season 3 part 2");
	}

	@Test
	public void updateReference() throws Exception {
		String fairyDataList = "3";
		String currentMax = "300";
		AnimediaMALTitleReferences animediaMALTitleReferences = AnimediaMALTitleReferences.builder()
				.url(FAIRY_TAIL_ROOT_URL)
				.dataList(fairyDataList)
				.currentMax(currentMax)
				.build();
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getMultiSeasonsReferencesList(LinkedHashSet.class, true);
		checkUpdatedCurrentMax(multiSeasonsReferencesList, FAIRY_TAIL_ROOT_URL, fairyDataList, currentMax, 0);
		referencesManager.updateCurrentMaxAndEpisodesRange(multiSeasonsReferencesList, animediaMALTitleReferences);
		checkUpdatedCurrentMax(multiSeasonsReferencesList, FAIRY_TAIL_ROOT_URL, fairyDataList, currentMax, 1);
	}

	private void stubAnimedia() {
		stubAnimeMainPageAndDataLists(FAIRY_TAIL_ROOT_URL,
				"animedia/fairyTail/fairyTailHtml.txt",
				FAIRY_TAIL_ID,
				of("1",
						"animedia/fairyTail/fairyTail1.txt",
						"2",
						"animedia/fairyTail/fairyTail2.txt",
						"3",
						"animedia/fairyTail/fairyTail3.txt",
						"7",
						"animedia/fairyTail/fairyTail7.txt"));
		stubAnimeMainPageAndDataLists(MULTI_SEASONS_TITLE_URL,
				"animedia/sao/saoHtml.txt",
				MULTI_SEASONS_TITLE_ID,
				of("1", "animedia/sao/sao1.txt", "2", "animedia/sao/sao2.txt", "3", "animedia/sao/sao3.txt", "7", "animedia/sao/sao7.txt"));
		stubAnimeMainPageAndDataLists(CONCRETIZED_AND_ONGOING_TITLE_URL,
				"animedia/titans/titansHtml.txt",
				CONCRETIZED_AND_ONGOING_TITLE_ID,
				of("3", "animedia/titans/titans3ConcretizedAndOngoing.txt"));
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

	private void checkMatchedReferences(Set<AnimediaMALTitleReferences> matchedReferences, String titleOnMal) {
		assertEquals(1,
				matchedReferences.stream()
						.filter(set -> set.getTitleOnMAL()
								.equals(titleOnMal))
						.count());
	}

	private void checkUpdatedCurrentMax(Set<AnimediaMALTitleReferences> multiSeasonsReferencesList, String url, String dataList, String currentMax,
			int expectedCountWithUpdatedCurrentMax) {
		assertEquals(1,
				multiSeasonsReferencesList.stream()
						.filter(set -> set.getUrl()
								.equals(url) && set.getDataList()
								.equals(dataList))
						.count());
		assertEquals(expectedCountWithUpdatedCurrentMax,
				multiSeasonsReferencesList.stream()
						.filter(set -> set.getUrl()
								.equals(url) && set.getDataList()
								.equals(dataList) && set.getCurrentMax()
								.equals(currentMax))
						.count());
	}
}