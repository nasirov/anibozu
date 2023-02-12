package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.fe.ProcessResult;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface ProcessServiceI {

	Mono<ProcessResult> process(String username);
}
