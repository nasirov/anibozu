package nasirov.yv.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.ResultViewServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ResultViewController {

	private final ResultViewServiceI resultViewService;

	@GetMapping("/result")
	public Mono<String> getResultView(@Valid UserInputDto userInputDto, Model model) {
		log.info("Received a request for result view by [{}].", userInputDto.getUsername());
		return resultViewService.getResultView(userInputDto, model);
	}
}
