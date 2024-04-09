package nasirov.yv.ab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.service.CacheServiceI;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CacheController {

	private static final String CACHE_REFRESHED_MESSAGE = "github cache has been refreshed.";

	private final CacheServiceI cacheService;

	@PostMapping("/refresh/cache/github")
	public Mono<String> refreshCache() {
		log.info("Trying to refresh github cache...");
		return cacheService.evictGithubCache()
				.then(cacheService.fillGithubCache())
				.then(Mono.just(CACHE_REFRESHED_MESSAGE))
				.doOnSuccess(x -> log.info(CACHE_REFRESHED_MESSAGE));
	}
}
