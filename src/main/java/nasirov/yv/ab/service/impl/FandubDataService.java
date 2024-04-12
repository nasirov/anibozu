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
		Optional<Cache> cacheOpt = Optional.ofNullable(cacheManager.getCache(cacheProps.getGithubCacheName()));
		String githubCacheKey = cacheProps.getGithubCacheKey();
		Optional<FandubData> cachedFandubData = cacheOpt.map(x -> x.get(githubCacheKey, FandubData.class));
		return Mono.justOrEmpty(cachedFandubData)
				.switchIfEmpty(buildFandubData().doOnSuccess(x -> cacheFandubData(cacheOpt, githubCacheKey, x)))
				.doOnSubscribe(x -> log.debug("Trying to get fandub data..."));
	}

	private Mono<FandubData> buildFandubData() {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(this::getCompiledAnimeResourcePairs)
				.collectList()
				.map(x -> new FandubData(x.stream().flatMap(List::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue))));
	}

	private Mono<List<Pair<FandubKey, Map<Integer, List<FandubEpisode>>>>> getCompiledAnimeResourcePairs(FandubSource fandubSource) {
		return compiledAnimeResourcesService.getCompiledAnimeResource(fandubSource).map(x -> buildPairs(fandubSource, x));
	}

	private List<Pair<FandubKey, Map<Integer, List<FandubEpisode>>>> buildPairs(FandubSource fandubSource,
			CompiledAnimeResource compiledAnimeResource) {
		return compiledAnimeResource.getResource().entrySet().stream().map(x -> Pair.of(new FandubKey(fandubSource, x.getKey()), x.getValue())).toList();
	}

	private void cacheFandubData(Optional<Cache> cacheOpt, String githubCacheKey, FandubData fandubData) {
		cacheOpt.ifPresent(cache -> {
			cache.put(githubCacheKey, fandubData);
			log.info("Cached.");
		});
	}
}
