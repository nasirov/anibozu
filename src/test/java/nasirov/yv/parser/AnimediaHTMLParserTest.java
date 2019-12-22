package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

	@Test
	public void getAnimedIdFound() {
		assertEquals("12345", animediaHTMLParser.getAnimeId("<ul role=\"tablist\" class=\"media__tabs__nav nav-tabs\" data-entry_id=\"12345\""));
	}

	@Test
	public void getAnimedIdNotFound() {
		assertNull(animediaHTMLParser.getAnimeId(""));
	}
}