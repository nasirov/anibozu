package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.front.Anime;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * Created by nasirov.yv
 */
public interface AnimeServiceI {

	/**
	 * Builds an {@link Anime} based on given watching title and fandub sources
	 *
	 * @param fanDubSources fandub sources
	 * @param watchingTitle user currently watching title
	 * @return an {@link Anime} dto
	 */
	Anime buildAnime(Set<FanDubSource> fanDubSources, MalTitle watchingTitle);
}
