package nasirov.yv.service.scheduler;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.ResourcesServiceI;
import nasirov.yv.util.RoutinesIO;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourcesCheckerService {

	private static final Pattern CYRILLIC_CHARACTERS_PATTERN = Pattern.compile("[а-яА-Я]");

	private final ReferencesServiceI referencesManager;

	private final AnimediaServiceI animediaService;

	private final ResourcesServiceI resourcesService;

	private final MALServiceI malService;

	private final ResourcesNames resourcesNames;

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	private final RoutinesIO routinesIO;

	private String tempFolder;

	@PostConstruct
	public void init() {
		tempFolder = resourcesNames.getTempFolder();
	}

	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	private void checkApplicationResources() {
		log.info("START CHECKING TITLES RESOURCES FROM GITHUB ...");
		Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia = animediaService.getAnimediaSearchListFromAnimedia();
		Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub = animediaService.getAnimediaSearchListFromGitHub();
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = checkAnimediaSearchListAndSortedAnime(animediaSearchListFromAnimedia,
				animediaSearchListFromGitHub);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		checkReferences(multiSeasonsAnime, allReferences);
		checkSingleSeasonTitles(singleSeasonAnime, animediaSearchListFromGitHub);
		checkNotFoundTitlesOnAnimedia(allReferences, animediaSearchListFromGitHub);
		log.info("END CHECKING TITLES RESOURCES FROM GITHUB.");
	}

	private Map<AnimeTypeOnAnimedia, Set<Anime>> checkAnimediaSearchListAndSortedAnime(Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes;
		boolean isAnimediaSearchListFromGitHubUpToDate = resourcesService
				.isAnimediaSearchListFromGitHubUpToDate(animediaSearchListFromGitHub, animediaSearchListFromAnimedia);
		if (isAnimediaSearchListFromGitHubUpToDate) {
			allTypes = resourcesService.getAnimeSortedByTypeFromCache();
			log.info("ANIMEDIA SEARCH LIST FROM GITHUB IS UP-TO-DATE.");
			if (allTypes.isEmpty()) {
				log.info("START CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM GITHUB ...");
				allTypes = resourcesService.getAnimeSortedByType(animediaSearchListFromGitHub);
				log.info("END CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM GITHUB.");
			}
		} else {
			log.warn("ANIMEDIA SEARCH LIST FROM GITHUB ISN'T UP-TO-DATE.");
			log.warn("START CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM ANIMEDIA...");
			allTypes = resourcesService.getAnimeSortedByType(animediaSearchListFromAnimedia);
			log.warn("END CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM ANIMEDIA.");
		}
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = resourcesService.checkSortedAnime(allTypes, animediaSearchListFromAnimedia);
		if (!notFoundInTheResources.isEmpty()) {
			log.warn("SORTED ANIME AREN'T UP-TO-DATE.");
			log.warn("START UPDATING SORTED ANIME ...");
			allTypes = resourcesService.getAnimeSortedByType(animediaSearchListFromAnimedia);
			log.warn("END UPDATING SORTED ANIME.");
		}
		return allTypes;
	}

	private void checkReferences(Set<Anime> multiSeasonsAnime, Set<AnimediaMALTitleReferences> allReferences) {
		boolean referencesAreFull = resourcesService.isReferencesAreFull(multiSeasonsAnime, allReferences);
		if (referencesAreFull) {
			log.info("REFERENCES ARE UP-TO-DATE.");
			log.info("START CHECKING REFERENCES TITLE NAME ON MAL ...");
			Set<AnimediaMALTitleReferences> referencesWithInvalidMALTitleName = new LinkedHashSet<>();
			for (AnimediaMALTitleReferences reference : allReferences) {
				String titleOnMAL = reference.getTitleOnMAL();
				if (!isTitleNotFoundOnMAL(reference)) {
					boolean titleExist = malService.isTitleExist(titleOnMAL);
					if (!titleExist) {
						log.error("TITLE NAME {} FROM {} DOESN'T EXIST!", titleOnMAL, reference);
						referencesWithInvalidMALTitleName.add(reference);
					}
				}
			}
			if (!referencesWithInvalidMALTitleName.isEmpty()) {
				routinesIO.marshalToFileInTheFolder(tempFolder, resourcesNames.getTempReferencesWithInvalidMALTitleName(),
						referencesWithInvalidMALTitleName);
			}
			log.info("END CHECKING REFERENCES TITLE NAME ON MAL.");
		} else {
			log.warn("REFERENCES AREN'T UP-TO-DATE. CHECK {}", resourcesNames.getTempRawReferences());
		}
	}

	private void checkSingleSeasonTitles(Set<Anime> singleSeasonAnime, Set<AnimediaTitleSearchInfo> animediaSearchList) {
		boolean allSingleSeasonTitlesHasConcretizedMALName = resourcesService
				.isAllSingleSeasonAnimeHasConcretizedMALTitleName(singleSeasonAnime, animediaSearchList);
		if (allSingleSeasonTitlesHasConcretizedMALName) {
			log.info("ALL SINGLESEASON ANIME HAS CONCRETIZED MAL NAMES.");
			log.info("START CHECKING SINGLESEASON TITLE NAME ON MAL ...");
			Set<AnimediaTitleSearchInfo> tempAllSingleSeasonTitles = new LinkedHashSet<>();
			for (Anime x : singleSeasonAnime) {
				animediaSearchList.stream().filter(title -> {
					Matcher matcher = CYRILLIC_CHARACTERS_PATTERN.matcher(title.getKeywords());
					return title.getUrl().equals(x.getRootUrl()) && !matcher.find() && !title.getKeywords().equals("");
				}).forEach(tempAllSingleSeasonTitles::add);
			}
			Set<AnimediaTitleSearchInfo> searchTitlesWithInvalidMALTitleName = new LinkedHashSet<>();
			for (AnimediaTitleSearchInfo title : tempAllSingleSeasonTitles) {
				String titleOnMAL = title.getKeywords();
				if (StringUtils.isNotBlank(titleOnMAL) && !isTitleNotFoundOnMAL(title)) {
					boolean titleExist = malService.isTitleExist(titleOnMAL);
					if (!titleExist) {
						log.error("TITLE NAME {} FROM {} DOESN'T EXIST!", titleOnMAL, title);
						searchTitlesWithInvalidMALTitleName.add(title);
					}
				}
			}
			if (!searchTitlesWithInvalidMALTitleName.isEmpty()) {
				routinesIO
						.marshalToFileInTheFolder(tempFolder, resourcesNames.getTempSearchTitlesWithInvalidMALTitleName(), searchTitlesWithInvalidMALTitleName);
			}
			log.info("END CHECKING SINGLESEASON TITLE NAME ON MAL.");
		}
	}

	private void checkNotFoundTitlesOnAnimedia(Set<AnimediaMALTitleReferences> allReferences,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		List<UserMALTitleInfo> notFoundAnimeOnAnimedia = notFoundAnimeOnAnimediaRepository.findAll();
		for (UserMALTitleInfo notFoundTitle : notFoundAnimeOnAnimedia) {
			if (allReferences.stream().anyMatch(ref -> ref.getTitleOnMAL().equals(notFoundTitle.getTitle())) || animediaSearchListFromGitHub.stream()
					.anyMatch(searchTitle -> searchTitle.getKeywords().equals(notFoundTitle.getTitle()))) {
				log.info("{} HAS REMOVED FROM NotFoundAnimeOnAnimediaRepository", notFoundTitle.getTitle());
				notFoundAnimeOnAnimediaRepository.delete(notFoundTitle);
			}
		}
	}
}
