package nasirov.yv.service;

import static nasirov.yv.enums.AnimeTypeOnAnimedia.ANNOUNCEMENT;
import static nasirov.yv.enums.AnimeTypeOnAnimedia.MULTISEASONS;
import static nasirov.yv.enums.AnimeTypeOnAnimedia.SINGLESEASON;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.enums.AnimeTypeOnAnimedia;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
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
public class SchedulerService {

	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;

	private ReferencesManager referencesManager;

	private AnimediaService animediaService;

	private CacheManager cacheManager;

	@Autowired
	public SchedulerService(ReferencesManager referencesManager, AnimediaService animediaService, CacheManager cacheManager) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
		this.cacheManager = cacheManager;
	}

	@Scheduled(cron = "${resources.check.cron.expression}")
	private void checkApplicationResources() {
		log.info("START OF CHECKING CLASSPATH RESOURCES ...");
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaSearchListCache.get(animediaSearchListCacheName, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> animediaSearchListFresh = animediaService.getAnimediaSearchList();
		Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes;
		boolean animediaSearchListUpToDate = animediaService.isAnimediaSearchListUpToDate(animediaSearchList, animediaSearchListFresh);
		if (animediaSearchListUpToDate) {
			allTypes = animediaService.getAnimeSortedByTypeFromResources();
			log.info("ANIMEDIA TITLE SEARCH LIST IS UP-TO-DATE.");
			if (allTypes.isEmpty()) {
				log.info("START OF CREATING SORTED ANIME BASED ON ANIMEDIA SEARCH LIST FROM RESOURCES ...");
				allTypes = animediaService.getAnimeSortedByType(animediaSearchList);
				log.info("SORTED ANIME BY TYPE ARE SUCCESSFULLY CREATED.");
			}
		} else {
			log.info("ANIMEDIA TITLE SEARCH LIST ISN'T UP-TO-DATE.");
			log.info("START OF CREATING SORTED ANIME BASED ON FRESH ANIMEDIA SEARCH LIST ...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchListFresh);
			log.info("SORTED ANIME BY TYPE ARE SUCCESSFULLY CREATED.");
		}
		Set<Anime> singleSeasonAnime = allTypes.get(SINGLESEASON);
		Set<Anime> multiSeasonsAnime = allTypes.get(MULTISEASONS);
		Set<Anime> announcements = allTypes.get(ANNOUNCEMENT);
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = animediaService
				.checkSortedAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchListFresh);
		if (!notFoundInTheResources.isEmpty()) {
			log.info("SORTED ANIME AREN'T UP-TO-DATE. START OF UPDATING SORTED ANIME ...");
			allTypes = animediaService.getAnimeSortedByType(animediaSearchList);
			singleSeasonAnime = allTypes.get(SINGLESEASON);
			multiSeasonsAnime = allTypes.get(MULTISEASONS);
			log.info("END OF UPDATING SORTED ANIME.");
		}
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		boolean referencesAreFull = referencesManager.isReferencesAreFull(multiSeasonsAnime, allReferences);
		if (referencesAreFull) {
			log.info("CLASSPATH MULTISEASONS REFERENCES ARE UP-TO-DATE.");
		}
		boolean allSingleSeasonTitlesHasConcretizedMALName = animediaService
				.isAllSingleSeasonAnimeHasConcretizedMALTitleInKeywordsInAnimediaSearchListFromResources(singleSeasonAnime, animediaSearchList);
		if (allSingleSeasonTitlesHasConcretizedMALName) {
			log.info("ALL SINGLE SEASON ANIME HAS CONCRETIZED MAL NAME.");
		}
		log.info("END OF CHECKING CLASSPATH RESOURCES.");
	}
}
