package nasirov.yv.ab.service.impl;

import nasirov.yv.ab.service.MalAnimeFilterI;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Service
public class WatchingAnimeFilter implements MalAnimeFilterI {

	@Override
	public boolean filter(MalAnime anime) {
		int maxEpisodes = anime.getMaxEpisodes();
		return maxEpisodes == 0 || anime.getWatchedEpisodes() < maxEpisodes;
	}
}
