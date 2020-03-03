package nasirov.yv.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MalException;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ResultController {

	private final SseEmitterExecutorServiceI sseService;

	private final MALServiceI malService;

	@PostMapping(value = "/result")
	public String getResultView(@Valid MALUser malUser, Model model) {
		String username = malUser.getUsername();
		log.debug("Received request for result view by [{}]", username);
		List<UserMALTitleInfo> watchingTitles;
		try {
			watchingTitles = malService.getWatchingTitles(username);
		} catch (MalException malException) {
			return handleError(malException.getMessage(), model);
		}
		return handleSuccess(watchingTitles.size(), malUser, model);
	}

	@GetMapping("/sse")
	@ResponseBody
	public ResponseEntity<SseEmitter> getSseEmitter(@Valid MALUser malUser) {
		log.debug("Received request for Server-Sent Events processing by [{}]", malUser.getUsername());
		return ResponseEntity.ok(sseService.buildAndExecuteSseEmitter(malUser));
	}

	private String handleSuccess(int watchingTitlesSize, MALUser malUser, Model model) {
		model.addAttribute("username", malUser.getUsername());
		model.addAttribute("watchingTitlesSize", watchingTitlesSize);
		model.addAttribute("fandubList", buildFanDubList(malUser.getFanDubSources()));
		return "result";
	}

	private String handleError(String errorMsg, Model model) {
		log.error(errorMsg);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}

	private String buildFanDubList(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.map(FanDubSource::name)
				.collect(Collectors.joining(","));
	}
}
