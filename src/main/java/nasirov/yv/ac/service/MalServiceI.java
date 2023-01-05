package nasirov.yv.ac.service;

import nasirov.yv.ac.dto.mal.MalUserInfo;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<MalUserInfo> getMalUserInfo(String username);
}
