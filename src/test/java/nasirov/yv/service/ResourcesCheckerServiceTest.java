package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getAnnouncement;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getRegularTitle;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.ReferencesBuilder.notFoundOnAnimedia;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.LinkedList;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.TitleReference;
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
	public void checkReferencesNamesOk() {
		mockGetReferences(getAllReferences());
		mockIsTitleExist(true);
		resourcesCheckerService.checkReferencesNames();
		verify(wrappedObjectMapper, never()).marshal(any(), any());
	}

	@Test
	public void checkReferencesNamesTitleIsNotExist() {
		mockGetReferences(getAllReferences());
		mockIsTitleExist(false);
		resourcesCheckerService.checkReferencesNames();
		checkMarshalledTempFiles(resourcesNames.getTempReferencesWithInvalidMALTitleName(), getRegularReferenceNotUpdated());
	}

	@Test
	public void checkReferencesMarshalMissingReferences() {
		mockGetAnimediaSearchList(Sets.newHashSet(getRegularTitle(), getAnnouncement()));
		mockGetReferences(getAllReferences());
		resourcesCheckerService.checkReferences();
		checkMarshalledTempFiles(resourcesNames.getTempRawReferences(),
				buildTempAnnouncementReference(getAnnouncement()),
				buildMissedReference("2"),
				buildMissedReference("3"),
				buildMissedReference("7"));
	}

	private <T> void checkMarshalledTempFiles(String fileName, T... title) {
		verify(wrappedObjectMapper, times(1)).marshal(argThat(x -> x.getName()
				.equals(fileName)), argThat((LinkedList x) -> x.size() == title.length && x.containsAll(CollectionUtils.arrayToList(title))));
	}

	private void mockGetReferences(Set<TitleReference> allReferences) {
		doReturn(allReferences).when(githubResourcesService)
				.getResource("animediaTitles.json", TitleReference.class);
	}

	private void mockIsTitleExist(boolean isTitleExist) {
		doReturn(isTitleExist).when(malService)
				.isTitleExist(REGULAR_TITLE_NAME, REGULAR_TITLE_MAL_ID);
	}

	private void mockGetAnimediaSearchList(Set<AnimediaSearchListTitle> animediaSearchListFromAnimedia) {
		doReturn(animediaSearchListFromAnimedia).when(animediaService)
				.getAnimediaSearchList();
	}

	private Set<TitleReference> getAllReferences() {
		return Sets.newHashSet(getRegularReferenceNotUpdated(), notFoundOnAnimedia());
	}

	private TitleReference buildMissedReference(String dataList) {
		return TitleReference.builder()
				.urlOnAnimedia(REGULAR_TITLE_URL)
				.animeIdOnAnimedia(REGULAR_TITLE_ID)
				.dataListOnAnimedia(dataList)
				.build();
	}

	private TitleReference buildTempAnnouncementReference(AnimediaSearchListTitle titleSearchInfo) {
		return TitleReference.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.build();
	}
}