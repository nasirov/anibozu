package nasirov.yv.service;

import nasirov.yv.data.mal.MalUser;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
public interface SseEmitterExecutorServiceI {

	SseEmitter buildAndExecuteSseEmitter(MalUser malUser);
}
