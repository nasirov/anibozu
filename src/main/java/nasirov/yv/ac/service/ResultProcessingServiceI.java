package nasirov.yv.ac.service;

import nasirov.yv.ac.dto.fe.InputDto;
import nasirov.yv.ac.dto.fe.ResultDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface ResultProcessingServiceI {

	Mono<ResultDto> getResult(InputDto inputDto);
}
