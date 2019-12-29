package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;

import nasirov.yv.exception.animedia.EpisodeNumberNotFoundException;
import nasirov.yv.parser.impl.AnimediaHTMLParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaHTMLParserTest {

	private final AnimediaHTMLParserI animediaHTMLParser = new AnimediaHTMLParser();

	@Test
	public void extractEpisodeNumberOk() {
		assertEquals("1", animediaHTMLParser.extractEpisodeNumber("Серия 1 (64)"));
		assertEquals("1-2", animediaHTMLParser.extractEpisodeNumber("Серия 1-2"));
		assertEquals("1", animediaHTMLParser.extractEpisodeNumber("Серия 1"));
		assertEquals("1", animediaHTMLParser.extractEpisodeNumber("Серия"));
	}

	@Test(expected = EpisodeNumberNotFoundException.class)
	public void extractEpisodeNumberException() {
		animediaHTMLParser.extractEpisodeNumber("");
	}
}