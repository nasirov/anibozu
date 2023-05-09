package nasirov.yv.ab.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	private final GitHubResourcesServiceI<Mono<List<CommonTitle>>> gitHubResourcesService;

	private final CacheManager cacheManager;

	private final AppProps appProps;

	@Override
	public Mono<Map<FandubSource, Map<Integer, List<CommonTitle>>>> getCommonTitlesMappedByMalId() {
		Optional<Cache> cacheOpt = Optional.ofNullable(cacheManager.getCache(appProps.getCacheProps().getGithubCacheName()));
		String githubCacheKey = appProps.getCacheProps().getGithubCacheKey();
		return Mono.justOrEmpty(cacheOpt.map(x -> x.get(githubCacheKey, Map.class)).map(x -> (Map<FandubSource, Map<Integer, List<CommonTitle>>>) x))
				.switchIfEmpty(buildAndCacheResult(cacheOpt, githubCacheKey))
				.doOnSubscribe(x -> log.debug("Trying to get common titles mapped by mal id..."));
	}

	@Override
	public Mono<Map<Integer, Map<FandubSource, List<CommonTitle>>>> getCommonTitles(Set<FandubSource> fandubSources, List<MalTitle> malTitles) {
		return getCommonTitlesMappedByMalId().<Map<Integer, Map<FandubSource, List<CommonTitle>>>>map(commonTitlesMappedByMalId -> malTitles.stream()
						.collect(Collectors.toMap(MalTitle::getId, malTitle -> fandubSources.stream()
										.collect(Collectors.toMap(Function.identity(),
												fandubSource -> commonTitlesMappedByMalId.getOrDefault(fandubSource, Map.of()).getOrDefault(malTitle.getId(), List.of()))),
								(o, n) -> o, LinkedHashMap::new)))
				.doOnSubscribe(x -> log.debug("Trying to get common titles..."))
				.doOnSuccess(x -> log.debug("Got [{}] common titles.", x.size()));
	}

	private Mono<Map<FandubSource, Map<Integer, List<CommonTitle>>>> buildAndCacheResult(Optional<Cache> cacheOpt, String githubCacheKey) {
		return Flux.fromIterable(appProps.getEnabledFandubSources())
				.flatMap(x -> gitHubResourcesService.getResource(x).map(y -> Pair.of(x, groupByMalId(y))))
				.collectList()
				.map(x -> x.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)))
				.doOnSuccess(x -> cacheOpt.ifPresent(cache -> {
					cache.put(githubCacheKey, x);
					log.info("Cached common titles mapped by mal id.");
				}));
	}

	private Map<Integer, List<CommonTitle>> groupByMalId(List<CommonTitle> titles) {
		return titles.stream()
				.filter(x -> x.getType() != TitleType.NOT_FOUND)
				.flatMap(x -> splitByMalId(x).stream())
				.collect(Collectors.groupingBy(x -> x.getMalIds().get(0)));
	}

	private List<CommonTitle> splitByMalId(CommonTitle commonTitle) {
		List<Integer> malIds = commonTitle.getMalIds();
		Map<Integer, List<CommonEpisode>> malIdToEpisodes = commonTitle.getMalIdToEpisodes();
		List<CommonTitle> result;
		if (malIds.size() > 1) {
			result = malIds.stream().map(x -> new CommonTitle(commonTitle, List.of(x), Map.of(x, malIdToEpisodes.getOrDefault(x, List.of())))).toList();
		} else {
			result = List.of(commonTitle);
		}
		return result;
	}
}
