package nasirov.yv.service;

import nasirov.yv.data.mal.MalUser;
import nasirov.yv.data.task.ServerSentEventThread;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
public interface ServerSentEventThreadServiceI {

	ServerSentEventThread buildServerSentEventThread(SseEmitter sseEmitter, MalUser malUser);
}