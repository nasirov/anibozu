package nasirov.yv.service;

import static java.util.Collections.emptyMap;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getAnimediaSearchList;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getAnnouncement;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getNewTitle;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.getRegularTitle;
import static nasirov.yv.utils.AnimediaSearchListTitleBuilder.tempStub;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.ReferencesBuilder.notFoundOnAnimedia;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpStatus.OK;
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
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourcesCheckerServiceTest extends AbstractTest {

	private Set<AnimediaSearchListTitle> animediaSearchListFromAnimedia;

	private Set<AnimediaSearchListTitle> animediaSearchListFromGitHub;

	@Before
	@Override
	@SneakyThrows
	public void setUp() {
		super.setUp();
		animediaSearchListFromAnimedia = getAnimediaSearchList();
		animediaSearchListFromGitHub = getAnimediaSearchList();
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
	public void checkAnimediaSearchListFromGitHubEquals() {
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnimediaSearchListFromGitHub();
		checkTempFolder(false);
	}

	@Test
	public void checkAnimediaSearchListFromGitHubTitleRemovedFromAnimedia() {
		removeTitleFromAnimedia();
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnimediaSearchListFromGitHub();
		checkMarshalledTempFiles(resourcesNames.getTempRemovedTitlesFromAnimediaSearchList(), AnimediaSearchListTitle.class, getRegularTitle());
	}

	@Test
	public void checkAnimediaSearchListFromGitHubDuplicatedTitleOnGitHub() {
		AnimediaSearchListTitle duplicatedTitle = addDuplicatedTitleOnGitHub();
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnimediaSearchListFromGitHub();
		checkMarshalledTempFiles(resourcesNames.getTempDuplicatedUrlsInAnimediaSearchList(),
				AnimediaSearchListTitle.class,
				getRegularTitle(),
				duplicatedTitle);
	}

	@Test
	public void checkAnimediaSearchListFromGitHubNewTitleOnAnimedia() {
		addNewTitleToAnimedia();
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnimediaSearchListFromGitHub();
		checkMarshalledTempFiles(resourcesNames.getTempNewTitlesInAnimediaSearchList(), AnimediaSearchListTitle.class, getNewTitle());
	}

	@Test
	public void checkAnnouncementsOk() {
		stubAnimedia("");
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnnouncements();
		checkTempFolder(false);
	}

	@Test
	public void checkAnnouncementsBecomeOngoing() {
		String animeId = "1234";
		AnimediaSearchListTitle exAnnouncement = announcementBecomeOngoing(animeId);
		stubAnimedia("<ul role=\"tablist\" class=\"media__tabs__nav nav-tabs\" " + "data-entry_id=\"" + animeId + "\"");
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		resourcesCheckerService.checkAnnouncements();
		checkMarshalledTempFiles(resourcesNames.getTempExAnnouncements(), AnimediaSearchListTitle.class, exAnnouncement);
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
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, Sets.newHashSet(getRegularTitle(), getAnnouncement(), tempStub()));
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

	@Test
	public void checkNotFoundTitlesOnAnimediaRemoveTitleFromRepo() {
		saveUserWatchingTitleToNotFoundRepo();
		mockGetReferences(getAllReferences());
		resourcesCheckerService.checkNotFoundTitlesOnAnimedia();
		assertTrue(notFoundAnimeOnAnimediaRepository.findAll()
				.isEmpty());
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

	private void saveUserWatchingTitleToNotFoundRepo() {
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, REGULAR_TITLE_NAME, 0, "testPoster", "testUrl"));
	}

	private void addNewTitleToAnimedia() {
		animediaSearchListFromAnimedia.add(getNewTitle());
	}

	private AnimediaSearchListTitle addDuplicatedTitleOnGitHub() {
		AnimediaSearchListTitle multiSeasonsAnime = getRegularTitle();
		multiSeasonsAnime.setAnimeId("1234");
		animediaSearchListFromGitHub.add(multiSeasonsAnime);
		return multiSeasonsAnime;
	}

	private void removeTitleFromAnimedia() {
		animediaSearchListFromAnimedia.removeIf(x -> x.getUrl()
				.equals(REGULAR_TITLE_URL));
	}

	private void stubAnimedia(String content) {
		createStubWithContent("/" + ANNOUNCEMENT_TITLE_URL, TEXT_HTML_CHARSET_UTF_8, content, OK.value());
	}

	private void mockGetReferences(Set<TitleReference> allReferences) {
		doReturn(allReferences).when(referencesService)
				.getReferences();
	}

	private void mockIsTitleExist(boolean isTitleExist) {
		doReturn(isTitleExist).when(malService)
				.isTitleExist(eq(REGULAR_TITLE_NAME));
	}

	private void mockGetAnimediaSearchList(Set<AnimediaSearchListTitle> animediaSearchListFromAnimedia,
			Set<AnimediaSearchListTitle> animediaSearchListFromGitHub) {
		doReturn(animediaSearchListFromAnimedia).when(animediaService)
				.getAnimediaSearchListFromAnimedia();
		doReturn(animediaSearchListFromGitHub).when(animediaService)
				.getAnimediaSearchListFromGitHub();
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

	private AnimediaSearchListTitle announcementBecomeOngoing(String animeId) {
		AnimediaSearchListTitle announcementTitle = getAnnouncement();
		announcementTitle.setAnimeId(animeId);
		return announcementTitle;
	}
}