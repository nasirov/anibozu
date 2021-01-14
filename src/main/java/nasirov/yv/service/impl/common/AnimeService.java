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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeService implements AnimeServiceI {

	private final Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy;

	@Override
	public Mono<Anime> buildAnime(Set<FanDubSource> fanDubSources, MalTitle watchingTitle) {
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getName())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		return Flux.fromIterable(fanDubSources)
				.flatMap(x -> buildFandubSourceEpisodeUrlPair(watchingTitle, x))
				.doOnNext(x -> animeBuilder.fanDubUrl(x.getKey(), x.getValue()))
				.then(Mono.just(animeBuilder))
				.map(AnimeBuilder::build)
				.doOnSubscribe(x -> log.debug("Trying to build Anime dto based on a mal title [{}]. Desired fandub sources [{}]...",
						watchingTitle.getAnimeUrl(),
						fanDubSources))
				.doOnSuccess(x -> log.debug("Successfully built {}", x));
	}

	private Mono<Pair<FanDubSource, String>> buildFandubSourceEpisodeUrlPair(MalTitle watchingTitle, FanDubSource targetFanDubSource) {
		return episodeUrlStrategy.get(targetFanDubSource)
				.getEpisodeUrl(watchingTitle)
				.map(x -> Pair.of(targetFanDubSource, x));
	}
}
