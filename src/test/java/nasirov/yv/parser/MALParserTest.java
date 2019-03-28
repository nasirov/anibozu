package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {MALParser.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MALParserTest extends AbstractTest {

	private static final String USER_ANIME_LIST_PRIVATE_ACCESS = "Access to this list has been restricted by the owner";

	@Autowired
	private MALParser malParser;

	@Test
	public void getUserTitlesInfo() throws Exception {
		Set<UserMALTitleInfo> userTitlesInfo = malParser
				.getUserTitlesInfo(new HttpResponse(RoutinesIO.readFromResource(testAccForDevWatchingTitles), HttpStatus.OK.value()), LinkedHashSet.class);
		assertNotNull(userTitlesInfo);
		assertEquals(300, userTitlesInfo.size());
	}

	@Test(expected = NullPointerException.class)
	public void getUserTitlesInfoHttpResponseIsNull() throws Exception {
		malParser.getUserTitlesInfo(null, LinkedHashSet.class);
	}

	@Test(expected = MALUserAnimeListAccessException.class)
	public void getUserTitlesInfoAnimeListHasPrivateAccess() throws Exception {
		malParser.getUserTitlesInfo(new HttpResponse(USER_ANIME_LIST_PRIVATE_ACCESS, HttpStatus.OK.value()), LinkedHashSet.class);
	}

	@Test(expected = JSONNotFoundException.class)
	public void getUserTitlesInfoHtmlWithoutJson() throws Exception {
		malParser.getUserTitlesInfo(new HttpResponse("", HttpStatus.OK.value()), LinkedHashSet.class);
	}

	@Test
	public void getNumWatchingTitles() throws Exception {
		String numWatchingTitlesString = malParser
				.getNumWatchingTitles(new HttpResponse(RoutinesIO.readFromResource(testAccForDevProfile), HttpStatus.OK.value()));
		assertNotNull(numWatchingTitlesString);
		assertNotEquals("", numWatchingTitlesString);
		int numWatchingTitles = Integer.parseInt(numWatchingTitlesString);
		assertEquals(TEST_ACC_WATCHING_TITLES, numWatchingTitles);
	}

	@Test(expected = NullPointerException.class)
	public void getNumWatchingTitlesHttpResponseIsNull() throws Exception {
		malParser.getNumWatchingTitles(null);
	}

	@Test(expected = MALUserAccountNotFoundException.class)
	public void getNumWatchingTitlesAccountIsNotFound() throws Exception {
		malParser.getNumWatchingTitles(new HttpResponse("", HttpStatus.NOT_FOUND.value()));
	}

	@Test
	public void getNumWatchingTitlesWatchingTitlesNotFound() throws Exception {
		String numWatchingTitles = malParser.getNumWatchingTitles(new HttpResponse("", HttpStatus.OK.value()));
		assertNull(numWatchingTitles);
	}
}