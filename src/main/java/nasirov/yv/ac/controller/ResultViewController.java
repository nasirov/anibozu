package nasirov.yv.ac.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ac.dto.fe.InputDto;
import nasirov.yv.ac.dto.fe.ResultDto;
import nasirov.yv.ac.service.ResultProcessingServiceI;
import org.apache.commons.lang3.StringUtils;
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

	private final ResultProcessingServiceI titlesService;

	@GetMapping("/result")
	public Mono<String> getResultView(@Valid InputDto inputDto, Model model) {
		return Mono.just(inputDto)
				.flatMap(titlesService::getResult)
				.map(x -> determineView(inputDto, model, x))
				.doOnSuccess(x -> log.info("Got view [{}] for [{}].", x, inputDto.getUsername()));
	}

	private String determineView(InputDto inputDto, Model model, ResultDto resultDto) {
		String viewName;
		String errorMessage = resultDto.getErrorMessage();
		if (StringUtils.isBlank(errorMessage)) {
			viewName = handleSuccess(inputDto, model, resultDto);
		} else {
			viewName = handleError(errorMessage, model);
		}
		return viewName;
	}

	private String handleSuccess(InputDto inputDto, Model model, ResultDto resultDto) {
		model.addAttribute("username", inputDto.getUsername());
		model.addAttribute("fandubMapJson", resultDto.getFandubMapJson());
		model.addAttribute("titlesJson", resultDto.getTitlesJson());
		return "result";
	}

	private String handleError(String errorMsg, Model model) {
		log.error(errorMsg);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}
}
