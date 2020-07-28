package nasirov.yv.service;

import nasirov.yv.data.mal.MalUser;
import nasirov.yv.data.task.SseAction;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
public interface SseActionServiceI {

	SseAction buildSseAction(SseEmitter sseEmitter, MalUser malUser);
}
