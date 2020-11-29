package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Nasirov Yuriy
 */
public interface SseEmitterExecutorServiceI {

	SseEmitter buildAndExecuteSseEmitter(UserInputDto userInputDto);
}
