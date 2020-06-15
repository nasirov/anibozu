package nasirov.yv.service.impl.fandub.anidub.api;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubApiTitles;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubApiTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anidub.api.AnidubApiTitle;
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
public class AnidubApiTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubApiTitleService anidubApiTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubApiTitles());
		AnidubApiTitle expected = buildRegularAnidubApiTitle();
		//when
		Map<Integer, List<AnidubApiTitle>> result = anidubApiTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<AnidubApiTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnidubApiTitle>> result = anidubApiTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubApiTitles.json").when(gitHubResourceProps)
				.getAnidubApiTitles();
	}

	private void mockGitHubResourcesService(List<AnidubApiTitle> anidubApiTitles) {
		doReturn(anidubApiTitles).when(gitHubResourcesService)
				.getResource("anidubApiTitles.json", AnidubApiTitle.class);
	}
}