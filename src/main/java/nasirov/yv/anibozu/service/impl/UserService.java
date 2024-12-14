package nasirov.yv.anibozu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.user.AnimeList;
import nasirov.yv.anibozu.dto.user.AnimeList.Anime;
import nasirov.yv.anibozu.dto.user.AnimeList.Anime.AnimeBuilder;
import nasirov.yv.anibozu.dto.user.EpisodeInfo;
import nasirov.yv.anibozu.mapper.AnimeDataMapper;
import nasirov.yv.anibozu.service.AnimeDataServiceI;
import nasirov.yv.anibozu.service.MalServiceI;
import nasirov.yv.anibozu.service.UserServiceI;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.mal.MalAnime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServiceI {

	private final MalServiceI malService;

	private final AnimeDataServiceI animeDataService;

	private final AnimeDataMapper mapper;

	@Override
	public Mono<AnimeList> getAnimeList(String username) {
		return malService.getAnimeList(username)
				.flatMapMany(Flux::fromIterable)
				.flatMap(this::buildAnime)
				.collectList()
				.map(AnimeList::new)
				.doOnSuccess(x -> log.info("Done [{}]", username));
	}

	private Mono<Anime> buildAnime(MalAnime malAnime) {
		int nextEpisode = malAnime.getWatchedEpisodes() + 1;
		AnimeBuilder result = Anime.builder()
				.name(malAnime.getName())
				.nextEpisode(nextEpisode)
				.maxEpisodes(malAnime.getMaxEpisodes())
				.posterUrl(malAnime.getPosterUrl())
				.malUrl(malAnime.getUrl());
		return animeDataService.getAnimeData(AnimeDataId.builder().malId(malAnime.getId()).episodeId(nextEpisode).build())
				.map(x -> result.episodes(buildEpisodes(x)).build());
	}

	private List<EpisodeInfo> buildEpisodes(AnimeData animeData) {
		return animeData.episodes().stream().map(mapper::toEpisodeInfo).toList();
	}
}
