package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;

import nasirov.yv.parser.impl.AnimediaHTMLParser;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaHTMLParserTest {

	private final AnimediaHTMLParserI animediaHTMLParser = new AnimediaHTMLParser();

	@Test
	public void extractEpisodeNumberOk() {
		assertEquals("2", animediaHTMLParser.extractEpisodeNumber("Серия 2 (64)"));
		assertEquals("1-2", animediaHTMLParser.extractEpisodeNumber("Серия 1-2"));
		assertEquals("2", animediaHTMLParser.extractEpisodeNumber("Серия 2"));
		assertEquals("1", animediaHTMLParser.extractEpisodeNumber("Серия"));
		assertEquals("2", animediaHTMLParser.extractEpisodeNumber("Серия 2 | OVA"));
		assertEquals("12.5", animediaHTMLParser.extractEpisodeNumber("Серия 12.5"));
		assertEquals("12.5", animediaHTMLParser.extractEpisodeNumber("Серия 12.5 RECAP"));
		assertEquals("123", animediaHTMLParser.extractEpisodeNumber("123"));
		assertEquals("123", animediaHTMLParser.extractEpisodeNumber("123 OVA"));
		assertEquals("123", animediaHTMLParser.extractEpisodeNumber("OVA 123 (456)"));
	}
}