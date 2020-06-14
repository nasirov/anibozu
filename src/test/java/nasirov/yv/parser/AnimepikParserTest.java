package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;

import nasirov.yv.parser.impl.AnimepikParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimepikParserTest {

	private final AnimepikParserI animepikParser = new AnimepikParser();

	@Test
	public void shouldExtractEpisodeNumber() {
		assertEquals(2, animepikParser.extractEpisodeNumber("2 серия ").intValue());
		assertEquals(1, animepikParser.extractEpisodeNumber("серия").intValue());
		assertEquals(1, animepikParser.extractEpisodeNumber("серия ").intValue());
		assertEquals(2, animepikParser.extractEpisodeNumber("2 серия | OVA").intValue());
		assertEquals(123, animepikParser.extractEpisodeNumber("123").intValue());
		assertEquals(123, animepikParser.extractEpisodeNumber("123 OVA").intValue());
		assertEquals(1, animepikParser.extractEpisodeNumber("1").intValue());
		assertEquals(1, animepikParser.extractEpisodeNumber(" ").intValue());
		assertEquals(1, animepikParser.extractEpisodeNumber(null).intValue());
	}
}