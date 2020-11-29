package nasirov.yv.service.impl.common;

import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.Anime.AnimeBuilder;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeService implements AnimeServiceI {

	private final Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy;

	@Override
	public Anime buildAnime(Set<FanDubSource> fanDubSources, MalTitle watchingTitle) {
		log.debug("Trying to build Anime dto based on a mal title [{}]. Desired fandub sources [{}]...", watchingTitle.getAnimeUrl(), fanDubSources);
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getName())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		fanDubSources.forEach(x -> animeBuilder.fanDubUrl(x, buildEpisodeUrlViaEpisodeUrlService(watchingTitle, x)));
		Anime result = animeBuilder.build();
		log.debug("Successfully built {}", result);
		return result;
	}

	private String buildEpisodeUrlViaEpisodeUrlService(MalTitle watchingTitle, FanDubSource targetFanDubSource) {
		return episodeUrlStrategy.get(targetFanDubSource)
				.getEpisodeUrl(targetFanDubSource, watchingTitle);
	}
}
