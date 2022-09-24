package nasirov.yv.ac.service;

import nasirov.yv.ac.data.front.InputDto;
import nasirov.yv.ac.data.front.ResultDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface ResultProcessingServiceI {

	Mono<ResultDto> getResult(InputDto inputDto);
}
