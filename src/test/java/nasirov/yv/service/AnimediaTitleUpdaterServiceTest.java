package nasirov.yv.service;

import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getAnimediaTitles;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaTitle;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaTitleUpdaterServiceTest extends AbstractTest {

	@Test
	public void updateAnimediaTitles() {
		mockAnimediaService(buildRegularTitleResponse());
		List<AnimediaTitle> animediaTitles = getAnimediaTitles(false);
		animediaTitlesUpdateService.updateAnimediaTitles(animediaTitles);
		List<AnimediaTitle> expectedUpdatedAnimediaTitles = getAnimediaTitles(true);
		assertEquals(expectedUpdatedAnimediaTitles.size(), animediaTitles.size());
		animediaTitles.forEach(x -> assertTrue(expectedUpdatedAnimediaTitles.contains(x)));
	}

	@Test
	public void updateRegularAnimediaTitlesWithJoinedEpisodes() {
		mockAnimediaService(buildRegularTitleWithJoinedEpisodesResponse());
		List<AnimediaTitle> animediaTitles = getAnimediaTitles(false);
		animediaTitlesUpdateService.updateAnimediaTitles(animediaTitles);
		List<AnimediaTitle> expectedUpdatedAnimediaTitles = buildExpectedWithRegularWithEpisodesRange();
		assertEquals(expectedUpdatedAnimediaTitles.size(), animediaTitles.size());
		animediaTitles.forEach(x -> assertTrue(expectedUpdatedAnimediaTitles.contains(x)));
	}

	private List<AnimediaTitle> buildExpectedWithRegularWithEpisodesRange() {
		List<AnimediaTitle> expectedUpdatedAnimediaTitles = getAnimediaTitles(true);
		expectedUpdatedAnimediaTitles.stream()
				.filter(x -> x.getUrlOnAnimedia()
						.equals(REGULAR_TITLE_URL))
				.findFirst()
				.get()
				.setEpisodesRangeOnAnimedia(Lists.newArrayList("1", "2", "3", "4-5"));
		return expectedUpdatedAnimediaTitles;
	}

	private void mockAnimediaService(List<String> regularTitleResponse) {
		doReturn(regularTitleResponse).when(animediaService)
				.getEpisodes(REGULAR_TITLE_ID, "1");
		doReturn(buildConcretizedAndOngoingTitleResponse()).when(animediaService)
				.getEpisodes(CONCRETIZED_AND_ONGOING_TITLE_ID, "3");
		doReturn(Collections.emptyList()).when(animediaService)
				.getEpisodes(ANNOUNCEMENT_TITLE_ID, "1");
	}

	private List<String> buildRegularTitleResponse() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4", "Серия 5");
	}

	private List<String> buildRegularTitleWithJoinedEpisodesResponse() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4-5");
	}

	private List<String> buildConcretizedAndOngoingTitleResponse() {
		return Lists.newArrayList("Серия 1 (38)", "Серия 2 (39)", "Серия 3 (40)", "Серия 4 (41)", "Серия 5 (42)");
	}
}