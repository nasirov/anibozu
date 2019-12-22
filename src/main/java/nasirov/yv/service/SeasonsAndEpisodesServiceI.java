package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface SeasonsAndEpisodesServiceI {

	Set<TitleReference> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<TitleReference> matchedAndUpdatedReferences, String username);
}
