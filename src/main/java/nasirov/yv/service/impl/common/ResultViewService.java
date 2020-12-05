package nasirov.yv.service.impl.common;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ResultViewServiceI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultViewService implements ResultViewServiceI {

	private final MalServiceI malService;

	@Override
	public Mono<String> getResultView(UserInputDto userInputDto, Model model) {
		return malService.getUserWatchingTitles(userInputDto)
				.map(x -> determineResultView(userInputDto, model, x))
				.doOnSuccess(x -> log.info("Got result view [{}] for [{}].", x, userInputDto.getUsername()));
	}

	private String determineResultView(UserInputDto userInputDto, Model model, MalServiceResponseDto malServiceResponseDto) {
		String resultView;
		String errorMessage = malServiceResponseDto.getErrorMessage();
		if (StringUtils.isBlank(errorMessage)) {
			resultView = handleSuccess(malServiceResponseDto, userInputDto, model);
		} else {
			resultView = handleError(errorMessage, model);
		}
		return resultView;
	}

	private String handleSuccess(MalServiceResponseDto malServiceResponseDto, UserInputDto userInputDto, Model model) {
		model.addAttribute("username", malServiceResponseDto.getUsername());
		model.addAttribute("watchingTitlesSize",
				malServiceResponseDto.getMalTitles()
						.size());
		model.addAttribute("fandubList", buildFanDubList(userInputDto.getFanDubSources()));
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
