package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface NineAnimeServiceI {

	Set<Anime> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles);
}
