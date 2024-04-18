package nasirov.yv.ab.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fandub_data.FandubDataId;
import nasirov.yv.ab.mapper.FandubDataMapper;
import nasirov.yv.ab.model.FandubDataKey;
import nasirov.yv.ab.model.FandubDataValue;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FandubDataService implements FandubDataServiceI {

	private static final Mono<FandubDataDto> FANDUB_DATA_FALLBACK = Mono.just(new FandubDataDto(List.of()));

	private final ReactiveRedisTemplate<FandubDataKey, FandubDataValue> redisTemplate;

	private final FandubDataMapper mapper;

	@Override
	public Mono<Boolean> createOrUpdateFandubData(FandubDataId fandubDataId, FandubDataDto fandubData) {
		return redisTemplate.opsForValue()
				.set(mapper.toFandubDataKey(fandubDataId), mapper.toFandubDataValue(fandubData))
				.doOnSuccess(x -> log.info("{} [{}:{}]", x ? "Set" : "Failed to set", fandubDataId.malId(), fandubDataId.episodeId()));
	}

	@Override
	public Mono<FandubDataDto> getFandubData(FandubDataId fandubDataId) {
		return redisTemplate.opsForValue()
				.get(mapper.toFandubDataKey(fandubDataId))
				.map(mapper::toFandubDataDto)
				.switchIfEmpty(FANDUB_DATA_FALLBACK)
				.doOnSubscribe(x -> log.debug("Getting [{}:{}]", fandubDataId.malId(), fandubDataId.episodeId()))
				.doOnSuccess(x -> log.debug("Got [{}] episodes for [{}:{}]", x.episodes().size(), fandubDataId.malId(), fandubDataId.episodeId()));
	}

	@Override
	public Mono<Boolean> deleteFandubData(FandubDataId fandubDataId) {
		return redisTemplate.opsForValue()
				.delete(mapper.toFandubDataKey(fandubDataId))
				.doOnSuccess(x -> log.info("{} [{}:{}]", x ? "Deleted" : "Failed to delete", fandubDataId.malId(), fandubDataId.episodeId()));
	}
}
