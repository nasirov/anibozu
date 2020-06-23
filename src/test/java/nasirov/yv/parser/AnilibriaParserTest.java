package nasirov.yv.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import nasirov.yv.parser.impl.AnilibriaParser;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnilibriaParserTest {

	private final AnilibriaParserI anilibriaParser = new AnilibriaParser();

	@Test
	public void shouldExtractEpisodeNumbers() {
		assertEquals(Lists.newArrayList(1, 2, 3),
				anilibriaParser.extractEpisodes(IOUtils.readFromFile("classpath:__files/anilibria/regularTitlePage.html")));
	}

	@Test
	public void shouldExtractEmptyList() {
		assertTrue(anilibriaParser.extractEpisodes("")
				.isEmpty());
	}
}