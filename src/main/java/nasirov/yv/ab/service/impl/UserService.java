package nasirov.yv.ab.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.Anime;
import nasirov.yv.ab.dto.fe.Anime.AnimeBuilder;
import nasirov.yv.ab.dto.fe.AnimeListResponse;
import nasirov.yv.ab.dto.fe.FandubInfo;
import nasirov.yv.ab.dto.internal.FandubData;
import nasirov.yv.ab.dto.internal.FandubKey;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.ab.service.MalAnimeFilterI;
import nasirov.yv.ab.service.MalAnimeFormatterI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.UserServiceI;
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
public class UserService implements UserServiceI {

	private static final String EMPTY_ANIME_LIST_ERROR_MESSAGE = "Not found actual watching anime! Please, try again later.";

	private static final AnimeListResponse EMPTY_ANIME_LIST_FALLBACK = new AnimeListResponse(EMPTY_ANIME_LIST_ERROR_MESSAGE);

	private final MalServiceI malService;

	private final MalAnimeFilterI malAnimeFilter;

	private final MalAnimeFormatterI malAnimeFormatter;

	private final FandubDataServiceI fandubDataService;

	private final AppProps appProps;

	@Override
	public Mono<AnimeListResponse> getAnimeList(String username) {
		return malService.getAnimeList(username)
				.map(this::processMalAnime)
				.flatMap(x -> fandubDataService.getFandubData().map(m -> buildResult(m, x)))
				.doOnSuccess(x -> log.info("Done [{}]", username));
	}

	private List<MalAnime> processMalAnime(List<MalAnime> malAnime) {
		return malAnime.stream().filter(malAnimeFilter::filter).map(malAnimeFormatter::format).toList();
	}

	private AnimeListResponse buildResult(FandubData fandubData, List<MalAnime> malAnimeList) {
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
			enrichFandubInfoList(fandubData, malId, nextEpisode, animeBuilder);
			animeList.add(animeBuilder.build());
		}
		return CollectionUtils.isNotEmpty(animeList) ? new AnimeListResponse(animeList) : EMPTY_ANIME_LIST_FALLBACK;
	}

	private void enrichFandubInfoList(FandubData fandubData, Integer malId, Integer nextEpisode, AnimeBuilder animeBuilder) {
		for (FandubSource fandubSource : appProps.getEnabledFandubSources()) {
			FandubKey key = new FandubKey(fandubSource, malId);
			Map<FandubKey, Map<Integer, List<FandubEpisode>>> episodes = fandubData.getEpisodes();
			List<FandubEpisode> matchedEpisodes = Optional.ofNullable(episodes.get(key)).map(x -> x.get(nextEpisode)).orElse(List.of());
			matchedEpisodes.forEach(x -> animeBuilder.fandubInfoList(buildFandubInfo(fandubSource, x)));
		}
	}

	private FandubInfo buildFandubInfo(FandubSource fandubSource, FandubEpisode episode) {
		return FandubInfo.builder()
				.fandubSource(fandubSource)
				.fandubSourceCanonicalName(fandubSource.getCanonicalName())
				.episodeUrl(episode.getPath())
				.episodeName(episode.getName())
				.types(buildTypes(episode))
				.build();
	}

	private List<String> buildTypes(FandubEpisode episode) {
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
