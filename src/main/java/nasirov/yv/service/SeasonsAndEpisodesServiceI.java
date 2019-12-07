package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface SeasonsAndEpisodesServiceI {

	Set<AnimediaMALTitleReferences> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<AnimediaMALTitleReferences> references,
			Set<AnimediaTitleSearchInfo> animediaSearchList, String username);

	void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> watchingTitles, AnimediaMALTitleReferences currentlyUpdatedTitleOnAnimedia,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache);

	void updateEpisodeNumberForWatchAndFinalUrl(Set<UserMALTitleInfo> updatedWatchingTitles, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<AnimediaTitleSearchInfo> animediaSearchList, String username);
}
