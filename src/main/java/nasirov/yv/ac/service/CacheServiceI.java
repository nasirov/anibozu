package nasirov.yv.ac.service;

import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface CacheServiceI {

	Mono<Void> evictGithubCache();

	Mono<Void> fillGithubCache();
}
