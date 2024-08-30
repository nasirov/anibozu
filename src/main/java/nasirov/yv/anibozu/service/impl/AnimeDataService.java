package nasirov.yv.anibozu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.mapper.AnimeDataMapper;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import nasirov.yv.anibozu.service.AnimeDataServiceI;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeDataService implements AnimeDataServiceI {

	private static final Mono<AnimeData> ANIME_DATA_FALLBACK = Mono.just(new AnimeData(List.of()));

	private final ReactiveRedisTemplate<AnimeDataKey, AnimeDataValue> redisTemplate;

	private final AnimeDataMapper mapper;

	@Override
	public Mono<Boolean> saveAnimeData(AnimeDataId animeDataId, AnimeData animeData) {
		return redisTemplate.opsForValue()
				.set(mapper.toAnimeDataKey(animeDataId), mapper.toAnimeDataValue(animeData))
				.doOnSuccess(x -> log.info("{} [{}:{}]", x ? "Set" : "Failed to set", animeDataId.malId(), animeDataId.episodeId()));
	}

	@Override
	public Mono<AnimeData> getAnimeData(AnimeDataId animeDataId) {
		return redisTemplate.opsForValue()
				.get(mapper.toAnimeDataKey(animeDataId))
				.map(mapper::toAnimeData)
				.switchIfEmpty(ANIME_DATA_FALLBACK)
				.doOnSubscribe(x -> log.debug("Getting [{}:{}]", animeDataId.malId(), animeDataId.episodeId()))
				.doOnSuccess(x -> log.debug("Got [{}] episodes for [{}:{}]", x.episodes().size(), animeDataId.malId(), animeDataId.episodeId()));
	}

	@Override
	public Mono<Boolean> deleteAnimeData(AnimeDataId animeDataId) {
		return redisTemplate.opsForValue()
				.delete(mapper.toAnimeDataKey(animeDataId))
				.doOnSuccess(x -> log.info("{} [{}:{}]", x ? "Deleted" : "Failed to delete", animeDataId.malId(), animeDataId.episodeId()));
	}
}
