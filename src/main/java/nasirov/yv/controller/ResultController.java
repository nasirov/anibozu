package nasirov.yv.controller;

import static java.util.Objects.isNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
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

	private final ReferencesServiceI referencesService;

	private final SeasonsAndEpisodesServiceI seasonAndEpisodeChecker;

	@PostMapping(value = "/result")
	public DeferredResult<String> checkResult(@Valid MALUser malUser, Model model) {
		malUser.setUsername(malUser.getUsername().toLowerCase());
		log.info("RECEIVED {}", malUser.getUsername());
		DeferredResult<String> result = new DeferredResult<>(5 * 60 * 1000L);
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
			result.setResult(handleUser(username, watchingTitles, model));
		};
	}

	private String handleUser(String username, Set<UserMALTitleInfo> watchingTitles, Model model) {
		log.info("HANDLE USER {}", username);
		Set<TitleReference> matchedReferences = referencesService.getMatchedReferences(watchingTitles, referencesService.getReferences());
		referencesService.updateReferences(matchedReferences);
		Set<TitleReference> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, username);
		return enrichModel(matchedAnime, watchingTitles, model);
	}

	private String enrichModel(Set<TitleReference> matchedAnime, Set<UserMALTitleInfo> watchingTitles, Model model) {
		List<TitleReference> newEpisodeAvailable = new LinkedList<>();
		List<TitleReference> newEpisodeNotAvailable = new LinkedList<>();
		List<UserMALTitleInfo> notFoundAnimeOnAnimedia = new LinkedList<>();
		for (UserMALTitleInfo titleInfo : watchingTitles) {
			TitleReference matchedReference = getMatchedReference(titleInfo, matchedAnime);
			if (isNull(matchedReference)) {
				notFoundAnimeOnAnimedia.add(titleInfo);
			} else if (matchedReference.getFinalUrlForFront()
					.equals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)) {
				newEpisodeNotAvailable.add(matchedReference);
			} else {
				newEpisodeAvailable.add(matchedReference);
			}
		}
		model.addAttribute("newEpisodeAvailable", newEpisodeAvailable);
		model.addAttribute("newEpisodeNotAvailable", newEpisodeNotAvailable);
		model.addAttribute("notFoundAnimeOnAnimedia", notFoundAnimeOnAnimedia);
		return "result";
	}

	private TitleReference getMatchedReference(UserMALTitleInfo userMALTitleInfo, Set<TitleReference> matchedAnime) {
		return matchedAnime.stream()
				.filter(m -> userMALTitleInfo.getTitle()
						.equals(m.getTitleNameOnMAL()))
				.findFirst()
				.orElse(null);
	}

	private String handleError(String errorMsg, Model model) {
		log.error(errorMsg);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}
}
