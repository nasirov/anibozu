package nasirov.yv.service.impl.common;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.Anime.AnimeBuilder;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;
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

	private final FanDubProps fanDubProps;

	@Override
	public Mono<Anime> buildAnime(MalTitle watchingTitle, Map<FanDubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		AnimeBuilder animeBuilder = Anime.builder()
				.animeName(watchingTitle.getName())
				.malEpisodeNumber(nextEpisodeForWatch.toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		return Flux.fromStream(commonTitlesByFandubSource.entrySet().stream())
				.doOnNext(x -> enrichWithEpisodeNameAndUrl(animeBuilder, x, nextEpisodeForWatch))
				.then(Mono.just(animeBuilder))
				.map(AnimeBuilder::build)
				.doOnSubscribe(x -> log.debug("Trying to build Anime dto based on a mal title [{}]. Desired fandub sources [{}]...",
						watchingTitle.getAnimeUrl(), commonTitlesByFandubSource.keySet()))
				.doOnSuccess(x -> log.debug("Successfully built {}", x));
	}

	private void enrichWithEpisodeNameAndUrl(AnimeBuilder animeBuilder, Entry<FanDubSource, List<CommonTitle>> entry,
			Integer nextEpisodeForWatch) {
		FanDubSource fanDubSource = entry.getKey();
		Pair<String, String> result = Optional.of(entry.getValue())
				.filter(CollectionUtils::isNotEmpty)
				.map(x -> buildNameAndUrlPair(nextEpisodeForWatch, x, fanDubProps.getUrls().get(fanDubSource)))
				.orElse(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL);
		animeBuilder.fanDubUrl(fanDubSource, result.getValue());
		animeBuilder.fanDubEpisodeName(fanDubSource, result.getKey());
	}

	private Pair<String, String> buildNameAndUrlPair(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles,
			String fandubUrl) {
		return matchedTitles.stream()
				.map(CommonTitle::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()))
				.orElse(NOT_AVAILABLE_EPISODE_NAME_AND_URL);
	}
}
