package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.mal.MalUserInfo;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<MalUserInfo> getMalUserInfo(String username);
}
