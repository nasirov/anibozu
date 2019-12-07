package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getAnimediaSearchList;
import static nasirov.yv.utils.AnimediaSearchListBuilder.getSingleSeasonAnime;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.FAIRY_TAIL_ROOT_URL;
import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by nasirov.yv
 */

public class ResourcesCheckerServiceTest extends AbstractTest {

	private Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia;

	private Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub;

	@Before
	@Override
	@SneakyThrows
	public void setUp() {
		super.setUp();
		animediaSearchListFromAnimedia = getAnimediaSearchList();
		animediaSearchListFromGitHub = getAnimediaSearchList();
		mockGetAnimediaSearchList(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		mockIsAnimediaSearchListFromGitHubUpToDate(true, animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		mockGetAnimeSortedByTypeFromCache();
		saveNotFoundTitlesToRepo();
		mockGetAnimeSortedByType(animediaSearchListFromGitHub);
		mockCheckSortedAnime(new LinkedHashSet<>(), animediaSearchListFromAnimedia);
		mockGetMultiSeasonsReferences();
		mockIsReferencesAreFull(true);
		mockIsAllSingleSeasonAnimeHasConcretizedMALTitleName(true, animediaSearchListFromGitHub);
		mockIsTitleExist(true);
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
	public void okCase() {
		invokeScheduledMethod();
		checkTempFolder(false);
	}

	@Test
	public void caseWithMarshalling() {
		mockIsTitleExist(false);
		invokeScheduledMethod();
		checkMarshalledTempFiles(referenceForCheck());
		checkMarshalledTempFiles(getSingleSeasonAnime());
	}

	@Test
	public void animediaSearchListAndReferencesAreNotUpToDate() {
		mockIsAnimediaSearchListFromGitHubUpToDate(false, animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
		mockIsReferencesAreFull(false);
		mockCheckSortedAnime(Sets.newLinkedHashSet(getSingleSeasonAnime()), animediaSearchListFromAnimedia);
		mockIsTitleExist(false);
		mockIsAllSingleSeasonAnimeHasConcretizedMALTitleName(false, animediaSearchListFromGitHub);
		invokeScheduledMethod();
		checkTempFolder(false);
	}

	private void mockIsTitleExist(boolean isTitleExist) {
		doReturn(isTitleExist).when(malService)
				.isTitleExist(argThat(x -> x.equals("black clover") || x.equals("fairy tail")));
	}

	private void mockIsAllSingleSeasonAnimeHasConcretizedMALTitleName(boolean isAllSingleSeasonAnimeHasConcretizedMALTitleName,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		doReturn(isAllSingleSeasonAnimeHasConcretizedMALTitleName).when(resourcesService)
				.isAllSingleSeasonAnimeHasConcretizedMALTitleName(any(), eq(animediaSearchListFromGitHub));
	}

	private void mockGetAnimeSortedByType(Set<AnimediaTitleSearchInfo> animediaSearchList) {
		doReturn(getSortedAnime()).when(resourcesService)
				.getAnimeSortedByType(animediaSearchList);
	}
	private void mockIsReferencesAreFull(boolean isReferencesAreFull) {
		doReturn(isReferencesAreFull).when(resourcesService)
				.isReferencesAreFull(any(), eq(getAllReferences()));
	}
	private void mockGetMultiSeasonsReferences() {
		doReturn(getAllReferences()).when(referencesManager)
				.getMultiSeasonsReferences();
	}
	private void mockCheckSortedAnime(Set<AnimediaTitleSearchInfo> notFoundInSortedLists,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia) {
		doReturn(notFoundInSortedLists).when(resourcesService)
				.checkSortedAnime(any(), eq(animediaSearchListFromAnimedia));
	}

	private void saveNotFoundTitlesToRepo() {
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "black clover", 0, "testPoster", "testUrl"));
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail", 0, "testPoster", "testUrl"));
	}

	private void mockGetAnimeSortedByTypeFromCache() {
		doReturn(new HashMap<>()).when(resourcesService)
				.getAnimeSortedByTypeFromCache();
	}
	private void mockIsAnimediaSearchListFromGitHubUpToDate(boolean isAnimediaSearchListFromGitHubUpToDate,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia, Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		doReturn(isAnimediaSearchListFromGitHubUpToDate).when(resourcesService)
				.isAnimediaSearchListFromGitHubUpToDate(animediaSearchListFromAnimedia, animediaSearchListFromGitHub);
	}
	private void mockGetAnimediaSearchList(Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		doReturn(animediaSearchListFromAnimedia).when(animediaService)
				.getAnimediaSearchListFromAnimedia();
		doReturn(animediaSearchListFromGitHub).when(animediaService)
				.getAnimediaSearchListFromGitHub();
	}

	private AnimediaMALTitleReferences referenceForCheck() {
		return AnimediaMALTitleReferences.builder()
				.url(FAIRY_TAIL_ROOT_URL)
				.dataList("1")
				.firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("175")
				.currentMax("175")
				.titleOnMAL("fairy tail")
				.build();
	}

	private Set<AnimediaMALTitleReferences> getAllReferences() {
		Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
		AnimediaMALTitleReferences fairyTail1 = referenceForCheck();
		AnimediaMALTitleReferences titleWithNoneTitleName = AnimediaMALTitleReferences.builder()
				.url("anime/something")
				.dataList("1")
				.firstEpisode("1")
				.titleOnMAL(BaseConstants.NOT_FOUND_ON_MAL)
				.build();
		allReferences.add(fairyTail1);
		allReferences.add(titleWithNoneTitleName);
		return allReferences;
	}

	private Map<AnimeTypeOnAnimedia, Set<Anime>> getSortedAnime() {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = new EnumMap(AnimeTypeOnAnimedia.class);
		allTypes.put(SINGLESEASON, Sets.newLinkedHashSet(new Anime("1.1", ANIMEDIA_ONLINE_TV + "anime/chyornyj-klever/1/1", "anime/chyornyj-klever")));
		allTypes.put(MULTISEASONS, new LinkedHashSet<>());
		allTypes.put(ANNOUNCEMENT, new LinkedHashSet<>());
		return allTypes;
	}

	private void invokeScheduledMethod() {
		String scheduledMethod = Arrays.stream(resourcesCheckerService.getClass()
				.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Scheduled.class))
				.findFirst()
				.get()
				.getName();
		ReflectionTestUtils.invokeMethod(resourcesCheckerService, scheduledMethod);
	}

	private void checkMarshalledTempFiles(AnimediaMALTitleReferences animediaMALTitleReference) {
		checkTempFolder(true);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		Set<AnimediaMALTitleReferences> referencesWithInvalidMALTitleName = routinesIO.unmarshalFromFile(
				prefix + resourcesNames.getTempReferencesWithInvalidMALTitleName(), AnimediaMALTitleReferences.class, LinkedHashSet.class);
		assertEquals(1,
				referencesWithInvalidMALTitleName.stream()
						.filter(ref -> ref.equals(animediaMALTitleReference))
						.count());
	}

	private void checkMarshalledTempFiles(AnimediaTitleSearchInfo animediaTitleSearchInfo) {
		checkTempFolder(true);
		String prefix = TEMP_FOLDER_NAME + File.separator;
		Set<AnimediaTitleSearchInfo> searchTitlesWithInvalidMALTitleName = routinesIO.unmarshalFromFile(
				prefix + resourcesNames.getTempSearchTitlesWithInvalidMALTitleName(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		assertEquals(1,
				searchTitlesWithInvalidMALTitleName.stream()
						.filter(title -> title.equals(animediaTitleSearchInfo))
						.count());
	}

	@SneakyThrows
	private void checkTempFolder(boolean expected) {
		assertEquals(expected, routinesIO.isDirectoryExists(TEMP_FOLDER_NAME));
	}
}