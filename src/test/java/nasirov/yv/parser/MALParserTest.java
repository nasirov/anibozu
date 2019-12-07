package nasirov.yv.parser;

import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */

public class MALParserTest {

	private final MALParser malParser = new MALParser();

	private final RoutinesIO routinesIO = new RoutinesIO(new WrappedObjectMapper(new ObjectMapper()));

	@Test
	public void getNumWatchingTitles() {
		Integer numWatchingTitlesString = malParser.getNumWatchingTitles(routinesIO.readFromFile("classpath:__files/mal/testAccForDevProfile.txt"));
		assertNotNull(numWatchingTitlesString);
		assertEquals(TEST_ACC_WATCHING_TITLES, numWatchingTitlesString.intValue());
	}

	@Test
	public void getNumWatchingTitlesWatchingTitlesNotFound() {
		Integer numWatchingTitles = malParser.getNumWatchingTitles("");
		assertNull(numWatchingTitles);
	}
}