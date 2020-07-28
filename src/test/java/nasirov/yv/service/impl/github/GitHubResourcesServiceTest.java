package nasirov.yv.service.impl.github;

import static nasirov.yv.utils.AnidubTitleBuilder.buildNotFoundOnMalAnidubTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubTitle;
import static nasirov.yv.utils.AnilibriaTitleBuilder.buildNotFoundOnMalAnilibriaTitle;
import static nasirov.yv.utils.AnilibriaTitleBuilder.buildRegularAnilibriaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnimediaTitles;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildNotFoundOnMalAnimepikTitle;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildRegularAnimepikTitle;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildNotFoundOnMalJesidaiTitle;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildRegularJesidaiTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.anidub.AnidubTitle;
import nasirov.yv.data.fandub.anilibria.AnilibriaTitle;
import nasirov.yv.data.fandub.anime_pik.AnimepikTitle;
import nasirov.yv.data.fandub.animedia.AnimediaTitle;
import nasirov.yv.data.fandub.jisedai.JisedaiTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.github.GitHubFeignClient;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Created by nasirov.yv
 */
public class GitHubResourcesServiceTest extends AbstractTest {

	@MockBean
	private GitHubFeignClient gitHubFeignClient;

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
	public void shouldReturnAnidubTitles() {
		//given
		stubGitHub("anidubSiteTitles.json");
		//when
		List<AnidubTitle> result = githubResourcesService.getResource("anidubSiteTitles.json", AnidubTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnidubTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnidubTitle()));
	}

	@Test
	public void shouldReturnJisedaiTitles() {
		//given
		stubGitHub("jisedaiSiteTitles.json");
		//when
		List<JisedaiTitle> result = githubResourcesService.getResource("jisedaiSiteTitles.json", JisedaiTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularJesidaiTitle()));
		assertTrue(result.contains(buildNotFoundOnMalJesidaiTitle()));
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

	@Test
	public void shouldReturnAnilibriaTitles() {
		//given
		stubGitHub("anilibriaSiteTitles.json");
		//when
		List<AnilibriaTitle> result = githubResourcesService.getResource("anilibriaSiteTitles.json", AnilibriaTitle.class);
		//then
		assertEquals(2, result.size());
		assertTrue(result.contains(buildRegularAnilibriaTitle()));
		assertTrue(result.contains(buildNotFoundOnMalAnilibriaTitle()));
	}

	private void stubGitHub(String resourceName) {
		doReturn(IOUtils.readFromFile("classpath:__files/github/" + resourceName)).when(gitHubFeignClient)
				.getResource("token " + gitHubAuthProps.getToken(), resourceName);
	}
}