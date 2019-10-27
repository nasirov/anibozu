package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.scheduler.ResourcesCheckerService;
import nasirov.yv.util.RoutinesIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by nasirov.yv
 */

public class ResourcesCheckerServiceTest extends AbstractTest {

	@MockBean
	private AnimediaServiceI animediaService;

	@MockBean
	private ResourcesServiceI resourcesService;

	@MockBean
	private MALServiceI malService;

	@MockBean
	private ReferencesServiceI referencesManager;

	@Autowired
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@Autowired
	private ResourcesCheckerService resourcesCheckerService;

	private Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia;

	private Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub;

	private Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes;

	private Set<Anime> single;

	private Set<Anime> multi;


	@Before
	public void setUp() {
		super.setUp();
		animediaSearchListFromAnimedia = getAnimediaSearchListFromResources();
		animediaSearchListFromGitHub = getAnimediaSearchListFromResources();
		doReturn(animediaSearchListFromAnimedia).when(animediaService).getAnimediaSearchListFromAnimedia();
		doReturn(animediaSearchListFromGitHub).when(animediaService).getAnimediaSearchListFromGitHub();
		allTypes = getSortedAnime();
		single = allTypes.get(SINGLESEASON);
		multi = allTypes.get(MULTISEASONS);
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "black clover", 0, "testPoster", "testUrl"));
		notFoundAnimeOnAnimediaRepository.saveAndFlush(new UserMALTitleInfo(0, WATCHING.getCode(), 0, "fairy tail", 0, "testPoster", "testUrl"));
		RoutinesIO.removeDir(tempFolderName);
	}

	@After
	public void tearDown() {
		RoutinesIO.removeDir(tempFolderName);
	}

	@Test
	public void testCheckApplicationResourcesAllUpToDateButTitlesNotFoundOnMAL() throws NotDirectoryException {
		doReturn(true).when(resourcesService)
				.isAnimediaSearchListFromGitHubUpToDate(eq(animediaSearchListFromGitHub), eq(animediaSearchListFromAnimedia));
		doReturn(new HashMap<>()).when(resourcesService).getAnimeSortedByTypeFromCache();
		doReturn(allTypes).when(resourcesService).getAnimeSortedByType(eq(animediaSearchListFromGitHub));
		Set<AnimediaTitleSearchInfo> notFoundInResources = new LinkedHashSet<>();
		notFoundInResources.add(new AnimediaTitleSearchInfo());
		doReturn(notFoundInResources).when(resourcesService).checkSortedAnime(eq(allTypes), eq(animediaSearchListFromAnimedia));
		Set<AnimediaMALTitleReferences> allReferences = getAllReferences();
		doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
		doReturn(true).when(resourcesService).isReferencesAreFull(eq(multi), eq(allReferences));
		AnimediaMALTitleReferences animediaMALTitleReference = allReferences.stream()
				.filter(ref -> !ref.getTitleOnMAL().equals(BaseConstants.NOT_FOUND_ON_MAL)).findFirst().get();
		doReturn(false).when(malService).isTitleExist(eq(animediaMALTitleReference.getTitleOnMAL()));
		Pattern pattern = Pattern.compile("[а-яА-Я]");
		AnimediaTitleSearchInfo animediaTitleSearchInfo = animediaSearchListFromAnimedia.stream().filter(title -> {
			Matcher matcher = pattern.matcher(title.getKeywords());
			return !matcher.find() && !title.getKeywords().equals("");
		}).findFirst().get();
		Anime singleTitle = new Anime("1.1", animediaOnlineTv + animediaTitleSearchInfo.getUrl() + "/1/1", animediaTitleSearchInfo.getUrl());
		single.add(singleTitle);
		doReturn(true).when(resourcesService).isAllSingleSeasonAnimeHasConcretizedMALTitleName(eq(single), eq(animediaSearchListFromAnimedia));
		doReturn(false).when(malService).isTitleExist(animediaTitleSearchInfo.getKeywords());
		String scheduledMethod = Arrays.stream(resourcesCheckerService.getClass().getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Scheduled.class)).findFirst().get().getName();
		ReflectionTestUtils.invokeMethod(resourcesCheckerService, scheduledMethod);
		verify(resourcesService, times(2)).getAnimeSortedByType(eq(animediaSearchListFromAnimedia));
		assertTrue(RoutinesIO.isDirectoryExists(tempFolderName));
		String prefix = tempFolderName + File.separator;
		Set<AnimediaMALTitleReferences> referencesWithInvalidMALTitleName = RoutinesIO
				.unmarshalFromFile(prefix + resourcesNames.getTempReferencesWithInvalidMALTitleName(), AnimediaMALTitleReferences.class,
						LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> searchTitlesWithInvalidMALTitleName = RoutinesIO
				.unmarshalFromFile(prefix + resourcesNames.getTempSearchTitlesWithInvalidMALTitleName(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		assertEquals(1, referencesWithInvalidMALTitleName.stream().filter(ref -> ref.equals(animediaMALTitleReference)).count());
		assertEquals(1, searchTitlesWithInvalidMALTitleName.stream().filter(title -> title.equals(animediaTitleSearchInfo)).count());
	}

	@Test
	public void testCheckApplicationResourcesAnimediaSearchListFromGitHubIsNotUpToDateAndTitlesNotFoundOnMAL() {
		doReturn(false).when(resourcesService)
				.isAnimediaSearchListFromGitHubUpToDate(eq(animediaSearchListFromGitHub), eq(animediaSearchListFromAnimedia));
		doReturn(allTypes).when(resourcesService).getAnimeSortedByType(eq(animediaSearchListFromAnimedia));
		doReturn(new LinkedHashSet<>()).when(resourcesService).checkSortedAnime(eq(allTypes), eq(animediaSearchListFromAnimedia));
		doReturn(getAllReferences()).when(referencesManager).getMultiSeasonsReferences();
		Pattern pattern = Pattern.compile("[а-яА-Я]");
		AnimediaTitleSearchInfo animediaTitleSearchInfo = animediaSearchListFromGitHub.stream().filter(title -> {
			Matcher matcher = pattern.matcher(title.getKeywords());
			return !matcher.find() && !title.getKeywords().equals("");
		}).findFirst().get();
		Anime singleTitle = new Anime("1.1", animediaOnlineTv + animediaTitleSearchInfo.getUrl() + "/1/1", animediaTitleSearchInfo.getUrl());
		single.add(singleTitle);
		doReturn(true).when(resourcesService).isAllSingleSeasonAnimeHasConcretizedMALTitleName(eq(single), eq(animediaSearchListFromGitHub));
		doReturn(false).when(malService).isTitleExist(animediaTitleSearchInfo.getKeywords());
		String scheduledMethod = Arrays.stream(resourcesCheckerService.getClass().getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Scheduled.class)).findFirst().get().getName();
		ReflectionTestUtils.invokeMethod(resourcesCheckerService, scheduledMethod);
		verify(resourcesService, times(1)).getAnimeSortedByType(eq(animediaSearchListFromAnimedia));
		assertTrue(notFoundAnimeOnAnimediaRepository.findAll().isEmpty());
	}

	private Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromResources() {
		Set<AnimediaTitleSearchInfo> animediaSearchListFromResources = new LinkedHashSet<>();
		AnimediaTitleSearchInfo titleWithRusKeywords = new AnimediaTitleSearchInfo("мастера меча онлайн",
				"мастера меча онлайн sword art online",
				"anime/mastera-mecha-onlayn",
				"http://static.animedia.tv/uploads/dasdggsdfgdsd.jpg?h=350&q=100");
		AnimediaTitleSearchInfo titleWithEmptyKeywords = new AnimediaTitleSearchInfo("тест",
				"",
				"anime/something",
				"http://static.animedia.tv/uploads/dfgdfg.jpg?h=350&q=100");
		AnimediaTitleSearchInfo titleWithConcretizedMALNameInKeywords = new AnimediaTitleSearchInfo("чёрный клевер",
				"black clover",
				"anime/chyornyj-klever",
				"http://static.animedia.tv/uploads/KLEVER.jpg?h=350&q=100");
		animediaSearchListFromResources.add(titleWithRusKeywords);
		animediaSearchListFromResources.add(titleWithEmptyKeywords);
		animediaSearchListFromResources.add(titleWithConcretizedMALNameInKeywords);
		return animediaSearchListFromResources;
	}

	private Set<AnimediaMALTitleReferences> getAllReferences() {
		Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder().url("anime/skazka-o-hvoste-fei-TV1").dataList("1").firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175").titleOnMAL("fairy tail").build();
		AnimediaMALTitleReferences titleWithNoneTitleName = AnimediaMALTitleReferences.builder().url("anime/something").dataList("1").firstEpisode("1")
				.minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("12").currentMax("12").titleOnMAL(BaseConstants.NOT_FOUND_ON_MAL)
				.build();
		allReferences.add(fairyTail1);
		allReferences.add(titleWithNoneTitleName);
		return allReferences;
	}

	private Map<AnimeTypeOnAnimedia, Set<Anime>> getSortedAnime() {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = new EnumMap(AnimeTypeOnAnimedia.class);
		allTypes.put(SINGLESEASON, new LinkedHashSet<>());
		allTypes.put(MULTISEASONS, new LinkedHashSet<>());
		allTypes.put(ANNOUNCEMENT, new LinkedHashSet<>());
		return allTypes;
	}
}