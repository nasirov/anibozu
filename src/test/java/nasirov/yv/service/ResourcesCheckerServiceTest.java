package nasirov.yv.service;

import static java.util.Collections.emptyMap;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getAnnouncement;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getRegularTitle;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.tempStub;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.ReferencesBuilder.notFoundOnAnimedia;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.TitleReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourcesCheckerServiceTest extends AbstractTest {

	@Before
	@Override
	@SneakyThrows
	public void setUp() {
		super.setUp();
		deleteRecursively(new File(TEMP_FOLDER_NAME));
	}

	@After
	@Override
	@SneakyThrows
	public void tearDown() {
		super.tearDown();
		deleteRecursively(new File(TEMP_FOLDER_NAME));
	}

	@Test
	public void checkReferencesNamesOk() {
		mockGetReferences(getAllReferences());
		mockIsTitleExist(true);
		resourcesCheckerService.checkReferencesNames();
		checkTempFolder(false);
	}

	@Test
	public void checkReferencesNamesTitleIsNotExist() {
		mockGetReferences(getAllReferences());
		mockIsTitleExist(false);
		resourcesCheckerService.checkReferencesNames();
		checkMarshalledTempFiles(resourcesNames.getTempReferencesWithInvalidMALTitleName(), TitleReference.class, getRegularReferenceNotUpdated());
	}

	@Test
	public void checkReferencesMarshalMissingReferences() {
		mockGetAnimediaSearchList(Sets.newHashSet(getRegularTitle(), getAnnouncement(), tempStub()));
		mockGetReferences(getAllReferences());
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID, "animedia/regular/regularTitle.json", emptyMap());
		resourcesCheckerService.checkReferences();
		checkMarshalledTempFiles(resourcesNames.getTempRawReferences(),
				TitleReference.class,
				buildTempAnnouncementReference(getAnnouncement()),
				buildMissedReference("2"),
				buildMissedReference("3"),
				buildMissedReference("7"));
	}

	private <T> void checkMarshalledTempFiles(String fileName, Class<T> targetClass, T... title) {
		checkTempFolder(true);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		List<T> marshaledTitles = routinesIO.unmarshalFromFile(prefix + fileName, targetClass, ArrayList.class);
		assertEquals(title.length, marshaledTitles.size());
		Stream.of(title)
				.forEach(x -> assertEquals(1,
						marshaledTitles.stream()
								.filter(ref -> ref.equals(x))
								.count()));
	}

	@SneakyThrows
	private void checkTempFolder(boolean expected) {
		assertEquals(expected, routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
	}

	private void mockGetReferences(Set<TitleReference> allReferences) {
		doReturn(allReferences).when(referencesService)
				.getReferences();
	}

	private void mockIsTitleExist(boolean isTitleExist) {
		doReturn(isTitleExist).when(malService)
				.isTitleExist(eq(REGULAR_TITLE_NAME));
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