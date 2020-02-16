package nasirov.yv.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
	public Set<Anime> getAnime(Set<FanDubSource> fanDubSources, Set<UserMALTitleInfo> watchingTitles) {
		return watchingTitles.stream()
				.map(x -> buildAnime(fanDubSources, x))
				.collect(Collectors.toSet());
	}

	private Anime buildAnime(Set<FanDubSource> fanDubSources, UserMALTitleInfo watchingTitle) {
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getTitle())
				.episode(buildNextEpisodeForWatch(watchingTitle))
				.posterUrlOnMAL(watchingTitle.getPosterUrl())
				.animeUrlOnMAL(watchingTitle.getAnimeUrl());
		fanDubSources.forEach(x -> animeBuilder.fanDubUrl(x, buildEpisodeUrlViaEpisodeUrlService(watchingTitle, x)));
		return animeBuilder.build();
	}

	private String buildNextEpisodeForWatch(UserMALTitleInfo watchingTitle) {
		return String.valueOf(watchingTitle.getNumWatchedEpisodes() + 1);
	}

	private String buildEpisodeUrlViaEpisodeUrlService(UserMALTitleInfo watchingTitle, FanDubSource targetFanDubSource) {
		return episodeUrlStrategy.get(targetFanDubSource)
				.getEpisodeUrl(watchingTitle);
	}
}
