package nasirov.yv.service;

import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class GithubResourcesServiceTest extends AbstractTest {

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		stubGitHub();
		Set<TitleReference> multiSeasonsReferences = githubResourcesService.getResource("animediaTitles.json", TitleReference.class);
		List<TitleReference> expected = getReferences(ArrayList.class, false);
		assertEquals(expected.size(), multiSeasonsReferences.size());
		multiSeasonsReferences.forEach(x -> assertTrue(expected.contains(x)));
	}

	private void stubGitHub() {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/animediaTitles.json", TEXT_PLAIN_CHARSET_UTF_8, "github/animediaTitles.json",
				gitHubAuthProps.getToken());
	}
}