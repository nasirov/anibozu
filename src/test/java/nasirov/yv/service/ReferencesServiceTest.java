package nasirov.yv.service;

import static nasirov.yv.utils.ReferencesBuilder.getReferences;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_AND_ONGOING_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class ReferencesServiceTest extends AbstractTest {

	@Test
	public void getMultiSeasonsReferences() throws Exception {
		stubGitHub();
		Set<TitleReference> multiSeasonsReferences = referencesService.getReferences();
		List<TitleReference> expected = getReferences(ArrayList.class, false);
		assertEquals(expected.size(), multiSeasonsReferences.size());
		multiSeasonsReferences.forEach(x -> assertTrue(expected.contains(x)));
	}

	@Test
	public void updateReferences() throws Exception {
		mockAnimediaService(buildRegularTitleResponse());
		Set<TitleReference> referencesForUpdate = getReferences(LinkedHashSet.class, false);
		referencesService.updateReferences(referencesForUpdate);
		List<TitleReference> expectedUpdatedReferences = getReferences(ArrayList.class, true);
		assertEquals(expectedUpdatedReferences.size(), referencesForUpdate.size());
		referencesForUpdate.forEach(x -> assertTrue(expectedUpdatedReferences.contains(x)));
	}

	@Test
	public void updateReferencesRegularWithJoinedEpisodes() throws Exception {
		mockAnimediaService(buildRegularTitleWithJoinedEpisodesResponse());
		Set<TitleReference> referencesForUpdate = getReferences(LinkedHashSet.class, false);
		referencesService.updateReferences(referencesForUpdate);
		List<TitleReference> expectedUpdatedReferences = buildExpectedWithRegularWithEpisodesRange();
		assertEquals(expectedUpdatedReferences.size(), referencesForUpdate.size());
		referencesForUpdate.forEach(x -> assertTrue(expectedUpdatedReferences.contains(x)));
	}

	private List<TitleReference> buildExpectedWithRegularWithEpisodesRange() throws IllegalAccessException, InstantiationException {
		List<TitleReference> expectedUpdatedReferences = getReferences(ArrayList.class, true);
		expectedUpdatedReferences.stream()
				.filter(x -> x.getUrlOnAnimedia()
						.equals(REGULAR_TITLE_URL))
				.findFirst()
				.get()
				.setEpisodesRangeOnAnimedia(Lists.newArrayList("1", "2", "3", "4-5"));
		return expectedUpdatedReferences;
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

	private void stubGitHub() {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/references.json",
				TEXT_PLAIN_CHARSET_UTF_8,
				"github/references.json",
				gitHubAuthProps.getToken());
	}
}