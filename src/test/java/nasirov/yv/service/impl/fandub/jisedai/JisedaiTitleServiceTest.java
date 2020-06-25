package nasirov.yv.service.impl.fandub.jisedai;

import static nasirov.yv.utils.JisedaiTitleBuilder.buildJesidaiTitles;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildRegularJesidaiTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.jisedai.JisedaiTitle;
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
public class JisedaiTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private JisedaiTitleService jisedaiTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildJesidaiTitles());
		JisedaiTitle expected = buildRegularJesidaiTitle();
		//when
		Map<Integer, List<JisedaiTitle>> result = jisedaiTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<JisedaiTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<JisedaiTitle>> result = jisedaiTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("jisedaiSiteTitles.json").when(gitHubResourceProps)
				.getJisedaiTitles();
	}

	private void mockGitHubResourcesService(List<JisedaiTitle> jisedaiTitles) {
		doReturn(jisedaiTitles).when(gitHubResourcesService)
				.getResource("jisedaiSiteTitles.json", JisedaiTitle.class);
	}
}