package nasirov.yv.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.InputDto;
import nasirov.yv.data.front.ResultDto;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.service.WrappedObjectMapperI;
import nasirov.yv.service.ResultProcessingServiceI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ResultViewController {

	private final ResultProcessingServiceI titlesService;

	private final WrappedObjectMapperI wrappedObjectMapper;

	@GetMapping("/result")
	public Mono<String> getResultView(@Valid InputDto inputDto, Model model) {
		return Mono.just(inputDto)
				.flatMap(titlesService::getResult)
				.map(x -> determineView(inputDto, model, x))
				.doOnSuccess(x -> log.info("Got result view [{}] for [{}].", x, inputDto.getUsername()));
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
		model.addAttribute("fandubList", buildFandubList(inputDto.getFandubSources()));
		model.addAttribute("availableTitles", toEscapedJson(resultDto.getAvailableTitles()));
		model.addAttribute("notAvailableTitles", toEscapedJson(resultDto.getNotAvailableTitles()));
		model.addAttribute("notFoundTitles", toEscapedJson(resultDto.getNotFoundTitles()));
		return "result";
	}

	private String buildFandubList(Set<FandubSource> fandubSources) {
		return fandubSources.stream().map(FandubSource::name).collect(Collectors.joining(","));
	}

	private String toEscapedJson(List<TitleDto> titles) {
		return HtmlUtils.htmlEscape(wrappedObjectMapper.toJson(titles), StandardCharsets.UTF_8.name());
	}

	private String handleError(String errorMsg, Model model) {
		log.error(errorMsg);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}
}
