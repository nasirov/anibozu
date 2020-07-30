package nasirov.yv.parser;

import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static org.junit.Assert.assertEquals;

import nasirov.yv.parser.impl.MALParser;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class MALParserTest {

	private final MALParserI malParser = new MALParser();

	@Test
	public void shouldReturnNumWatchingTitles() {
		//given
		String userProfilePage = IOUtils.readFromFile("classpath:__files/mal/testAccForDevProfile.html");
		//when
		int result = malParser.getNumWatchingTitles(userProfilePage);
		//then
		assertEquals(TEST_ACC_WATCHING_TITLES, result);
	}

	@Test
	public void shouldReturnZeroNumWatchingTitles() {
		//given
		String userProfilePage = "";
		//when
		int result = malParser.getNumWatchingTitles(userProfilePage);
		//then
		assertEquals(0, result);
	}
}