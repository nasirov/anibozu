package nasirov.yv.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.ResultViewServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Controller
@RequiredArgsConstructor
public class ResultViewController {

	private final ResultViewServiceI resultViewService;

	@GetMapping("/result")
	public Mono<String> getResultView(@Valid UserInputDto userInputDto, Model model) {
		return resultViewService.getResultView(userInputDto, model);
	}
}
