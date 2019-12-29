package nasirov.yv.controller;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by nasirov.yv
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ResultController {

	private final MALServiceI malService;

	private final ReferencesServiceI referencesService;

	private final SeasonsAndEpisodesServiceI seasonAndEpisodeChecker;

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@PostMapping(value = "/result")
	public String checkResult(@Valid MALUser malUser, Model model) {
		String username = malUser.getUsername()
				.toLowerCase();
		model.addAttribute("username", username);
		Set<UserMALTitleInfo> watchingTitles;
		try {
			watchingTitles = malService.getWatchingTitles(username);
		} catch (MALUserAccountNotFoundException | WatchingTitlesNotFoundException | MALUserAnimeListAccessException e) {
			return handleError(e.getMessage(), model, e);
		}
		return handleNewUser(username, watchingTitles, model);
	}

	private String handleNewUser(String username, Set<UserMALTitleInfo> watchingTitles, Model model) {
		Set<TitleReference> matchedReferences = referencesService.getMatchedReferences(watchingTitles, referencesService.getReferences());
		referencesService.updateReferences(matchedReferences);
		Set<TitleReference> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, username);
		return enrichModel(matchedAnime, watchingTitles, model);
	}

	private String enrichModel(Set<TitleReference> matchedAnime, Set<UserMALTitleInfo> watchingTitles, Model model) {
		List<TitleReference> newEpisodeAvailable = new LinkedList<>();
		List<TitleReference> newEpisodeNotAvailable = new LinkedList<>();
		for (TitleReference anime : matchedAnime) {
			if (anime.getFinalUrlForFront()
					.equals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)) {
				newEpisodeNotAvailable.add(anime);
			} else {
				newEpisodeAvailable.add(anime);
			}
		}
		Set<UserMALTitleInfo> notFoundAnimeOnAnimedia = new LinkedHashSet<>(notFoundAnimeOnAnimediaRepository.findAll());
		Set<UserMALTitleInfo> matchedNotFoundAnimeOnAnimedia = watchingTitles.stream()
				.filter(x -> notFoundAnimeOnAnimedia.stream()
						.anyMatch(set -> set.getTitle()
								.equals(x.getTitle())))
				.collect(Collectors.toSet());
		model.addAttribute("newEpisodeAvailable", newEpisodeAvailable);
		model.addAttribute("newEpisodeNotAvailable", newEpisodeNotAvailable);
		model.addAttribute("matchedNotFoundAnimeOnAnimedia", matchedNotFoundAnimeOnAnimedia);
		return "result";
	}

	private String handleError(String errorMsg, Model model, Exception exception) {
		log.error(errorMsg, exception);
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}
}
