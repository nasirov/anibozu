package nasirov.yv.controller;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.MalServiceI;
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

	private final MalServiceI malService;

	@GetMapping(value = "/result")
	public String getResultView(@Valid UserInputDto userInputDto, Model model) {
		String username = userInputDto.getUsername();
		log.info("Received a request for result view by [{}]...", username);
		String resultView;
		MalUserInfo malUserInfo = malService.getMalUserInfo(username);
		String errorMessage = malUserInfo.getErrorMessage();
		if (Objects.isNull(errorMessage)) {
			resultView = handleSuccess(malUserInfo, userInputDto, model);
		} else {
			resultView = handleError(errorMessage, model);
		}
		log.info("Got result view [{}]. End of a request for [{}].", resultView, username);
		return resultView;
	}

	private String handleSuccess(MalUserInfo malUserInfo, UserInputDto userInputDto, Model model) {
		model.addAttribute("username", malUserInfo.getUsername());
		model.addAttribute("watchingTitlesSize",
				malUserInfo.getMalTitles()
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
