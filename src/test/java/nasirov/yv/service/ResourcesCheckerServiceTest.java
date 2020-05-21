package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getAnnouncement;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getRegularTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getNotFoundOnMalAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getRegularNotUpdatedAnimediaTitle;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.AnimediaTitle;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

/**
 * Created by nasirov.yv
 */
public class ResourcesCheckerServiceTest extends AbstractTest {

	@Override
	@Before
	public void setUp() {
		super.setUp();
		doNothing().when(wrappedObjectMapper)
				.marshal(any(File.class), any());
	}

	@Test
	public void checkAnimediaTitlesExistOnMalOk() {
		mockGetAnimediaTitles(getAllAnimediaTitles());
		mockIsTitleExist(true);
		resourcesCheckerService.checkAnimediaTitlesExistenceOnMal();
		verify(wrappedObjectMapper, never()).marshal(any(), any());
	}

	@Test
	public void checkAnimediaTitlesAreNotExistOnMal() {
		mockGetAnimediaTitles(getAllAnimediaTitles());
		mockIsTitleExist(false);
		resourcesCheckerService.checkAnimediaTitlesExistenceOnMal();
		checkMarshalledTempFiles(resourcesNames.getTempAnimediaTitlesNotFoundOnMal(), getRegularNotUpdatedAnimediaTitle());
	}

	@Test
	public void checkAnimediaTitlesAreNotExistOnAnimedia() {
		mockGetAnimediaSearchList(Sets.newHashSet(getRegularTitle(), getAnnouncement()));
		mockGetAnimediaTitles(getAllAnimediaTitles());
		resourcesCheckerService.checkAnimediaTitlesOnAnimedia();
		checkMarshalledTempFiles(resourcesNames.getTempMissedAnimediaTitles(),
				buildTempAnnouncementAnimediaTitle(getAnnouncement()),
				buildMissedAnimediaTitle("2"),
				buildMissedAnimediaTitle("3"),
				buildMissedAnimediaTitle("7"));
	}

	private <T> void checkMarshalledTempFiles(String fileName, T... title) {
		verify(wrappedObjectMapper, times(1)).marshal(argThat(x -> x.getName()
				.equals(fileName)), argThat((LinkedList x) -> x.size() == title.length && x.containsAll(CollectionUtils.arrayToList(title))));
	}

	private void mockGetAnimediaTitles(List<AnimediaTitle> allAnimediaTitles) {
		doReturn(allAnimediaTitles).when(githubResourcesService)
				.getResource("animediaTitles.json", AnimediaTitle.class);
	}

	private void mockIsTitleExist(boolean isTitleExist) {
		doReturn(isTitleExist).when(malService)
				.isTitleExist(REGULAR_TITLE_NAME, REGULAR_TITLE_MAL_ANIME_ID);
	}

	private void mockGetAnimediaSearchList(Set<AnimediaSearchListTitle> animediaSearchListFromAnimedia) {
		doReturn(animediaSearchListFromAnimedia).when(animediaService)
				.getAnimediaSearchList();
	}

	private List<AnimediaTitle> getAllAnimediaTitles() {
		return Lists.newArrayList(getRegularNotUpdatedAnimediaTitle(), getNotFoundOnMalAnimediaTitle());
	}

	private AnimediaTitle buildMissedAnimediaTitle(String dataList) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(REGULAR_TITLE_URL)
				.animeIdOnAnimedia(REGULAR_TITLE_ID)
				.dataListOnAnimedia(dataList)
				.build();
	}

	private AnimediaTitle buildTempAnnouncementAnimediaTitle(AnimediaSearchListTitle titleSearchInfo) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.build();
	}
}