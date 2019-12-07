package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */


public class MALServiceTest extends AbstractTest {

	private static final String TEST_ACC_FOR_DEV = "testaccfordev";

	private static final String CACHED_ANIME_LIST = "cached";

	private static final String NEW_TITLE_IN_ANIME_LIST = "newTitleInAnimeList";

	private static final String TITLE_REMOVED_FROM_ANIME_LIST = "titleRemovedFromAnimeList";

	private static final String NUMBER_OF_WATCHED_EPISODES_INCREASED = "numberOfWatchedEpisodesIncreased";

	private static final String NUMBER_OF_WATCHED_EPISODES_DECREASED = "numberOfWatchedEpisodesDecreased";

	@Test
	public void getWatchingTitles() throws Exception {
		stubTestUser();
		Set<UserMALTitleInfo> watchingTitles = malService.getWatchingTitles(TEST_ACC_FOR_DEV);
		assertNotNull(watchingTitles);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitles.size());
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(TEST_ACC_FOR_DEV, LinkedHashSet.class);
		assertNotNull(watchingTitlesFromCache);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitlesFromCache.size());
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesNotFound() throws Exception {
		createStubWithContent("/profile/testaccfordev", TEXT_HTML_CHARSET_UTF_8, "Watching</a><span class=\"changed-class\">123</span>", OK.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesEqualsZero() throws Exception {
		createStubWithBodyFile("/profile/testaccfordev", TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfileWatchingTitles0.txt");
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALUserAccountNotFoundException.class)
	public void getWatchingTitlesMALUserAccountNotFound() throws Exception {
		createStubWithContent("/profile/testaccfordev", TEXT_HTML_CHARSET_UTF_8, "", NOT_FOUND.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALUserAnimeListAccessException.class)
	public void getWatchingTitlesMALUserAnimeListAccessException() throws Exception {
		createStubWithBodyFile("/profile/testaccfordev", TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfile.txt");
		createStubWithContent("/animelist/testaccfordev/load.json?offset=0&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"{\"errors\":[{\"message\":\"invalid request\"}]}",
				BAD_REQUEST.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test
	public void isWatchingTitlesUpdatedEquals() {
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		assertFalse(malService.isWatchingTitlesUpdated(cached, cached));
		assertEquals(cached, new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST)));
	}

	@Test
	public void isWatchingTitlesUpdatedNumberOfWatchedEpisodesIncreased() {
		checkTitlesWithUpdatedNumberOfWatchedEpisodes(NUMBER_OF_WATCHED_EPISODES_INCREASED);
	}

	@Test
	public void isWatchingTitlesUpdatedNumberOfWatchedEpisodesDecreased() {
		checkTitlesWithUpdatedNumberOfWatchedEpisodes(NUMBER_OF_WATCHED_EPISODES_DECREASED);
	}

	@Test
	public void isWatchingTitlesUpdatedNewTitleInAnimeList() {
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		Set<UserMALTitleInfo> animeListWithNewTitle = new LinkedHashSet<>(getWatchingTitlesForTest(NEW_TITLE_IN_ANIME_LIST));
		assertNotEquals(cached, animeListWithNewTitle);
		UserMALTitleInfo newTitle = getWatchingTitlesForTest(NEW_TITLE_IN_ANIME_LIST).get(3);
		assertFalse(cached.contains(newTitle));
		assertTrue(malService.isWatchingTitlesUpdated(animeListWithNewTitle, cached));
		assertEquals(cached, animeListWithNewTitle);
		assertTrue(cached.contains(newTitle));
		assertEquals(new ArrayList<>(cached).get(3), newTitle);
	}

	@Test
	public void isWatchingTitlesUpdatedTitleRemovedFromAnimeList() {
		List<UserMALTitleInfo> cachedList = getWatchingTitlesForTest(CACHED_ANIME_LIST);
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(cachedList);
		Set<UserMALTitleInfo> animeListWithRemovedTitle = new LinkedHashSet<>(getWatchingTitlesForTest(TITLE_REMOVED_FROM_ANIME_LIST));
		assertNotEquals(cached, animeListWithRemovedTitle);
		UserMALTitleInfo removedTitle = cachedList.get(0);
		assertFalse(animeListWithRemovedTitle.contains(removedTitle));
		assertTrue(malService.isWatchingTitlesUpdated(animeListWithRemovedTitle, cached));
		assertEquals(cached, animeListWithRemovedTitle);
		assertFalse(cached.contains(removedTitle));
		assertFalse(animeListWithRemovedTitle.contains(removedTitle));
	}

	@Test
	public void watchingTitlesWithoutUpdates() {
		UserMALTitleInfo titleForTest = getTitleForTest(0);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes =
				malService.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Sets.newHashSet(
				titleForTest), Sets.newHashSet(titleForTest));
		assertTrue(watchingTitlesWithUpdatedNumberOfWatchedEpisodes.isEmpty());
	}

	@Test
	public void watchingTitlesWithIncreasedNumberOfWatchedEpisodes() {
		UserMALTitleInfo titleForTest = getTitleForTest(0);
		UserMALTitleInfo titleForTestWithIncreasedNumOfWtchedEpisodes = getTitleForTest(10);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes =
				malService.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Sets.newHashSet(
				titleForTest,
				titleForTestWithIncreasedNumOfWtchedEpisodes), Sets.newHashSet(titleForTest));
		checkUpdatedTitle(titleForTestWithIncreasedNumOfWtchedEpisodes, watchingTitlesWithUpdatedNumberOfWatchedEpisodes);
	}

	@Test
	public void watchingTitlesWithDecreasedNumberOfWatchedEpisodes() {
		UserMALTitleInfo titleForTest = getTitleForTest(10);
		UserMALTitleInfo titleForTestWithDecreasedNumberOfWatchedEpisodes = getTitleForTest(5);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes =
				malService.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Sets.newHashSet(
				titleForTest,
				titleForTestWithDecreasedNumberOfWatchedEpisodes), Sets.newHashSet(titleForTest));
		checkUpdatedTitle(titleForTestWithDecreasedNumberOfWatchedEpisodes, watchingTitlesWithUpdatedNumberOfWatchedEpisodes);
	}

	@Test
	public void testIsTitleExistResultFound() {
		createStubWithBodyFile("/search/prefix.json?type=all&v=1&keyword=fairy%20tail", APPLICATION_JSON_CHARSET_UTF_8, "mal/searchTitleFairyTail.json");
		assertTrue(malService.isTitleExist("fairy tail"));
	}

	@Test
	public void testIsTitleExistResultNotFound() {
		createStubWithBodyFile("/search/prefix.json?type=all&v=1&keyword=notFairyTail", APPLICATION_JSON_CHARSET_UTF_8, "mal/searchTitleNotFound.json");
		assertFalse(malService.isTitleExist("notFairyTail"));
	}

	@Test
	public void testIsTitleExistBadRequest() {
		createStubWithContent("/search/prefix.json?type=all&v=1&keyword=notFairyTail",
				APPLICATION_JSON_CHARSET_UTF_8,
				"{\"errors\":[{\"message" + "\":\"Your keyword length must save less than or equal to 100.\"}]}",
				BAD_REQUEST.value());
		assertFalse(malService.isTitleExist("notFairyTail"));
	}

	private void stubTestUser() {
		createStubWithBodyFile("/profile/testaccfordev", TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfile.txt");
		createStubWithBodyFile("/animelist/testaccfordev/load.json?offset=0&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/testAccForDevFirstJson300.json");
		createStubWithBodyFile("/animelist/testaccfordev/load.json?offset=300&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/testAccForDevAdditionalJsonMoreThan300.json");
		createStubWithBodyFile("/animelist/testaccfordev/load.json?offset=600&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/testAccForDevAdditionalJsonMoreThan600.json");
		createStubWithBodyFile("/animelist/testaccfordev/load.json?offset=900&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/testAccForDevAdditionalJsonMoreThan900.json");
	}

	private void checkUpdatedTitle(UserMALTitleInfo updatedTitle, Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes) {
		assertEquals(1, watchingTitlesWithUpdatedNumberOfWatchedEpisodes.size());
		assertEquals(1,
				watchingTitlesWithUpdatedNumberOfWatchedEpisodes.stream()
						.filter(title -> title.equals(updatedTitle))
						.count());
	}

	private void checkTitlesWithUpdatedNumberOfWatchedEpisodes(String typeOfChange) {
		Set<UserMALTitleInfo> increasedWatchedEpisodes = new LinkedHashSet<>(getWatchingTitlesForTest(typeOfChange));
		Set<UserMALTitleInfo> cached = new LinkedHashSet<>(getWatchingTitlesForTest(CACHED_ANIME_LIST));
		UserMALTitleInfo updatedTitle = getWatchingTitlesForTest(typeOfChange).get(0);
		assertNotEquals(increasedWatchedEpisodes, cached);
		assertNotEquals(updatedTitle.getNumWatchedEpisodes(),
				new ArrayList<>(cached).get(0)
						.getNumWatchedEpisodes());
		assertTrue(malService.isWatchingTitlesUpdated(increasedWatchedEpisodes, cached));
		assertEquals(updatedTitle.getNumWatchedEpisodes(),
				new ArrayList<>(cached).get(0)
						.getNumWatchedEpisodes());
		assertEquals(increasedWatchedEpisodes, cached);
	}

	private UserMALTitleInfo getTitleForTest(int numWatchedEpisodes) {
		return new UserMALTitleInfo(0,
				WATCHING.getCode(),
				numWatchedEpisodes,
				"accel world",
				24,
				"https://myanimelist.cdn-dena.com/images/anime/8/38155.webp",
				"https://myanimelist.net//anime/11759/Accel_World");
	}

	private List<UserMALTitleInfo> getWatchingTitlesForTest(String type) {
		List<UserMALTitleInfo> watchingTitles = new ArrayList<>();
		UserMALTitleInfo realGirl = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				5,
				"3d kanojo: real girl",
				12,
				"https://myanimelist.cdn-dena.com/images/anime/1327/93616.webp",
				"https://myanimelist.net//anime/36793/3D_Kanojo__Real_Girl");
		UserMALTitleInfo accelWorld = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"accel world",
				24,
				"https://myanimelist.cdn-dena.com/images/anime/8/38155.webp",
				"https://myanimelist.net//anime/11759/Accel_World");
		UserMALTitleInfo angelBeats = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"angel beats!",
				13,
				"https://myanimelist.cdn-dena.com/images/anime/10/22061.webp",
				"https://myanimelist.net//anime/6547/Angel_Beats");
		UserMALTitleInfo noragami = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				"noragami",
				12,
				"https://myanimelist.cdn-dena.com/images/anime/9/77809.webp",
				"https://myanimelist.net//anime/20507/Noragami");
		switch (type) {
			case CACHED_ANIME_LIST:
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NUMBER_OF_WATCHED_EPISODES_INCREASED:
				realGirl.setNumWatchedEpisodes(10);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NUMBER_OF_WATCHED_EPISODES_DECREASED:
				realGirl.setNumWatchedEpisodes(0);
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
			case NEW_TITLE_IN_ANIME_LIST:
				watchingTitles.add(realGirl);
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				watchingTitles.add(noragami);
				break;
			case TITLE_REMOVED_FROM_ANIME_LIST:
				watchingTitles.add(accelWorld);
				watchingTitles.add(angelBeats);
				break;
		}
		return watchingTitles;
	}
}