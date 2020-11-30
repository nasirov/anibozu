package nasirov.yv.controller;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.feign.mal_service.MalServiceFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ResultViewController {

	private final MalServiceFeignClient malServiceFeignClient;

	private final AuthProps authProps;

	@GetMapping(value = "/result")
	public String getResultView(@Valid UserInputDto userInputDto, Model model) {
		String username = userInputDto.getUsername();
		log.info("Received a request for result view by [{}]...", username);
		String resultView;
		MalServiceResponseDto malServiceResponseDto = malServiceFeignClient.getUserTitles(authProps.getMalServiceBasicAuth(),
				userInputDto.getUsername(),
				MalTitleWatchingStatus.WATCHING);
		String errorMessage = malServiceResponseDto.getErrorMessage();
		if (StringUtils.isBlank(errorMessage)) {
			resultView = handleSuccess(malServiceResponseDto, userInputDto, model);
		} else {
			resultView = handleError(errorMessage, model);
		}
		log.info("Got result view [{}]. End of a request for [{}].", resultView, username);
		return resultView;
	}

	private String handleSuccess(MalServiceResponseDto malServiceResponseDto, UserInputDto userInputDto, Model model) {
		model.addAttribute("username", malServiceResponseDto.getUsername());
		model.addAttribute("watchingTitlesSize", malServiceResponseDto.getMalTitles()
						.size());
		model.addAttribute("fandubList", buildFanDubList(userInputDto.getFanDubSources()));
		return "result";
	}

	private String handleError(String errorMsg, Model model) {
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}

	private String buildFanDubList(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.map(FanDubSource::name)
				.collect(Collectors.joining(","));
	}
}
