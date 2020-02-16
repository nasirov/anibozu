package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface AnimeServiceI {

	/**
	 * Builds a set with {@link Anime} based on given watching titles and fandub sources
	 *
	 * @param fanDubSources  fandub sources
	 * @param watchingTitles an user currently watching titles
	 * @return a set with {@link Anime}
	 */
	Set<Anime> getAnime(Set<FanDubSource> fanDubSources, Set<UserMALTitleInfo> watchingTitles);
}
