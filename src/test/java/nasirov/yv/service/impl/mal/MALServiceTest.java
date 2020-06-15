package nasirov.yv.service.impl.mal;

import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.collect.Sets;
import feign.template.UriUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.exception.mal.MALForbiddenException;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class MALServiceTest extends AbstractTest {

	@Test
	public void getWatchingTitles() throws Exception {
		stubTestUser();
		List<MalTitle> watchingTitles = malService.getWatchingTitles(TEST_ACC_FOR_DEV);
		assertNotNull(watchingTitles);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitles.size());
		watchingTitles.forEach(this::checkTitle);
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesNotFound() throws Exception {
		createStubWithContent("/profile/" + TEST_ACC_FOR_DEV,
				TEXT_HTML_CHARSET_UTF_8,
				"Watching</a><span class=\"changed-class\">123</span>",
				OK.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	public void getWatchingTitlesEqualsZero() throws Exception {
		createStubWithBodyFile("/profile/" + TEST_ACC_FOR_DEV, TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfileWatchingTitles0.html");
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALUserAccountNotFoundException.class)
	public void getWatchingTitlesMALUserAccountNotFound() throws Exception {
		createStubWithContent("/profile/" + TEST_ACC_FOR_DEV, TEXT_HTML_CHARSET_UTF_8, "", NOT_FOUND.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALForbiddenException.class)
	public void getWatchingTitlesMALForbiddenException() throws Exception {
		createStubWithContent("/profile/" + TEST_ACC_FOR_DEV, TEXT_HTML_CHARSET_UTF_8, "", FORBIDDEN.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MALUserAnimeListAccessException.class)
	public void getWatchingTitlesMALUserAnimeListAccessException() throws Exception {
		createStubWithBodyFile("/profile/" + TEST_ACC_FOR_DEV, TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfile.html");
		createStubWithContent("/animelist/" + TEST_ACC_FOR_DEV + "/load.json?offset=0&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"{\"errors\":[{\"message\":\"invalid request\"}]}",
				BAD_REQUEST.value());
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test
	public void testIsTitleExistResultFound() {
		createStubWithBodyFile("/search/prefix.json?type=all&v=1&keyword=" + UriUtils.encode(REGULAR_TITLE_NAME, StandardCharsets.UTF_8),
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/searchRegularTitle.json");
		assertTrue(malService.isTitleExist(REGULAR_TITLE_NAME, REGULAR_TITLE_MAL_ANIME_ID));
	}

	@Test
	public void testIsTitleExistResultNotFound() {
		String notRegularTitleName = "notRegularTitleName";
		createStubWithBodyFile("/search/prefix.json?type=all&v=1&keyword=" + notRegularTitleName,
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/searchRegularTitle.json");
		assertFalse(malService.isTitleExist(notRegularTitleName, REGULAR_TITLE_MAL_ANIME_ID));
	}

	@Test
	public void testIsTitleExistBadRequest() {
		String keywordLargerThan100 = "keywordLargerThan100";
		createStubWithContent("/search/prefix.json?type=all&v=1&keyword=" + keywordLargerThan100,
				APPLICATION_JSON_CHARSET_UTF_8,
				"{\"errors\":[{\"message" + "\":\"Your keyword length must save less than or equal to 100.\"}]}",
				BAD_REQUEST.value());
		assertFalse(malService.isTitleExist(keywordLargerThan100, REGULAR_TITLE_MAL_ANIME_ID));
	}

	private void stubTestUser() {
		createStubWithBodyFile("/profile/" + TEST_ACC_FOR_DEV, TEXT_HTML_CHARSET_UTF_8, "mal/testAccForDevProfile.html");
		createStubWithBodyFile("/animelist/" + TEST_ACC_FOR_DEV + "/load.json?offset=0&status=1",
				APPLICATION_JSON_CHARSET_UTF_8,
				"mal/testAccForDevFirstJson300.json");
	}

	private void checkTitle(MalTitle title) {
		assertTrue(expectedTitles().contains(title.getName()));
		assertTrue(expectedPosters().contains(title.getPosterUrl()));
		assertTrue(expectedUrls().contains(title.getAnimeUrl()));
	}

	private Set<String> expectedTitles() {
		return Sets.newHashSet("\"bungaku shoujo\" kyou no oyatsu: hatsukoi", ".hack//g.u. trilogy");
	}

	private Set<String> expectedPosters() {
		return Sets.newHashSet("https://cdn.myanimelist.net/images/anime/2/79900.jpg", "https://cdn.myanimelist.net/images/anime/4/23083.jpg");
	}

	private Set<String> expectedUrls() {
		return Sets.newHashSet("https://myanimelist.net/anime/7669/Bungaku_Shoujo_Kyou_no_Oyatsu__Hatsukoi",
				"https://myanimelist.net/anime/3269/hack__GU_Trilogy");
	}
}