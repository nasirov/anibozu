package nasirov.yv.service;

import nasirov.yv.data.front.InputDto;
import nasirov.yv.data.front.ResultDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface ResultProcessingServiceI {

	Mono<ResultDto> getResult(InputDto inputDto);
}
