package nasirov.yv.ab.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.cache.CacheEntity;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonEpisode;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.fandub.common.TitleType;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.service.GitHubResourcesServiceI;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonTitlesService implements CommonTitlesServiceI {

	private final GitHubResourcesServiceI<Mono<List<CommonTitle>>, Mono<Map<FandubSource, Integer>>> gitHubResourcesService;

	private final CacheManager cacheManager;

	private final AppProps appProps;

	@Override
	public Mono<Map<FandubSource, Map<Integer, List<CommonTitle>>>> getCommonTitlesMappedByMalId() {
		Cache cache = cacheManager.getCache(appProps.getCacheProps().getGithubCacheName());
		return Mono.justOrEmpty(cache)
				.flatMap(this::getResultFromCache)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.switchIfEmpty(buildResult(cache))
				.doOnSubscribe(x -> log.debug("Trying to get common titles mapped by mal id..."));
	}

	@Override
	public Mono<Map<Integer, Map<FandubSource, List<CommonTitle>>>> getCommonTitles(Set<FandubSource> fandubSources,
			List<MalTitle> malTitles) {
		return getCommonTitlesMappedByMalId().map(map -> malTitles.stream()
						.collect(Collectors.toMap(MalTitle::getId, malTitle -> fandubSources.stream()
								.collect(Collectors.toMap(Function.identity(),
										fandubSource -> map.getOrDefault(fandubSource, Map.of()).getOrDefault(malTitle.getId(), List.of()))))))
				.doOnSubscribe(x -> log.debug("Trying to get common titles..."))
				.doOnSuccess(x -> log.debug("Got [{}] common titles.", x.size()));
	}

	private Mono<Optional<Map<FandubSource, Map<Integer, List<CommonTitle>>>>> getResultFromCache(Cache cache) {
		return Mono.fromFuture(() -> CompletableFuture.supplyAsync(
								() -> Optional.ofNullable(cache.get(appProps.getCacheProps().getGithubCacheKey(), List.class))
										.map(x -> (List<CacheEntity>) x)
										.map(x -> x.stream()
												.collect(Collectors.toMap(CacheEntity::getFandubSource, CacheEntity::getMalIdToCommonTitles))))
						.orTimeout(10, TimeUnit.SECONDS))
				.doOnError(e -> log.error("Failed to lookup cache", e))
				.onErrorReturn(Optional.empty())
				.doOnSuccess(x -> x.ifPresent(r -> log.debug("Found cached common titles mapped by mal id.")));
	}

	private Mono<Map<FandubSource, Map<Integer, List<CommonTitle>>>> buildResult(Cache cache) {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(x -> gitHubResourcesService.getResource(x).map(y -> Pair.of(x, y)))
				.map(this::groupByMalId)
				.collectList()
				.map(x -> x.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)))
				.doOnSuccess(x -> cacheResult(x, cache));
	}

	private Pair<FandubSource, Map<Integer, List<CommonTitle>>> groupByMalId(
			Pair<FandubSource, List<CommonTitle>> fandubSourceToTitles) {
		Map<Integer, List<CommonTitle>> groupedByMalId = fandubSourceToTitles.getValue()
				.stream()
				.filter(x -> x.getType() != TitleType.NOT_FOUND)
				.map(this::splitCommonTitleByMalIds)
				.flatMap(List::stream)
				.collect(Collectors.groupingBy(x -> x.getMalIds().get(0)));
		return Pair.of(fandubSourceToTitles.getKey(), groupedByMalId);
	}

	private List<CommonTitle> splitCommonTitleByMalIds(CommonTitle commonTitle) {
		List<Integer> malIds = commonTitle.getMalIds();
		Map<Integer, List<CommonEpisode>> malIdToEpisodes = commonTitle.getMalIdToEpisodes();
		List<CommonTitle> result;
		if (malIds.size() > 1) {
			result = malIds.stream()
					.map(x -> new CommonTitle(commonTitle, List.of(x), Map.of(x, malIdToEpisodes.getOrDefault(x, List.of()))))
					.toList();
		} else {
			result = List.of(commonTitle);
		}
		return result;
	}

	private void cacheResult(Map<FandubSource, Map<Integer, List<CommonTitle>>> result, Cache cache) {
		List<CacheEntity> entities = result.entrySet()
				.stream()
				.map(x -> new CacheEntity(x.getKey(), x.getValue()))
				.collect(Collectors.toCollection(ArrayList::new));
		CompletableFuture.runAsync(() -> {
			cache.put(appProps.getCacheProps().getGithubCacheKey(), entities);
			log.info("Cached common titles mapped by mal id.");
		}).orTimeout(10, TimeUnit.SECONDS).exceptionally(x -> {
			log.error("Failed to cache common titles mapped by mal id.", x);
			return null;
		});
	}
}
