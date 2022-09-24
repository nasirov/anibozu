package nasirov.yv.ac.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ac.data.front.InputDto;
import nasirov.yv.ac.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.ac.service.MalServiceI;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MalService implements MalServiceI {

	private final HttpRequestServiceI httpRequestService;

	private final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Override
	public Mono<MalServiceResponseDto> getUserWatchingTitles(InputDto inputDto) {
		String username = inputDto.getUsername();
		return httpRequestService.performHttpRequest(
						httpRequestServiceDtoBuilder.malService(username, MalTitleWatchingStatus.WATCHING))
				.doOnSubscribe(x -> log.debug("Trying to get watching titles for [{}]...", username))
				.doOnSuccess(x -> log.info("Got [{}] watching titles for [{}].", x.getMalTitles().size(), username));
	}
}
