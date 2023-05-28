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
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonEpisode;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.fandub.common.IgnoredTitle;
import nasirov.yv.starter.common.dto.fandub.common.TitleType;
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
public class CommonTitlesService implements CommonTitlesServiceI {

	private final GitHubResourcesServiceI<Mono<List<CommonTitle>>, Mono<List<IgnoredTitle>>> gitHubResourcesService;

	private final CacheManager cacheManager;

	private final AppProps appProps;

	public Mono<Map<GithubCacheKey, List<CommonEpisode>>> getCommonEpisodesMappedByKey() {
		Optional<Cache> cacheOpt = Optional.ofNullable(cacheManager.getCache(appProps.getCacheProps().getGithubCacheName()));
		String githubCacheKey = appProps.getCacheProps().getGithubCacheKey();
		return Mono.justOrEmpty(cacheOpt.map(x -> x.get(githubCacheKey, Map.class)).map(x -> (Map<GithubCacheKey, List<CommonEpisode>>) x))
				.switchIfEmpty(buildAndCacheResult(cacheOpt, githubCacheKey))
				.doOnSubscribe(x -> log.debug("Trying to get common titles..."));
	}

	private Mono<Map<GithubCacheKey, List<CommonEpisode>>> buildAndCacheResult(Optional<Cache> cacheOpt, String githubCacheKey) {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(fandubSource -> gitHubResourcesService.getCommonTitles(fandubSource).map(titles -> groupCommonEpisodesByKey(fandubSource, titles)))
				.collectList()
				.map(x -> x.stream().map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
				.doOnSuccess(x -> cacheOpt.ifPresent(cache -> {
					cache.put(githubCacheKey, x);
					log.info("Cached.");
				}));
	}

	private Map<GithubCacheKey, List<CommonEpisode>> groupCommonEpisodesByKey(FandubSource fandubSource, List<CommonTitle> titles) {
		return titles.stream()
				.filter(x -> x.getType() != TitleType.NOT_FOUND)
				.flatMap(x -> x.getMalIdToEpisodes().entrySet().stream().filter(e -> e.getKey() > 0))
				.collect(Collectors.groupingBy(x -> new GithubCacheKey(fandubSource, x.getKey()),
						Collectors.flatMapping(x -> x.getValue().stream(), Collectors.toList())));
	}
}
