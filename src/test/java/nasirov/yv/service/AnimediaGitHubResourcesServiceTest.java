package nasirov.yv.service;

import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnimediaTitles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.impl.fandub.animedia.AnimediaGitHubResourcesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimediaGitHubResourcesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnimediaGitHubResourcesService animediaGitHubResourcesService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		Set<AnimediaTitle> expected = getAnimediaTitles(LinkedHashSet.class, false);
		mockGitHubResourcesService(expected);
		//when
		Map<Integer, Set<AnimediaTitle>> result = animediaGitHubResourcesService.getAnimediaTitles();
		//then
		assertEquals(expected.size(), result.size());
		expected.forEach(x -> {
			Set<AnimediaTitle> animediaTitlesByAnimediaTitleIdOnMal = result.get(x.getTitleIdOnMal());
			assertEquals(1, animediaTitlesByAnimediaTitleIdOnMal.size());
			assertTrue(animediaTitlesByAnimediaTitleIdOnMal.contains(x));
		});
	}

	@Test
	public void shouldEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptySet());
		//when
		Map<Integer, Set<AnimediaTitle>> result = animediaGitHubResourcesService.getAnimediaTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("animediaTitles.json").when(gitHubResourceProps)
				.getAnimediaTitles();
	}

	private void mockGitHubResourcesService(Set<AnimediaTitle> animediaTitles) {
		doReturn(animediaTitles).when(gitHubResourcesService)
				.getResource("animediaTitles.json", AnimediaTitle.class);
	}
}