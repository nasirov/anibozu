package nasirov.yv.ac.service;

import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalAccessRestorerI {

	Mono<Boolean> restoreMalAccess();
}
