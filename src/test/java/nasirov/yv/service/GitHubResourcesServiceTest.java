package nasirov.yv.service;

import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMal;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubTitle;
import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.anidub.AnidubTitle;
import nasirov.yv.data.animedia.TitleReference;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class GitHubResourcesServiceTest extends AbstractTest {

	@Test
	public void shouldReturnAnimediaTitles() {
		//given
		stubGitHub("animediaTitles.json");
		List<TitleReference> expected = getReferences(ArrayList.class, false);
		//when
		Set<TitleReference> result = githubResourcesService.getResource("animediaTitles.json", TitleReference.class);
		//then
		assertEquals(expected.size(), result.size());
		result.forEach(x -> assertTrue(expected.contains(x)));
	}

	@Test
	public void shouldReturnAnidubTitles() {
		//given
		stubGitHub("anidubTitles.json");
		//when
		Set<AnidubTitle> result = githubResourcesService.getResource("anidubTitles.json", AnidubTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnidubTitle()));
		assertTrue(result.contains(buildNotFoundOnMal()));
	}

	private void stubGitHub(String resourceName) {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/" + resourceName, TEXT_PLAIN_CHARSET_UTF_8, "github/" + resourceName,
				gitHubAuthProps.getToken());
	}
}