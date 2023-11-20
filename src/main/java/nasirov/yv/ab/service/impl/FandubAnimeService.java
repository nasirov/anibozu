package nasirov.yv.ab.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.internal.GithubCacheKey;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.FandubAnimeServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.FandubAnime;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisode;
import nasirov.yv.starter.common.dto.fandub.common.IgnoredAnime;
import nasirov.yv.starter.common.dto.fandub.common.MappingType;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.starter.common.service.GitHubResourcesServiceI;
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
public class FandubAnimeService implements FandubAnimeServiceI {

	private final GitHubResourcesServiceI<Mono<List<FandubAnime>>, Mono<List<IgnoredAnime>>> gitHubResourcesService;

	private final CacheManager cacheManager;

	private final AppProps appProps;

	private final StarterCommonProperties starterCommonProperties;

	public Mono<Map<GithubCacheKey, List<FandubEpisode>>> getEpisodesMappedByKey() {
		Optional<Cache> cacheOpt = Optional.ofNullable(cacheManager.getCache(appProps.getCacheProps().getGithubCacheName()));
		String githubCacheKey = appProps.getCacheProps().getGithubCacheKey();
		return Mono.justOrEmpty(cacheOpt.map(x -> x.get(githubCacheKey, Map.class)).map(x -> (Map<GithubCacheKey, List<FandubEpisode>>) x))
				.switchIfEmpty(buildAndCacheResult(cacheOpt, githubCacheKey))
				.doOnSubscribe(x -> log.debug("Trying to get fandub anime..."));
	}

	private Mono<Map<GithubCacheKey, List<FandubEpisode>>> buildAndCacheResult(Optional<Cache> cacheOpt, String githubCacheKey) {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(fandubSource -> gitHubResourcesService.getFandubAnime(fandubSource).map(anime -> groupEpisodesByKey(fandubSource, anime)))
				.collectList()
				.map(x -> x.stream().map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
				.doOnSuccess(x -> cacheOpt.ifPresent(cache -> {
					cache.put(githubCacheKey, x);
					log.info("Cached.");
				}));
	}

	private Map<GithubCacheKey, List<FandubEpisode>> groupEpisodesByKey(FandubSource fandubSource, List<FandubAnime> animeList) {
		return animeList.stream()
				.filter(x -> x.getMappingType() != MappingType.NOT_FOUND)
				.flatMap(x -> x.getMalIdToEpisodes().entrySet().stream().filter(e -> e.getKey() > 0))
				.peek(x -> x.getValue()
						.forEach(episode -> episode.setPath(starterCommonProperties.getFandub().getUrls().get(fandubSource) + episode.getPath())))
				.collect(Collectors.groupingBy(x -> new GithubCacheKey(fandubSource, x.getKey()),
						Collectors.flatMapping(x -> x.getValue().stream(), Collectors.toList())));
	}
}
