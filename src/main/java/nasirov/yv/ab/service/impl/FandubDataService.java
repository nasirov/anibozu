package nasirov.yv.ab.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.internal.FandubData;
import nasirov.yv.ab.dto.internal.FandubKey;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.properties.CacheProps;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CompiledAnimeResource;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisode;
import nasirov.yv.starter.common.service.CompiledAnimeResourcesServiceI;
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
public class FandubDataService implements FandubDataServiceI {

	private final CompiledAnimeResourcesServiceI<Mono<CompiledAnimeResource>> compiledAnimeResourcesService;

	private final CacheManager cacheManager;

	private final AppProps appProps;

	public Mono<FandubData> getFandubData() {
		CacheProps cacheProps = appProps.getCacheProps();
		String githubCacheName = cacheProps.getGithubCacheName();
		Optional<Cache> cacheOpt = Optional.ofNullable(cacheManager.getCache(githubCacheName));
		String githubCacheKey = cacheProps.getGithubCacheKey();
		Optional<FandubData> cachedFandubData = cacheOpt.map(x -> x.get(githubCacheKey, FandubData.class));
		return Mono.justOrEmpty(cachedFandubData)
				.switchIfEmpty(buildAndCacheFandubData(cacheOpt, githubCacheKey))
				.doOnSubscribe(x -> log.debug("Trying to get fandub data..."));
	}

	private Mono<FandubData> buildAndCacheFandubData(Optional<Cache> cacheOpt, String githubCacheKey) {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(this::getCompiledAnimeResourcePairs)
				.collectList()
				.map(this::buildFandubData)
				.doOnSuccess(x -> cacheOpt.ifPresent(cache -> {
					cache.put(githubCacheKey, x);
					log.info("Cached.");
				}));
	}

	private Mono<List<Pair<FandubKey, Map<Integer, List<FandubEpisode>>>>> getCompiledAnimeResourcePairs(FandubSource fandubSource) {
		return compiledAnimeResourcesService.getCompiledAnimeResource(fandubSource).map(x -> buildPairs(fandubSource, x));
	}

	private List<Pair<FandubKey, Map<Integer, List<FandubEpisode>>>> buildPairs(FandubSource fandubSource,
			CompiledAnimeResource compiledAnimeResource) {
		return compiledAnimeResource.getResource().entrySet().stream().map(x -> Pair.of(new FandubKey(fandubSource, x.getKey()), x.getValue())).toList();
	}

	private FandubData buildFandubData(List<List<Pair<FandubKey, Map<Integer, List<FandubEpisode>>>>> lists) {
		return new FandubData(lists.stream().flatMap(List::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
	}
}
