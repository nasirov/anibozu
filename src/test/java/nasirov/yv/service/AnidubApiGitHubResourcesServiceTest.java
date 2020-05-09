package nasirov.yv.service;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubApiTitles;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubApiTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.fandub.anidub.AnidubApiGitHubResourcesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnidubApiGitHubResourcesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubApiGitHubResourcesService anidubApiGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubApiTitles());
		AnidubApiTitle expected = buildRegularAnidubApiTitle();
		//when
		Map<Integer, AnidubApiTitle> result = anidubApiGitHubResourcesService.getAnidubTitles();
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
		Map<Integer, AnidubApiTitle> result = anidubApiGitHubResourcesService.getAnidubTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubApiTitles.json").when(gitHubResourceProps)
				.getAnidubApiTitles();
	}

	private void mockGitHubResourcesService(Set<AnidubApiTitle> anidubApiTitles) {
		doReturn(anidubApiTitles).when(gitHubResourcesService)
				.getResource("anidubApiTitles.json", AnidubApiTitle.class);
	}
}