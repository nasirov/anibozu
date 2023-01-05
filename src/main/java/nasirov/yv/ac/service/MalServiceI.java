package nasirov.yv.ac.service;

import nasirov.yv.ac.dto.mal.MalUserInfo;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<MalUserInfo> getMalUserInfo(String username, MalTitleWatchingStatus status);
}
