package nasirov.yv.ab.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.service.CacheServiceI;
import nasirov.yv.ab.service.FandubAnimeServiceI;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService implements CacheServiceI {

	private final FandubAnimeServiceI fandubAnimeService;

	@Override
	@CacheEvict(value = "github", allEntries = true)
	public Mono<Void> evictGithubCache() {
		return Mono.empty().then().doOnSuccess(x -> log.info("github cache has been evicted."));
	}

	@Override
	@EventListener(classes = ApplicationReadyEvent.class, condition = "@appProps.getCacheProps().isCacheOnStartup()")
	public Mono<Void> fillGithubCache() {
		log.info("Trying to fill github cache...");
		return fandubAnimeService.getEpisodesMappedByKey().then().doOnSuccess(x -> log.info("github cache has been filled."));
	}
}
