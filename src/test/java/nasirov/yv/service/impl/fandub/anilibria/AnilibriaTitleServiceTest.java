package nasirov.yv.service.impl.fandub.anilibria;

import static nasirov.yv.utils.AnilibriaTitleBuilder.buildAnilibriaTitles;
import static nasirov.yv.utils.AnilibriaTitleBuilder.buildRegularAnilibriaTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anilibria.AnilibriaTitle;
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
public class AnilibriaTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnilibriaTitleService anilibriaTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnilibriaTitles());
		AnilibriaTitle expected = buildRegularAnilibriaTitle();
		//when
		Map<Integer, List<AnilibriaTitle>> result = anilibriaTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<AnilibriaTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnilibriaTitle>> result = anilibriaTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("anilibriaSiteTitles.json").when(gitHubResourceProps)
				.getAnilibriaTitles();
	}

	private void mockGitHubResourcesService(List<AnilibriaTitle> anilibriaTitles) {
		doReturn(anilibriaTitles).when(gitHubResourcesService)
				.getResource("anilibriaSiteTitles.json", AnilibriaTitle.class);
	}
}