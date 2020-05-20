package nasirov.yv.service;

import static nasirov.yv.utils.JisedaiTitleBuilder.buildJesidaiSiteTitles;
import static nasirov.yv.utils.JisedaiTitleBuilder.buildRegularJesidaiSiteTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiGitHubResourcesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class JisedaiSiteGitHubResourcesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private JisedaiGitHubResourcesService anidubApiGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildJesidaiSiteTitles());
		JisedaiSiteTitle expected = buildRegularJesidaiSiteTitle();
		//when
		Map<Integer, JisedaiSiteTitle> result = anidubApiGitHubResourcesService.getJisedaiTitles();
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
		Map<Integer, JisedaiSiteTitle> result = anidubApiGitHubResourcesService.getJisedaiTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("jisedaiSiteTitles.json").when(gitHubResourceProps)
				.getJisedaiSiteTitles();
	}

	private void mockGitHubResourcesService(Set<JisedaiSiteTitle> jisedaiSiteTitles) {
		doReturn(jisedaiSiteTitles).when(gitHubResourcesService)
				.getResource("jisedaiSiteTitles.json", JisedaiSiteTitle.class);
	}
}