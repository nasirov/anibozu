package nasirov.yv.service;

import static nasirov.yv.utils.AnimepikTitleBuilder.buildAnimepikTitles;
import static nasirov.yv.utils.AnimepikTitleBuilder.buildRegularAnimepikTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.anime_pik.api.AnimepikTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.fandub.animepik.AnimepikTitleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimepikTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnimepikTitleService animePikTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(buildAnimepikTitles());
		AnimepikTitle expected = buildRegularAnimepikTitle();
		//when
		Map<Integer, List<AnimepikTitle>> result = animePikTitleService.getTitles();
		//then
		assertEquals(1, result.size());
		List<AnimepikTitle> titles = result.get(expected.getTitleIdOnMal());
		assertEquals(1, titles.size());
		assertEquals(expected, titles.get(0));
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnimepikTitle>> result = animePikTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("animepikTitles.json").when(gitHubResourceProps)
				.getAnimepikTitles();
	}

	private void mockGitHubResourcesService(List<AnimepikTitle> animepikTitles) {
		doReturn(animepikTitles).when(gitHubResourcesService)
				.getResource("animepikTitles.json", AnimepikTitle.class);
	}
}