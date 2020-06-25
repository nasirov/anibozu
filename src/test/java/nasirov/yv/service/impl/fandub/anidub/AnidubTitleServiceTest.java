package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.utils.AnidubTitleBuilder.buildAnidubTitles;
import static nasirov.yv.utils.AnidubTitleBuilder.buildRegularAnidubTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anidub.AnidubTitle;
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
public class AnidubTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnidubTitleService anidubTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnidubTitles());
		AnidubTitle expected = buildRegularAnidubTitle();
		//when
		Map<Integer, List<AnidubTitle>> result = anidubTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<AnidubTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnidubTitle>> result = anidubTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anidubSiteTitles.json").when(gitHubResourceProps)
				.getAnidubTitles();
	}

	private void mockGitHubResourcesService(List<AnidubTitle> anidubTitles) {
		doReturn(anidubTitles).when(gitHubResourcesService)
				.getResource("anidubSiteTitles.json", AnidubTitle.class);
	}
}