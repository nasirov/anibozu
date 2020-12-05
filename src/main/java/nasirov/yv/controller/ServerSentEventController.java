package nasirov.yv.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.ServerSentEventServiceI;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ServerSentEventController {

	private final ServerSentEventServiceI serverSentEventService;

	@GetMapping("/sse")
	public Flux<ServerSentEvent<SseDto>> getServerSentEvents(@Valid UserInputDto userInputDto) {
		log.info("Received a request for Server-Sent Events by [{}].", userInputDto.getUsername());
		return serverSentEventService.getServerSentEvents(userInputDto);
	}
}
