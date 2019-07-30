package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;
import static nasirov.yv.data.enums.Constants.NOT_FOUND_ON_MAL;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class SchedulerService {

	@Value("${resources.tempFolder.name}")
	private String tempFolderName;

	@Value("${resources.tempReferencesWithInvalidMALTitleName.name}")
	private String tempReferencesWithInvalidMALTitleName;

	@Value("${resources.tempSearchTitlesWithInvalidMALTitleName.name}")
	private String tempSearchTitlesWithInvalidMALTitleName;

	private ReferencesManager referencesManager;

	private AnimediaService animediaService;

	private MALService malService;

	@Autowired
	public SchedulerService(ReferencesManager referencesManager, AnimediaService animediaService, MALService malService) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
		this.malService = malService;
	}

	//@Scheduled(cron = "${resources.check.cron.expression}")
	private void checkApplicationResources() {
		log.info("START CHECKING TITLES RESOURCES FROM GITHUB ...");
		Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia = animediaService.getAnimediaSearchListFromAnimedia();
		Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub = animediaService.getAnimediaSearchListFromGitHub();
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = checkAnimediaSearchListAndSortedAnime(animediaSearchListFromAnimedia,
				animediaSearchListFromGitHub);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		checkReferences(multiSeasonsAnime);
		checkSingleSeasonTitles(singleSeasonAnime, animediaSearchListFromGitHub);
		log.info("END CHECKING TITLES RESOURCES FROM GITHUB.");
	}

	private Map<AnimeTypeOnAnimedia, Set<Anime>> checkAnimediaSearchListAndSortedAnime(Set<AnimediaTitleSearchInfo> animediaSearchListFromAnimedia,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromGitHub) {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes;
		boolean isAnimediaSearchListFromGitHubUpToDate = animediaService
				.isAnimediaSearchListUpToDate(animediaSearchListFromGitHub, animediaSearchListFromAnimedia);
		if (isAnimediaSearchListFromGitHubUpToDate) {
			allTypes = animediaService.getAnimeSortedByTypeFromResources();
			log.info("ANIMEDIA SEARCH LIST FROM GITHUB IS UP-TO-DATE.");
			if (allTypes.isEmpty()) {
				log.info("START CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM GITHUB ...");
				allTypes = animediaService.getAnimeSortedByType(animediaSearchListFromGitHub);
				log.info("END CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM GITHUB.");
			}
		} else {
			log.info("ANIMEDIA SEARCH LIST FROM GITHUB ISN'T UP-TO-DATE.");
			log.info("START CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM ANIMEDIA...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchListFromAnimedia);
			log.info("END CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM ANIMEDIA.");
		}
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> announcements = allTypes.get(ANNOUNCEMENT);
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = animediaService
				.checkSortedAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchListFromAnimedia);
		if (!notFoundInTheResources.isEmpty()) {
			log.info("SORTED ANIME AREN'T UP-TO-DATE.");
			log.info("START UPDATING SORTED ANIME ...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchListFromAnimedia);
			log.info("END UPDATING SORTED ANIME.");
		}
		return allTypes;
	}

	private void checkReferences(Set<Anime> multiSeasonsAnime) {
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		boolean referencesAreFull = referencesManager.isReferencesAreFull(multiSeasonsAnime, allReferences);
		if (referencesAreFull) {
			log.info("REFERENCES ARE UP-TO-DATE.");
			log.info("START CHECKING REFERENCES TITLE NAME ON MAL ...");
			Set<AnimediaMALTitleReferences> referencesWithInvalidMALTitleName = new LinkedHashSet<>();
			for (AnimediaMALTitleReferences reference : allReferences) {
				String titleOnMAL = reference.getTitleOnMAL();
				if (!titleOnMAL.equalsIgnoreCase(NOT_FOUND_ON_MAL.getDescription())) {
					boolean titleExist = malService.isTitleExist(titleOnMAL);
					if (!titleExist) {
						log.error("TITLE NAME {} FROM {} DOESN'T EXIST!", titleOnMAL, reference);
						referencesWithInvalidMALTitleName.add(reference);
					}
				}
			}
			if (!referencesWithInvalidMALTitleName.isEmpty()) {
				marshalToFileInTheTempFolder(tempReferencesWithInvalidMALTitleName, referencesWithInvalidMALTitleName);
			}
			log.info("END CHECKING REFERENCES TITLE NAME ON MAL.");
		}
	}

	private void checkSingleSeasonTitles(Set<Anime> singleSeasonAnime, Set<AnimediaTitleSearchInfo> animediaSearchList) {
		boolean allSingleSeasonTitlesHasConcretizedMALName = animediaService
				.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeasonAnime, animediaSearchList);
		if (allSingleSeasonTitlesHasConcretizedMALName) {
			log.info("ALL SINGLESEASON ANIME HAS CONCRETIZED MAL NAMES.");
			log.info("START CHECKING SINGLESEASON TITLE NAME ON MAL ...");
			Set<AnimediaTitleSearchInfo> tempAllSingleSeasonTitles = new LinkedHashSet<>();
			Pattern pattern = Pattern.compile("[а-яА-Я]");
			for (Anime x : singleSeasonAnime) {
				animediaSearchList.stream().filter(title -> {
					Matcher matcher = pattern.matcher(title.getKeywords());
					return title.getUrl().equals(x.getRootUrl()) && !matcher.find() && !title.getKeywords().equals("");
				}).forEach(tempAllSingleSeasonTitles::add);
			}
			Set<AnimediaTitleSearchInfo> searchTitlesWithInvalidMALTitleName = new LinkedHashSet<>();
			for (AnimediaTitleSearchInfo title : tempAllSingleSeasonTitles) {
				String titleOnMAL = title.getKeywords();
				if (!titleOnMAL.equals("")) {
					boolean titleExist = malService.isTitleExist(titleOnMAL);
					if (!titleExist) {
						log.error("TITLE NAME {} FROM {} DOESN'T EXIST!", titleOnMAL, title);
						searchTitlesWithInvalidMALTitleName.add(title);
					}
				}
			}
			if (!searchTitlesWithInvalidMALTitleName.isEmpty()) {
				marshalToFileInTheTempFolder(tempSearchTitlesWithInvalidMALTitleName, searchTitlesWithInvalidMALTitleName);
			}
			log.info("END CHECKING SINGLESEASON TITLE NAME ON MAL.");
		}
	}

	private void marshalToFileInTheTempFolder(String fileName, Object content) {
		try {
			if (!RoutinesIO.isDirectoryExists(tempFolderName)) {
				RoutinesIO.mkDir(tempFolderName);
			}
			String prefix = tempFolderName + File.separator;
			RoutinesIO.marshalToFile(prefix + fileName, content);
		} catch (NotDirectoryException e) {
			log.error("CHECK system.properties VARIABLE resources.tempfolder.name! {} IS NOT A DIRECTORY!", tempFolderName);
		}
	}
}
