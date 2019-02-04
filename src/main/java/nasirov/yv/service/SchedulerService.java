package nasirov.yv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class SchedulerService {
	private ReferencesManager referencesManager;
	
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	private AnimediaService animediaService;
	
	@Autowired
	public SchedulerService(ReferencesManager referencesManager,
							SeasonAndEpisodeChecker seasonAndEpisodeChecker,
							AnimediaService animediaService) {
		this.referencesManager = referencesManager;
		this.seasonAndEpisodeChecker = seasonAndEpisodeChecker;
		this.animediaService = animediaService;
	}
	
	private void z() {
		// TODO: 25.01.2019 в шедуллер
//            List<Set<Anime>> allSeasons = animediaService.getAnime(animediaSearchList);
//            Set<Anime> singleSeasonAnime = allSeasons.get(0);
//            Set<Anime> multiSeasonsAnime = allSeasons.get(1);
//            Set<Anime> announcements = allSeasons.get(2);
		// Set<AnimediaTitleSearchInfo> notFound = animediaService.checkAnime(singleSeasonAnime, multiSeasonsAnime, announcements, animediaSearchList);
		// TODO: 25.01.2019 в шедуллер
		// referencesManager.checkReferences(multiSeasonsAnime, allReferences);
		// seasonAndEpisodeChecker.differences(allReferences, matchedAnime);
	}
}
