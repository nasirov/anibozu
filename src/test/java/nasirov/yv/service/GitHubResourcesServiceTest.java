package nasirov.yv.service;

import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMalAnidubApiTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMalAnidubSiteTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubApiTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubSiteTitle;
import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
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
	public void shouldReturnAnidubApiTitles() {
		//given
		stubGitHub("anidubApiTitles.json");
		//when
		Set<AnidubApiTitle> result = githubResourcesService.getResource("anidubApiTitles.json", AnidubApiTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnidubApiTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnidubApiTitle()));
	}

	@Test
	public void shouldReturnAnidubSiteTitles() {
		//given
		stubGitHub("anidubSiteTitles.json");
		//when
		Set<AnidubSiteTitle> result = githubResourcesService.getResource("anidubSiteTitles.json", AnidubSiteTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnidubSiteTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnidubSiteTitle()));
	}

	private void stubGitHub(String resourceName) {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/" + resourceName, TEXT_PLAIN_CHARSET_UTF_8, "github/" + resourceName,
				gitHubAuthProps.getToken());
	}
}