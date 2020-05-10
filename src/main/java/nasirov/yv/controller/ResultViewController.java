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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ResultViewController {

	private final MALServiceI malService;

	@PostMapping(value = "/result")
	public String getResultView(@Valid MALUser malUser, Model model) {
		String username = malUser.getUsername();
		log.info("Received a request for result view by [{}] ...", username);
		String resultView;
		try {
			List<UserMALTitleInfo> watchingTitles = malService.getWatchingTitles(username);
			resultView = handleSuccess(watchingTitles.size(), malUser, model);
		} catch (MalException malException) {
			resultView = handleError(malException.getMessage(), model);
		}
		log.info("Got result view [{}]. End of a request for [{}].", resultView, username);
		return resultView;
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
