package nasirov.yv.ab.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fandub_data.FandubDataId;
import nasirov.yv.ab.dto.user.AnimeList;
import nasirov.yv.ab.dto.user.AnimeList.Anime;
import nasirov.yv.ab.dto.user.AnimeList.Anime.AnimeBuilder;
import nasirov.yv.ab.dto.user.EpisodeInfo;
import nasirov.yv.ab.mapper.FandubDataMapper;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.UserServiceI;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServiceI {

	private final MalServiceI malService;

	private final FandubDataServiceI fandubDataService;

	private final FandubDataMapper mapper;

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
		return fandubDataService.getFandubData(FandubDataId.builder().malId(malAnime.getId()).episodeId(nextEpisode).build())
				.map(x -> result.episodes(buildEpisodes(x)).build());
	}

	private List<EpisodeInfo> buildEpisodes(FandubDataDto fandubData) {
		return fandubData.episodes().stream().map(mapper::toEpisodeInfo).toList();
	}
}
