package nasirov.yv.ac.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ac.dto.cache.CacheEntity;
import nasirov.yv.ac.properties.AppProps;
import nasirov.yv.ac.service.CommonTitlesServiceI;
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
		Map<Integer, Integer> malIdToEpisode = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, MalTitle::getNextEpisodeForWatch, (o, n) -> o, LinkedHashMap::new));
		return getCommonTitlesMappedByMalId().flatMapMany(
						x -> Flux.fromStream(malIdToEpisode.entrySet().stream().map(y -> Pair.of(x, y))))
				.map(x -> buildMalIdToFandubSourcesCommonTitlesPair(fandubSources, x.getValue().getKey(), x.getValue().getValue(),
						x.getKey()))
				.collectMap(Pair::getKey, Pair::getValue, LinkedHashMap::new)
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
					.collect(Collectors.toList());
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

	private Pair<Integer, Map<FandubSource, List<CommonTitle>>> buildMalIdToFandubSourcesCommonTitlesPair(
			Set<FandubSource> fandubSources, Integer malId, Integer malEpisodeId,
			Map<FandubSource, Map<Integer, List<CommonTitle>>> cache) {
		return Pair.of(malId, fandubSources.stream()
				.map(x -> buildFandubSourceToCommonTitlesPair(x, malId, malEpisodeId, cache))
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
	}

	private Pair<FandubSource, List<CommonTitle>> buildFandubSourceToCommonTitlesPair(FandubSource fandubSource,
			Integer malId,
			Integer malEpisodeId, Map<FandubSource, Map<Integer, List<CommonTitle>>> cache) {
		return Pair.of(fandubSource, cache.getOrDefault(fandubSource, Map.of())
				.getOrDefault(malId, List.of())
				.stream()
				.map(x -> buildCommonTitleWithTargetEpisode(malEpisodeId, x, malId))
				.collect(Collectors.toList()));
	}

	private CommonTitle buildCommonTitleWithTargetEpisode(Integer malEpisodeId, CommonTitle commonTitle, Integer malId) {
		List<CommonEpisode> listWithTargetEpisode = commonTitle.getMalIdToEpisodes()
				.getOrDefault(malId, List.of())
				.stream()
				.filter(x -> x.getMalEpisodeId().equals(malEpisodeId))
				.findFirst()
				.map(List::of)
				.orElse(List.of());
		return new CommonTitle(commonTitle, Map.of(malId, listWithTargetEpisode));
	}
}
