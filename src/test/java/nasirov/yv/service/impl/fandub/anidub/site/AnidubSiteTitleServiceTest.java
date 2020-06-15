package nasirov.yv.service.impl.fandub.anidub.site;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubSiteTitles;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubSiteTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnidubSiteTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubSiteTitleService anidubSiteTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubSiteTitles());
		AnidubSiteTitle expected = buildRegularAnidubSiteTitle();
		//when
		Map<Integer, List<AnidubSiteTitle>> result = anidubSiteTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<AnidubSiteTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnidubSiteTitle>> result = anidubSiteTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubSiteTitles.json").when(gitHubResourceProps)
				.getAnidubSiteTitles();
	}

	private void mockGitHubResourcesService(List<AnidubSiteTitle> anidubApiTitles) {
		doReturn(anidubApiTitles).when(gitHubResourcesService)
				.getResource("anidubSiteTitles.json", AnidubSiteTitle.class);
	}
}