package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
public interface SseEmitterExecutorServiceI {

	SseEmitter buildAndExecuteSseEmitter(UserInputDto userInputDto);
}
