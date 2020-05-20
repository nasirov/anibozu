package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;

import nasirov.yv.parser.impl.JisedaiParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class JisedaiParserTest {

	private final JisedaiParserI jisedaiParser = new JisedaiParser();

	@Test
	public void shouldExtractEpisodeNumber() {
		assertEquals(2, jisedaiParser.extractEpisodeNumber("2 эпизод ").intValue());
		assertEquals(1, jisedaiParser.extractEpisodeNumber("эпизод").intValue());
		assertEquals(1, jisedaiParser.extractEpisodeNumber("эпизод ").intValue());
		assertEquals(2, jisedaiParser.extractEpisodeNumber("2 эпизод | OVA").intValue());
		assertEquals(123, jisedaiParser.extractEpisodeNumber("123").intValue());
		assertEquals(123, jisedaiParser.extractEpisodeNumber("123 OVA").intValue());
		assertEquals(1, jisedaiParser.extractEpisodeNumber("1").intValue());
		assertEquals(1, jisedaiParser.extractEpisodeNumber(" ").intValue());
		assertEquals(1, jisedaiParser.extractEpisodeNumber(null).intValue());
	}
}