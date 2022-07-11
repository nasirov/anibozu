package nasirov.yv.service;

import nasirov.yv.data.front.InputDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<MalServiceResponseDto> getUserWatchingTitles(InputDto inputDto);
}
