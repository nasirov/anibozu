package nasirov.yv.service.impl.common;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.Anime.AnimeBuilder;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.util.MalUtils;
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

	private final Map<FanDubSource, EpisodeNameAndUrlServiceI> episodeNameAndUrlServiceStrategy;

	@Override
	public Mono<Anime> buildAnime(MalTitle watchingTitle, Map<FanDubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getName())
				.malEpisodeNumber(nextEpisodeForWatch.toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		return Flux.fromStream(commonTitlesByFandubSource.entrySet()
						.stream())
				.flatMap(x -> mapEpisodeNameAndUrlByFandubSource(watchingTitle, x))
				.doOnNext(x -> enrichWithEpisodeNameAndUrl(animeBuilder, x))
				.then(Mono.just(animeBuilder))
				.map(AnimeBuilder::build)
				.doOnSubscribe(x -> log.debug("Trying to build Anime dto based on a mal title [{}]. Desired fandub sources [{}]...",
						watchingTitle.getAnimeUrl(),
						commonTitlesByFandubSource.keySet()))
				.doOnSuccess(x -> log.debug("Successfully built {}", x));
	}

	private Mono<Pair<FanDubSource, Pair<String, String>>> mapEpisodeNameAndUrlByFandubSource(MalTitle watchingTitle,
			Entry<FanDubSource, List<CommonTitle>> entry) {
		FanDubSource targetFanDubSource = entry.getKey();
		return episodeNameAndUrlServiceStrategy.get(targetFanDubSource)
				.getEpisodeNameAndUrl(watchingTitle, entry.getValue())
				.map(x -> Pair.of(targetFanDubSource, x));
	}

	private void enrichWithEpisodeNameAndUrl(AnimeBuilder animeBuilder, Pair<FanDubSource, Pair<String, String>> episodeNameAndUrlByFandubSource) {
		FanDubSource fanDubSource = episodeNameAndUrlByFandubSource.getKey();
		Pair<String, String> episodeNameAndUrl = episodeNameAndUrlByFandubSource.getValue();
		animeBuilder.fanDubUrl(fanDubSource, episodeNameAndUrl.getValue());
		animeBuilder.fanDubEpisodeName(fanDubSource, episodeNameAndUrl.getKey());
	}
}
