package nasirov.yv.service;

import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.data.animedia.AnimeTypeOnAnimedia.SINGLESEASON;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class SchedulerService {

	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;

	@Value("${resources.tempFolder.name}")
	private String tempFolderName;

	@Value("${resources.tempReferencesWithInvalidMALTitleName.name}")
	private String tempReferencesWithInvalidMALTitleName;

	@Value("${resources.tempSearchTitlesWithInvalidMALTitleName.name}")
	private String tempSearchTitlesWithInvalidMALTitleName;

	private ReferencesManager referencesManager;

	private AnimediaService animediaService;

	private CacheManager cacheManager;

	private MALService malService;

	@Autowired
	public SchedulerService(ReferencesManager referencesManager, AnimediaService animediaService, CacheManager cacheManager, MALService malService) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
		this.cacheManager = cacheManager;
		this.malService = malService;
	}

	@Scheduled(cron = "${resources.check.cron.expression}")
	private void checkApplicationResources() {
		log.info("START OF CHECKING CLASSPATH RESOURCES ...");
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaSearchListCache.get(animediaSearchListCacheName, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> animediaSearchListFresh = animediaService.getAnimediaSearchList();
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes = checkAnimediaSearchListAndSortedAnime(animediaSearchList, animediaSearchListFresh);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		checkMultiSeasonsReferences(multiSeasonsAnime);
		checkSingleSeasonTitles(singleSeasonAnime, animediaSearchList);
		log.info("END OF CHECKING CLASSPATH RESOURCES.");
	}

	private Map<AnimeTypeOnAnimedia, Set<Anime>> checkAnimediaSearchListAndSortedAnime(Set<AnimediaTitleSearchInfo> animediaSearchList,
			Set<AnimediaTitleSearchInfo> animediaSearchListFresh) {
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes;
		boolean animediaSearchListUpToDate = animediaService.isAnimediaSearchListUpToDate(animediaSearchList, animediaSearchListFresh);
		if (animediaSearchListUpToDate) {
			allTypes = animediaService.getAnimeSortedByTypeFromResources();
			log.info("ANIMEDIA SEARCH LIST IS UP-TO-DATE.");
			if (allTypes.isEmpty()) {
				log.info("START OF CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM RESOURCES ...");
				allTypes = animediaService.getAnimeSortedByType(animediaSearchList);
				log.info("END OF CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM RESOURCES.");
			}
		} else {
			log.info("ANIMEDIA SEARCH LIST ISN'T UP-TO-DATE.");
			log.info("START OF CREATING SORTED ANIME BASED ON FRESH ANIMEDIA SEARCH LIST ...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchListFresh);
			log.info("END OF CREATING SORTED ANIME BASED ON FRESH ANIMEDIA SEARCH LIST.");
		}
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> announcements = allTypes.get(ANNOUNCEMENT);
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = animediaService
				.checkSortedAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchListFresh);
		if (!notFoundInTheResources.isEmpty()) {
			log.info("SORTED ANIME AREN'T UP-TO-DATE.");
			log.info("START OF UPDATING SORTED ANIME ...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchList);
			log.info("END OF UPDATING SORTED ANIME.");
		}
		return allTypes;
	}

	private void checkMultiSeasonsReferences(Set<Anime> multiSeasonsAnime) {
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		boolean referencesAreFull = referencesManager.isReferencesAreFull(multiSeasonsAnime, allReferences);
		if (referencesAreFull) {
			log.info("CLASSPATH MULTISEASONS REFERENCES ARE UP-TO-DATE.");
			log.info("START OF CHECKING MULTISEASONS REFERENCES TITLE NAME ON MAL ...");
			Set<AnimediaMALTitleReferences> referencesWithInvalidMALTitleName = new LinkedHashSet<>();
			for (AnimediaMALTitleReferences reference : allReferences) {
				String titleOnMAL = reference.getTitleOnMAL();
				if (!titleOnMAL.equalsIgnoreCase("none")) {
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
			log.info("END OF CHECKING MULTISEASONS REFERENCES TITLE NAME ON MAL.");
		}
	}

	private void checkSingleSeasonTitles(Set<Anime> singleSeasonAnime, Set<AnimediaTitleSearchInfo> animediaSearchList) {
		boolean allSingleSeasonTitlesHasConcretizedMALName = animediaService
				.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeasonAnime, animediaSearchList);
		if (allSingleSeasonTitlesHasConcretizedMALName) {
			log.info("ALL SINGLESEASON ANIME HAS CONCRETIZED MAL NAMES.");
			log.info("START OF CHECKING SINGLESEASON TITLE NAME ON MAL ...");
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
			log.info("END OF CHECKING SINGLESEASON TITLE NAME ON MAL.");
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
