package nasirov.yv.anibozu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.user.AnimeList;
import nasirov.yv.anibozu.dto.user.AnimeList.Anime;
import nasirov.yv.anibozu.dto.user.AnimeList.Anime.AnimeBuilder;
import nasirov.yv.anibozu.mapper.AnimeDataMapper;
import nasirov.yv.anibozu.service.AnimeDataServiceI;
import nasirov.yv.anibozu.service.MalServiceI;
import nasirov.yv.anibozu.service.UserServiceI;
import nasirov.yv.starter_common.dto.mal.MalAnime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServiceI {

	private static final int MAL_ANIME_AIRING_STATUS = 1;

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
				.malUrl(malAnime.getUrl())
				.airing(malAnime.getAnimeAiringStatus() == MAL_ANIME_AIRING_STATUS);
		return animeDataService.getAnimeData(AnimeDataId.builder().malId(malAnime.getId()).episodeId(nextEpisode).build())
				.map(x -> result.dub(x.episodes().dub())
						.sub(x.episodes().sub())
						.episodes(x.episodes().list().stream().map(mapper::toEpisode).toList())
						.build());
	}
}
