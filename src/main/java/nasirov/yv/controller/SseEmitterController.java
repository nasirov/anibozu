package nasirov.yv.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SseEmitterController {

	private final SseEmitterExecutorServiceI sseService;

	@GetMapping("/sse")
	public ResponseEntity<SseEmitter> getSseEmitter(@Valid UserInputDto userInputDto) {
		String username = userInputDto.getUsername();
		log.info("Received a request for Server-Sent Events processing by [{}]...", username);
		SseEmitter sseEmitter = sseService.buildAndExecuteSseEmitter(userInputDto);
		log.info("Got SseEmitter. End of a request for [{}].", username);
		return ResponseEntity.ok(sseEmitter);
	}
}
