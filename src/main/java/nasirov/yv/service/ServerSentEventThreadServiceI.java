package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.task.ServerSentEventThread;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Nasirov Yuriy
 */
public interface ServerSentEventThreadServiceI {

	ServerSentEventThread buildServerSentEventThread(SseEmitter sseEmitter, UserInputDto userInputDto);
}
