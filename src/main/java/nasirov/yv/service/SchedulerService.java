package nasirov.yv.service;

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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static nasirov.yv.enums.AnimeTypeOnAnimedia.*;

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
	public SchedulerService(ReferencesManager referencesManager,
							AnimediaService animediaService,
							CacheManager cacheManager) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
		this.cacheManager = cacheManager;
	}
	
	@Scheduled(cron = "${resources.check.cron.expression}")
	private void checkApplicationResources() {
		log.info("Start of checking classpath resources ...");
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaSearchListCache.get(animediaSearchListCacheName, LinkedHashSet.class);
		Set<AnimediaTitleSearchInfo> animediaSearchListFresh = animediaService.getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		Map<AnimeTypeOnAnimedia, Set<Anime>> allSeasons = animediaService.getAnimeSortedForTypeFromResources();
		if (allSeasons.isEmpty()) {
			log.info("Start of creating sorted anime ...");
			allSeasons = animediaService.getAnimeSortedForType(animediaSearchList);
			log.info("Sorted anime for type are successfully created.");
		}
		Set<Anime> singleSeasonAnime = allSeasons.get(SINGLESEASON);
		Set<Anime> multiSeasonsAnime = allSeasons.get(MULTISEASONS);
		Set<Anime> announcements = allSeasons.get(ANNOUNCEMENT);
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = animediaService.checkSortedAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchList);
		if (!notFoundInTheResources.isEmpty()) {
			log.info("Sorted anime from classpath aren't up-to-date. Start of updating sorted anime ...");
			allSeasons = animediaService.getAnimeSortedForType(animediaSearchList);
			multiSeasonsAnime = allSeasons.get(MULTISEASONS);
			log.info("End of updating sorted anime.");
		}
		boolean referencesAreFull = referencesManager.isReferencesAreFull(multiSeasonsAnime, allReferences);
		if (referencesAreFull) {
			log.info("Classpath multiseasons references are up-to-date.");
		}
		boolean animediaSearchListUpToDate = animediaService.isAnimediaSearchListUpToDate(animediaSearchList, animediaSearchListFresh);
		if (animediaSearchListUpToDate) {
			log.info("Animedia title search list is up-to-date.");
		}
		log.info("End of checking classpath resources.");
	}
}
