package nasirov.yv.service.impl.fandub.jisedai.site;

import static nasirov.yv.utils.JisedaiTitleBuilder.buildJesidaiSiteTitles;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildRegularJesidaiSiteTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
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
public class JisedaiSiteTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private JisedaiSiteTitleService anidubApiGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildJesidaiSiteTitles());
		JisedaiSiteTitle expected = buildRegularJesidaiSiteTitle();
		//when
		Map<Integer, List<JisedaiSiteTitle>> result = anidubApiGitHubResourcesService.getTitles();
		//then
		assertEquals(1, result.size());
		List<JisedaiSiteTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<JisedaiSiteTitle>> result = anidubApiGitHubResourcesService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("jisedaiSiteTitles.json").when(gitHubResourceProps)
				.getJisedaiSiteTitles();
	}

	private void mockGitHubResourcesService(List<JisedaiSiteTitle> jisedaiSiteTitles) {
		doReturn(jisedaiSiteTitles).when(gitHubResourcesService)
				.getResource("jisedaiSiteTitles.json", JisedaiSiteTitle.class);
	}
}