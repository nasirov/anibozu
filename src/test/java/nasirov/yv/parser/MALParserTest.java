package nasirov.yv.parser;

import static nasirov.yv.utils.IOUtils.readFromFile;
import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nasirov.yv.parser.impl.MALParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class MALParserTest {

	private final MALParserI malParser = new MALParser();

	@Test
	public void getNumWatchingTitlesOk() {
		int numWatchingTitlesString = malParser.getNumWatchingTitles(readFromFile("classpath:__files/mal/testAccForDevProfile.html"));
		assertEquals(TEST_ACC_WATCHING_TITLES, numWatchingTitlesString);
	}

	@Test
	public void getNumWatchingTitlesWatchingTitlesNotFound() {
		int numWatchingTitles = malParser.getNumWatchingTitles("");
		assertEquals(0, numWatchingTitles);
	}
}