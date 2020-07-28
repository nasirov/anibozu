package nasirov.yv.service.impl.common;

import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.Anime.AnimeBuilder;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeService implements AnimeServiceI {

	private final Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy;

	@Override
	public Anime buildAnime(Set<FanDubSource> fanDubSources, MalTitle watchingTitle) {
		log.debug("Trying to build Anime dto based on a mal title with name [{}], id [{}]. Desired fandub sources [{}]...",
				watchingTitle.getName(),
				watchingTitle.getId(),
				fanDubSources);
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getName())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMAL(watchingTitle.getPosterUrl())
				.animeUrlOnMAL(watchingTitle.getAnimeUrl());
		fanDubSources.forEach(x -> animeBuilder.fanDubUrl(x, buildEpisodeUrlViaEpisodeUrlService(watchingTitle, x)));
		Anime result = animeBuilder.build();
		log.debug("Successfully built {}", result);
		return result;
	}

	private String buildEpisodeUrlViaEpisodeUrlService(MalTitle watchingTitle, FanDubSource targetFanDubSource) {
		return episodeUrlStrategy.get(targetFanDubSource)
				.getEpisodeUrl(watchingTitle);
	}
}
