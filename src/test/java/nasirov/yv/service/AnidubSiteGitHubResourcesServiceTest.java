package nasirov.yv.service;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubSiteTitles;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubSiteTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.fandub.anidub.AnidubSiteGitHubResourcesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnidubSiteGitHubResourcesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubSiteGitHubResourcesService anidubSiteGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubSiteTitles());
		AnidubSiteTitle expected = buildRegularAnidubSiteTitle();
		//when
		Map<Integer, AnidubSiteTitle> result = anidubSiteGitHubResourcesService.getAnidubTitles();
		//then
		assertEquals(1, result.size());
		assertEquals(expected, result.get(expected.getTitleIdOnMal()));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptySet());
		//when
		Map<Integer, AnidubSiteTitle> result = anidubSiteGitHubResourcesService.getAnidubTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubSiteTitles.json").when(gitHubResourceProps)
				.getAnidubSiteTitles();
	}

	private void mockGitHubResourcesService(Set<AnidubSiteTitle> anidubApiTitles) {
		doReturn(anidubApiTitles).when(gitHubResourcesService)
				.getResource("anidubSiteTitles.json", AnidubSiteTitle.class);
	}
}