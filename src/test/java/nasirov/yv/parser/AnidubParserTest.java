package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;

import nasirov.yv.parser.impl.AnidubParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnidubParserTest {

	private final AnidubParserI anidubParser = new AnidubParser();

	@Test
	public void shouldExtractEpisodeNumber() {
		assertEquals(2, anidubParser.extractEpisodeNumber("2 Серия ").intValue());
		assertEquals(1, anidubParser.extractEpisodeNumber("Серия").intValue());
		assertEquals(1, anidubParser.extractEpisodeNumber("Серия ").intValue());
		assertEquals(2, anidubParser.extractEpisodeNumber("2 Серия | OVA").intValue());
		assertEquals(123, anidubParser.extractEpisodeNumber("123").intValue());
		assertEquals(123, anidubParser.extractEpisodeNumber("123 OVA").intValue());
		assertEquals(1, anidubParser.extractEpisodeNumber("1").intValue());
		assertEquals(1, anidubParser.extractEpisodeNumber(" ").intValue());
		assertEquals(1, anidubParser.extractEpisodeNumber(null).intValue());
	}

	@Test
	public void shouldFixBrokenUrl() {
		assertEquals("https://video.sibnet.ru/shell.php?videoid=4325321",
				anidubParser.fixBrokenUrl("https://video.sibnet.ru/shell.php?videoid=4325321\" frameborder=\"0\" scrolling=\"no"));
		assertEquals("https://video.sibnet.ru/shell.php?videoid=4325321",
				anidubParser.fixBrokenUrl("https://video.sibnet.ru/shell" + ".php?videoid=4325321"));
		assertEquals("https://videofile.online/anime/9573/1.mp4", anidubParser.fixBrokenUrl("https://videofile.online/anime/9573/1.mp4"));
	}
}