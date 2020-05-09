package nasirov.yv.service;

import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;
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
		Set<TitleReference> expected = getReferences(LinkedHashSet.class, false);
		mockGitHubResourcesService(expected);
		//when
		Map<Integer, Set<TitleReference>> result = animediaGitHubResourcesService.getTitleReferences();
		//then
		assertEquals(expected.size(), result.size());
		expected.forEach(x -> {
			Set<TitleReference> titleReferencesByTitleIdOnMal = result.get(x.getTitleIdOnMAL());
			assertEquals(1, titleReferencesByTitleIdOnMal.size());
			assertTrue(titleReferencesByTitleIdOnMal.contains(x));
		});
	}

	@Test
	public void shouldEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptySet());
		//when
		Map<Integer, Set<TitleReference>> result = animediaGitHubResourcesService.getTitleReferences();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("animediaTitles.json").when(gitHubResourceProps)
				.getAnimediaTitles();
	}

	private void mockGitHubResourcesService(Set<TitleReference> animediaTitles) {
		doReturn(animediaTitles).when(gitHubResourcesService)
				.getResource("animediaTitles.json", TitleReference.class);
	}
}