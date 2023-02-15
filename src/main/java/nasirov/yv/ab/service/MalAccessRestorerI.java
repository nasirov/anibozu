package nasirov.yv.ab.service;

import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalAccessRestorerI {

	Mono<Boolean> restoreMalAccess();

	void restoreMalAccessAsync();
}
