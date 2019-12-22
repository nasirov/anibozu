package nasirov.yv.service;

import static nasirov.yv.utils.TestConstants.TEXT_JAVASCRIPT_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaServiceTest extends AbstractTest {

	@Test
	public void testGetAnimediaSearchListFromAnimedia() {
		createStubWithBodyFile("/ajax/anime_list", TEXT_JAVASCRIPT_CHARSET_UTF_8, "animedia/search/animediaSearchListFromAnimedia.json");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaService.getAnimediaSearchListFromAnimedia();
		assertEquals(3, animediaSearchList.size());
		assertTrue(animediaSearchList.stream()
				.allMatch(set -> set.getUrl()
						.matches("^anime/.+")));
	}

	@Test
	public void testGetAnimediaSearchListFromGitHub() {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/animediaSearchList.json",
				TEXT_PLAIN_CHARSET_UTF_8,
				"github/animediaSearchListFromGitHub.json");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		assertEquals(2, animediaSearchList.size());
	}
}