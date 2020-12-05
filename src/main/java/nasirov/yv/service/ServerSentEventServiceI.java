package nasirov.yv.service;

import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.UserInputDto;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
public interface ServerSentEventServiceI {

	Flux<ServerSentEvent<SseDto>> getServerSentEvents(UserInputDto userInputDto);
}
