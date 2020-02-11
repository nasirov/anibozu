package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.constants.FunDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface AnimeServiceI {

	/**
	 * Builds a set with {@link Anime} based on given watching titles and fundub sources
	 *
	 * @param funDubSources  fundub sources
	 * @param watchingTitles an user currently watching titles
	 * @return a set with {@link Anime}
	 */
	Set<Anime> getAnime(Set<FunDubSource> funDubSources, Set<UserMALTitleInfo> watchingTitles);
}
