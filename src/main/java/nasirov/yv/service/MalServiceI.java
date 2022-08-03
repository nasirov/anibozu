package nasirov.yv.service;

import nasirov.yv.data.front.InputDto;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<MalServiceResponseDto> getUserWatchingTitles(InputDto inputDto);
}
