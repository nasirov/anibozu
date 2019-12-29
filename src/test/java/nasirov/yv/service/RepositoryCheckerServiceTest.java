package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.ReferencesBuilder.notFoundOnAnimedia;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.Test;

public class RepositoryCheckerServiceTest extends AbstractTest {


	@Test
	public void checkNotFoundTitlesOnAnimediaRemoveTitleFromRepo() {
		saveUserWatchingTitleToNotFoundRepo();
		mockGetReferences(getAllReferences());
		repositoryCheckerService.checkNotFoundTitlesOnAnimedia();
		assertTrue(notFoundAnimeOnAnimediaRepository.findAll()
				.isEmpty());
	}

	private void saveUserWatchingTitleToNotFoundRepo() {
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, REGULAR_TITLE_NAME, 0, "testPoster", "testUrl"));
	}

	private void mockGetReferences(Set<TitleReference> allReferences) {
		doReturn(allReferences).when(referencesService)
				.getReferences();
	}

	private Set<TitleReference> getAllReferences() {
		return Sets.newHashSet(getRegularReferenceNotUpdated(), notFoundOnAnimedia());
	}
}