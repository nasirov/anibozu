package nasirov.yv.anibozu.service.impl;

import nasirov.yv.anibozu.service.MalAnimeFilterI;
import nasirov.yv.starter_common.dto.mal.MalAnime;
import org.springframework.stereotype.Service;

@Service
public class WatchingAnimeFilter implements MalAnimeFilterI {

	@Override
	public boolean filter(MalAnime anime) {
		int maxEpisodes = anime.getMaxEpisodes();
		return maxEpisodes == 0 || anime.getWatchedEpisodes() < maxEpisodes;
	}
}
