package nasirov.yv.service.impl.common;

import lombok.RequiredArgsConstructor;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.service.MalServiceI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class MalService implements MalServiceI {

	private final HttpRequestServiceI httpRequestService;

	private final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Override
	public Mono<MalServiceResponseDto> getUserWatchingTitles(UserInputDto userInputDto) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.malService(userInputDto.getUsername(),
				MalTitleWatchingStatus.WATCHING));
	}
}
