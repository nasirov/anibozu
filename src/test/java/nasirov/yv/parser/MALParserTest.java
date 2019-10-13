package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nasirov.yv.AbstractTest;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Created by nasirov.yv
 */

public class MALParserTest extends AbstractTest {

	@Autowired
	private MALParser malParser;

	@Test
	public void getNumWatchingTitles() {
		Integer numWatchingTitlesString = malParser
				.getNumWatchingTitles(new HttpResponse(RoutinesIO.readFromResource(testAccForDevProfile), HttpStatus.OK.value()));
		assertNotNull(numWatchingTitlesString);
		assertEquals(TEST_ACC_WATCHING_TITLES, numWatchingTitlesString.intValue());
	}

	@Test
	public void getNumWatchingTitlesWatchingTitlesNotFound() {
		Integer numWatchingTitles = malParser.getNumWatchingTitles(new HttpResponse("", HttpStatus.OK.value()));
		assertNull(numWatchingTitles);
	}
}