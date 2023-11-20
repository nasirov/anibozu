package nasirov.yv.ab.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.Anime;
import nasirov.yv.ab.dto.fe.Anime.AnimeBuilder;
import nasirov.yv.ab.dto.fe.FandubInfo;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.internal.GithubCacheKey;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.FandubAnimeServiceI;
import nasirov.yv.ab.service.MalAnimeFilterI;
import nasirov.yv.ab.service.MalAnimeFormatterI;
import nasirov.yv.ab.service.MalAnimeServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisode;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService implements ProcessServiceI {

	private static final String EMPTY_ANIME_LIST_ERROR_MESSAGE = "Not found actual watching anime! Please, try again later.";

	private static final ProcessResult EMPTY_ANIME_LIST_FALLBACK = new ProcessResult(EMPTY_ANIME_LIST_ERROR_MESSAGE);

	private final MalAnimeServiceI malAnimeService;

	private final MalAnimeFilterI malAnimeFilter;

	private final MalAnimeFormatterI malAnimeFormatter;

	private final FandubAnimeServiceI fandubAnimeService;

	private final AppProps appProps;

	@Override
	public Mono<ProcessResult> process(String username) {
		log.info("Processing [{}]", username);
		return malAnimeService.getAnimeList(username)
				.map(x -> x.stream().filter(malAnimeFilter::filter).map(malAnimeFormatter::format).toList())
				.flatMap(x -> fandubAnimeService.getEpisodesMappedByKey().map(m -> buildProcessResult(m, x)))
				.doOnSuccess(x -> log.info("Done [{}]", username));
	}

	private ProcessResult buildProcessResult(Map<GithubCacheKey, List<FandubEpisode>> keyToEpisodes, List<MalAnime> malAnimeList) {
		List<Anime> animeList = new ArrayList<>();
		for (MalAnime malAnime : malAnimeList) {
			Integer malId = malAnime.getId();
			Integer nextEpisode = malAnime.getWatchedEpisodes() + 1;
			AnimeBuilder animeBuilder = Anime.builder()
					.name(malAnime.getName())
					.nextEpisode(nextEpisode.toString())
					.maxEpisodes(String.valueOf(malAnime.getMaxEpisodes()))
					.posterUrl(malAnime.getPosterUrl())
					.malUrl(malAnime.getUrl());
			for (FandubSource fandubSource : appProps.getEnabledFandubSources()) {
				GithubCacheKey key = new GithubCacheKey(fandubSource, malId);
				List<FandubEpisode> matchedEpisodes = Optional.ofNullable(keyToEpisodes.get(key))
						.map(x -> getMatchedEpisodes(x, nextEpisode))
						.orElse(List.of());
				matchedEpisodes.forEach(x -> animeBuilder.fandubInfoList(FandubInfo.builder()
						.fandubSource(fandubSource)
						.fandubSourceCanonicalName(fandubSource.getCanonicalName())
						.episodeUrl(x.getPath())
						.episodeName(x.getName())
						.types(getTypes(x))
						.build()));
			}
			animeList.add(animeBuilder.build());
		}
		return CollectionUtils.isNotEmpty(animeList) ? new ProcessResult(animeList) : EMPTY_ANIME_LIST_FALLBACK;
	}

	private List<FandubEpisode> getMatchedEpisodes(List<FandubEpisode> episodes, Integer nextEpisode) {
		return episodes.stream().filter(x -> nextEpisode.equals(x.getMalEpisodeId())).toList();
	}

	private List<String> getTypes(FandubEpisode episode) {
		List<String> result = new ArrayList<>();
		if (episode.isDub()) {
			result.add("dub");
		}
		if (episode.isSub()) {
			result.add("sub");
		}
		if (episode.isSoftSub()) {
			result.add("soft sub");
		}
		return result;
	}
}
