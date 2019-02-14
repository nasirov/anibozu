package nasirov.yv.service;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.enums.AnimeTypeOnAnimedia;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static nasirov.yv.enums.AnimeTypeOnAnimedia.*;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class SchedulerService {
	private ReferencesManager referencesManager;
	
	private AnimediaService animediaService;
	
	@Autowired
	public SchedulerService(ReferencesManager referencesManager,
							AnimediaService animediaService) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
	}
	
	@Scheduled(cron = "${resources.check.cron.expression}")
	private void checkApplicationResources() {
		log.info("Start of checking classpath resources ...");
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
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
		Set<AnimediaTitleSearchInfo> notFoundInTheResources = animediaService.checkAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchList);
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
		log.info("End of checking classpath resources.");
	}
}
