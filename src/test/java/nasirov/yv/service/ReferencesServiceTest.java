package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class ReferencesServiceTest extends AbstractTest {

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		stubGitHub();
		Set<TitleReference> multiSeasonsReferences = referencesService.getReferences();
		List<TitleReference> expected = getReferences(ArrayList.class, false);
		assertEquals(expected.size(), multiSeasonsReferences.size());
		multiSeasonsReferences.forEach(x -> assertTrue(expected.contains(x)));
	}

	@Test
	public void updateReferences() throws Exception {
		stubAnimedia();
		Set<TitleReference> referencesForUpdate = getReferences(LinkedHashSet.class, false);
		referencesService.updateReferences(referencesForUpdate);
		List<TitleReference> expectedUpdatedReferences = getReferences(ArrayList.class, true);
		assertEquals(expectedUpdatedReferences.size(), referencesForUpdate.size());
		referencesForUpdate.forEach(x -> assertTrue(expectedUpdatedReferences.contains(x)));
	}

	@Test
	public void getMatchedReferences() throws Exception {
		stubGitHub();
		Set<TitleReference> references = getReferences(LinkedHashSet.class, true);
		references.forEach(set -> assertNull(set.getPosterUrlOnMAL()));
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles();
		Set<TitleReference> matchedReferences = referencesService.getMatchedReferences(watchingTitles, references);
		assertEquals(4, matchedReferences.size());
		assertTrue(matchedReferences.stream()
				.allMatch(x -> x.getPosterUrlOnMAL()
						.equals("testPoster")));
		checkMatchedReferences(matchedReferences, REGULAR_TITLE_NAME);
		checkMatchedReferences(matchedReferences, ANNOUNCEMENT_TITLE_NAME);
		checkMatchedReferences(matchedReferences, CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME);
		checkMatchedReferences(matchedReferences, CONCRETIZED_AND_ONGOING_TITLE_NAME);
	}

	private void checkMatchedReferences(Set<TitleReference> matchedReferences, String titleOnMal) {
		assertEquals(1,
				matchedReferences.stream()
						.filter(set -> set.getTitleNameOnMAL()
								.equals(titleOnMal))
						.count());
	}

	private void stubAnimedia() {
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID, "animedia/regular/regularTitle.json", of("1", "animedia/regular/regularTitleDataList1.json"));
		stubAnimeMainPageAndDataLists(CONCRETIZED_AND_ONGOING_TITLE_ID,
				"animedia/concretized/concretizedAndOngoingTitle.json",
				of("3", "animedia/concretized/concretizedAndOngoingTitleDataList3.json"));
		stubAnimeMainPageAndDataLists(ANNOUNCEMENT_TITLE_ID,
				"animedia/announcement/announcementTitle.json",
				of("1", "animedia/announcement" + "/announcementTitleDataList1.json"));
	}

	private void stubGitHub() {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/references.json", TEXT_PLAIN_CHARSET_UTF_8, "github/references.json");
	}

	private Set<UserMALTitleInfo> getWatchingTitles() {
		Set<UserMALTitleInfo> userMALTitleInfo = new LinkedHashSet<>();
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, REGULAR_TITLE_NAME, 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, ANNOUNCEMENT_TITLE_NAME, 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, CONCRETIZED_TITLE_WITH_EPISODES_RANGE_NAME, 0, "testPoster", "testUrl"));
		userMALTitleInfo.add(new UserMALTitleInfo(0, WATCHING.getCode(), 0, CONCRETIZED_AND_ONGOING_TITLE_NAME, 0, "testPoster", "testUrl"));
		return userMALTitleInfo;
	}

}