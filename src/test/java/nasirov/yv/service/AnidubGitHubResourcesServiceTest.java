package nasirov.yv.service;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubTitle;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.anidub.AnidubTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.AnidubGitHubResourcesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnidubGitHubResourcesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubGitHubResourcesService anidubGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubTitle());
		AnidubTitle expected = buildRegularAnidubTitle();
		//when
		Map<Integer, AnidubTitle> result = anidubGitHubResourcesService.getAnidubTitles();
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
		Map<Integer, AnidubTitle> result = anidubGitHubResourcesService.getAnidubTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubTitles.json").when(gitHubResourceProps)
				.getAnidubTitles();
	}

	private void mockGitHubResourcesService(Set<AnidubTitle> anidubTitles) {
		doReturn(anidubTitles).when(gitHubResourcesService)
				.getResource("anidubTitles.json", AnidubTitle.class);
	}
}