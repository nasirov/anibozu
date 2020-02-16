package nasirov.yv.controller;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.FunDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.DeferredResultProps;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.MALServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Created by nasirov.yv
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ResultController {

	private static final ForkJoinPool COMMON_POOL = ForkJoinPool.commonPool();

	private final MALServiceI malService;

	private final AnimeServiceI animeService;

	private final DeferredResultProps deferredResultProps;

	@PostMapping(value = "/result")
	public DeferredResult<String> checkResult(@Valid MALUser malUser, Model model) {
		malUser.setUsername(malUser.getUsername()
				.toLowerCase());
		log.info("RECEIVED {}", malUser.getUsername());
		DeferredResult<String> result = new DeferredResult<>(MINUTES.toMillis(deferredResultProps.getTimeoutInMin()));
		COMMON_POOL.submit(processResult(malUser, model, result));
		return result;
	}

	private Runnable processResult(MALUser malUser, Model model, DeferredResult<String> result) {
		return () -> {
			String username = malUser.getUsername();
			log.info("PROCESSING {}", username);
			model.addAttribute("username", username);
			Set<UserMALTitleInfo> watchingTitles;
			try {
				watchingTitles = malService.getWatchingTitles(username);
			} catch (MALUserAccountNotFoundException | WatchingTitlesNotFoundException | MALUserAnimeListAccessException e) {
				result.setErrorResult(handleError(e.getMessage(), model));
				return;
			}
			result.setResult(handleUser(malUser, watchingTitles, model));
		};
	}

	private String handleUser(MALUser malUser, Set<UserMALTitleInfo> watchingTitles, Model model) {
		log.info("HANDLE USER {}", malUser.getUsername());
		Set<Anime> anime = animeService.getAnime(malUser.getFunDubSources(), watchingTitles);
		model.addAttribute("resultAnimeList", anime);
		model.addAttribute("fundubList", buildFunDubList(malUser.getFunDubSources()));
		return "result";
	}

	private String handleError(String errorMsg, Model model) {
		log.error(errorMsg);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}

	private List<String> buildFunDubList(Set<FunDubSource> funDubSources) {
		return funDubSources.stream()
				.map(FunDubSource::getName)
				.collect(Collectors.toList());
	}
}
