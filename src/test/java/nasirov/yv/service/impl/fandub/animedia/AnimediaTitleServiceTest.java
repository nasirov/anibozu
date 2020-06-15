package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnimediaTitles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.animedia.AnimediaTitle;
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
public class AnimediaTitleServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private AnimediaTitleService animediaTitleService;

	@Test
	public void shouldReturnDistinctNonNull() {
		//given
		mockGitHubResourceProps();
		List<AnimediaTitle> expected = getAnimediaTitles(false);
		mockGitHubResourcesService(expected);
		//when
		Map<Integer, List<AnimediaTitle>> result = animediaTitleService.getTitles();
		//then
		assertEquals(expected.size(), result.size());
		expected.forEach(x -> {
			List<AnimediaTitle> animediaTitlesByAnimediaTitleIdOnMal = result.get(x.getTitleIdOnMal());
			assertEquals(1, animediaTitlesByAnimediaTitleIdOnMal.size());
			assertEquals(x, animediaTitlesByAnimediaTitleIdOnMal.get(0));
		});
	}

	@Test
	public void shouldEmptyMap() {
		//given
		mockGitHubResourceProps();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<AnimediaTitle>> result = animediaTitleService.getTitles();
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourceProps() {
		doReturn("animediaTitles.json").when(gitHubResourceProps)
				.getAnimediaTitles();
	}

	private void mockGitHubResourcesService(List<AnimediaTitle> animediaTitles) {
		doReturn(animediaTitles).when(gitHubResourcesService)
				.getResource("animediaTitles.json", AnimediaTitle.class);
	}
}