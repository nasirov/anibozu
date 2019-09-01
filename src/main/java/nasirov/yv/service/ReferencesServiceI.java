package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface ReferencesServiceI {

	Set<AnimediaMALTitleReferences> getMultiSeasonsReferences();

	void updateReferences(Set<AnimediaMALTitleReferences> references);

	Set<AnimediaMALTitleReferences> getMatchedReferences(Set<AnimediaMALTitleReferences> references, Set<UserMALTitleInfo> watchingTitles);

	void updateCurrentMax(Set<AnimediaMALTitleReferences> matchedAnimeFromCache, AnimediaMALTitleReferences currentlyUpdatedTitle);
}
