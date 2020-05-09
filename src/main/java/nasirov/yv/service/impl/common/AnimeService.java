package nasirov.yv.service.impl.common;

import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.Anime.AnimeBuilder;
import nasirov.yv.data.mal.UserMALTitleInfo;
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
	public Anime buildAnime(Set<FanDubSource> fanDubSources, UserMALTitleInfo watchingTitle) {
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getTitle())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMAL(watchingTitle.getPosterUrl())
				.animeUrlOnMAL(watchingTitle.getAnimeUrl());
		fanDubSources.forEach(x -> animeBuilder.fanDubUrl(x, buildEpisodeUrlViaEpisodeUrlService(watchingTitle, x)));
		return animeBuilder.build();
	}

	private String buildEpisodeUrlViaEpisodeUrlService(UserMALTitleInfo watchingTitle, FanDubSource targetFanDubSource) {
		return episodeUrlStrategy.get(targetFanDubSource)
				.getEpisodeUrl(watchingTitle);
	}
}
