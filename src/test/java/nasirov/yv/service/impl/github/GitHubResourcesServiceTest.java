package nasirov.yv.service.impl.github;

import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMalAnidubApiTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMalAnidubSiteTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubApiTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubSiteTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnimediaTitles;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildNotFoundOnMalAnimepikTitle;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildRegularAnimepikTitle;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildNotFoundOnMalJesidaiSiteTitle;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildRegularJesidaiSiteTitle;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.anidub.api.AnidubApiTitle;
import nasirov.yv.data.fandub.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.fandub.anime_pik.api.AnimepikTitle;
import nasirov.yv.data.fandub.animedia.AnimediaTitle;
import nasirov.yv.data.fandub.jisedai.site.JisedaiSiteTitle;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class GitHubResourcesServiceTest extends AbstractTest {

	@Test
	public void shouldReturnAnimediaTitles() {
		//given
		stubGitHub("animediaTitles.json");
		List<AnimediaTitle> expected = getAnimediaTitles(false);
		//when
		List<AnimediaTitle> result = githubResourcesService.getResource("animediaTitles.json", AnimediaTitle.class);
		//then
		assertEquals(expected.size(), result.size());
		result.forEach(x -> assertTrue(expected.contains(x)));
	}

	@Test
	public void shouldReturnAnidubApiTitles() {
		//given
		stubGitHub("anidubApiTitles.json");
		//when
		List<AnidubApiTitle> result = githubResourcesService.getResource("anidubApiTitles.json", AnidubApiTitle.class);
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
		List<AnidubSiteTitle> result = githubResourcesService.getResource("anidubSiteTitles.json", AnidubSiteTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnidubSiteTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnidubSiteTitle()));
	}

	@Test
	public void shouldReturnJisedaiSiteTitles() {
		//given
		stubGitHub("jisedaiSiteTitles.json");
		//when
		List<JisedaiSiteTitle> result = githubResourcesService.getResource("jisedaiSiteTitles.json", JisedaiSiteTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularJesidaiSiteTitle()));
		assertTrue(result.contains(buildNotFoundOnMalJesidaiSiteTitle()));
	}

	@Test
	public void shouldReturnAnimepikTitles() {
		//given
		stubGitHub("animepikTitles.json");
		//when
		List<AnimepikTitle> result = githubResourcesService.getResource("animepikTitles.json", AnimepikTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnimepikTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnimepikTitle()));
	}

	private void stubGitHub(String resourceName) {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/" + resourceName,
				TEXT_PLAIN_CHARSET_UTF_8,
				"github/" + resourceName,
				gitHubAuthProps.getToken());
	}
}