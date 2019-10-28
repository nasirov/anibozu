package nasirov.yv.controller;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.MALUser;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by nasirov.yv
 */
@Controller
@Slf4j
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ResultController {

	private static final String RESULT_VIEW = "result";

	private static final String ERROR_VIEW = "error";

	private static final String MODEL_ATTRIBUTE_ERROR_MSG = "errorMsg";

	private static final String MODEL_ATTRIBUTE_USERNAME = "username";

	private static final String MODEL_ATTRIBUTE_NEW_EPISODE_AVAILABLE = "newEpisodeAvailable";

	private static final String MODEL_ATTRIBUTE_NEW_EPISODE_NOT_AVAILABLE = "newEpisodeNotAvailable";

	private static final String MODEL_ATTRIBUTE_NOT_FOUND_ON_ANIMEDIA = "matchedNotFoundAnimeOnAnimedia";

	private final MALServiceI malService;

	private final AnimediaServiceI animediaService;

	private final ReferencesServiceI referencesManager;

	private final SeasonsAndEpisodesServiceI seasonAndEpisodeChecker;

	private final CacheManager cacheManager;

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	private Cache userMALCache;

	private Cache userMatchedAnimeCache;

	private Cache currentlyUpdatedTitlesCache;

	@PostConstruct
	public void init() {
		userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		currentlyUpdatedTitlesCache = cacheManager.getCache(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE);
	}

	@PostMapping(value = "/result")
	public String checkResult(@Valid MALUser malUser, Model model) {
		String username = malUser.getUsername().toLowerCase();
		model.addAttribute(MODEL_ATTRIBUTE_USERNAME, username);
		Set<UserMALTitleInfo> watchingTitles;
		try {
			watchingTitles = malService.getWatchingTitles(username);
		} catch (MALUserAccountNotFoundException | WatchingTitlesNotFoundException e) {
			return handleError(e.getMessage(), model, e);
		}
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(username, LinkedHashSet.class);
		Set<AnimediaMALTitleReferences> matchedAnimeFromCache = userMatchedAnimeCache.get(username, LinkedHashSet.class);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache = currentlyUpdatedTitlesCache
				.get(CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
		if (isUserCached(watchingTitlesFromCache, matchedAnimeFromCache, currentlyUpdatedTitlesFromCache)) {
			possibleUpdateBasedOnCurrentlyUpdatedTitles(currentlyUpdatedTitlesFromCache, matchedAnimeFromCache, watchingTitlesFromCache);
			possibleUpdateBasedOnWatchingTitles(watchingTitles, matchedAnimeFromCache, watchingTitlesFromCache, username);
			return enrichModel(matchedAnimeFromCache, watchingTitlesFromCache, model);
		}
		return handleNewUser(username, watchingTitles, model);
	}

	private boolean isUserCached(Set<UserMALTitleInfo> watchingTitlesFromCache, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache) {
		return watchingTitlesFromCache != null && matchedAnimeFromCache != null && currentlyUpdatedTitlesFromCache != null;
	}

	private void possibleUpdateBasedOnCurrentlyUpdatedTitles(List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache) {
		List<AnimediaMALTitleReferences> differences = animediaService
				.checkCurrentlyUpdatedTitles(animediaService.getCurrentlyUpdatedTitles(), currentlyUpdatedTitlesFromCache);
		if (!differences.isEmpty()) {
			updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(differences, matchedAnimeFromCache, watchingTitlesFromCache);
		}
	}

	private void possibleUpdateBasedOnWatchingTitles(Set<UserMALTitleInfo> watchingTitles, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache, String username) {
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes = malService
				.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(watchingTitles, watchingTitlesFromCache);
		if (!watchingTitlesWithUpdatedNumberOfWatchedEpisodes.isEmpty()) {
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesWithUpdatedNumberOfWatchedEpisodes,
					matchedAnimeFromCache,
					animediaService.getAnimediaSearchListFromGitHub(),
					username);
		}
		boolean isWatchingTitlesUpdated = malService.isWatchingTitlesUpdated(watchingTitles, watchingTitlesFromCache);
		if (isWatchingTitlesUpdated) {
			pruneMatchedAnime(matchedAnimeFromCache, watchingTitlesFromCache);
			addNewMatchedAnime(matchedAnimeFromCache, watchingTitlesFromCache, username);
		}
	}

	private String handleNewUser(String username, Set<UserMALTitleInfo> watchingTitles, Model model) {
		Set<AnimediaMALTitleReferences> matchedAnimeFromCache = userMatchedAnimeCache.get(username, LinkedHashSet.class);
		animediaService.getCurrentlyUpdatedTitles();
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager
				.getMatchedReferences(referencesManager.getMultiSeasonsReferences(), watchingTitles);
		referencesManager.updateReferences(matchedReferences);
		Set<AnimediaMALTitleReferences> matchedAnime = matchedAnimeFromCache != null ? matchedAnimeFromCache
				: seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, animediaSearchList, username);
		return enrichModel(matchedAnime, watchingTitles, model);
	}

	private String enrichModel(Set<AnimediaMALTitleReferences> matchedAnime, Set<UserMALTitleInfo> watchingTitles, Model model) {
		List<AnimediaMALTitleReferences> newEpisodeAvailable = new LinkedList<>();
		List<AnimediaMALTitleReferences> newEpisodeNotAvailable = new LinkedList<>();
		for (AnimediaMALTitleReferences anime : matchedAnime) {
			if (anime.getFinalUrl().equals(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)) {
				newEpisodeNotAvailable.add(anime);
			} else {
				newEpisodeAvailable.add(anime);
			}
		}
		Set<UserMALTitleInfo> notFoundAnimeOnAnimedia = new LinkedHashSet<>(notFoundAnimeOnAnimediaRepository.findAll());
		Set<UserMALTitleInfo> matchedNotFoundAnimeOnAnimedia = watchingTitles.stream()
				.filter(x -> notFoundAnimeOnAnimedia
						.stream()
						.anyMatch(set -> set.getTitle().equals(x.getTitle())))
				.collect(Collectors.toSet());
		model.addAttribute(MODEL_ATTRIBUTE_NEW_EPISODE_AVAILABLE, newEpisodeAvailable);
		model.addAttribute(MODEL_ATTRIBUTE_NEW_EPISODE_NOT_AVAILABLE, newEpisodeNotAvailable);
		model.addAttribute(MODEL_ATTRIBUTE_NOT_FOUND_ON_ANIMEDIA, matchedNotFoundAnimeOnAnimedia);
		return RESULT_VIEW;
	}

	private String handleError(String errorMsg, Model model, Exception exception) {
		log.error(errorMsg, exception);
		model.addAttribute(MODEL_ATTRIBUTE_ERROR_MSG, errorMsg);
		return ERROR_VIEW;
	}

	//don't work with currently finished releases because dataList and currentMax are absent on currently added episodes page
	private void updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(List<AnimediaMALTitleReferences> differences,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		for (AnimediaMALTitleReferences currentlyUpdatedTitle : differences) {
			boolean isCachedMatchedAnimeNeedUpdate = matchedAnimeFromCache.stream()
					.anyMatch(title -> title.getUrl().equals(currentlyUpdatedTitle.getUrl())
							&& title.getDataList().equals(currentlyUpdatedTitle.getDataList())
							&& !isTitleConcretizedOnMAL(title));
			if (isCachedMatchedAnimeNeedUpdate) {
				referencesManager.updateCurrentMax(matchedAnimeFromCache, currentlyUpdatedTitle);
				seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFromCache, currentlyUpdatedTitle, matchedAnimeFromCache);
			}
		}
	}

	private void pruneMatchedAnime(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache) {
		matchedAnimeFromCache
				.removeIf(m -> watchingTitlesFromCache.stream()
				.noneMatch(w -> w.getTitle().equals(m.getTitleOnMAL())));
	}

	private void addNewMatchedAnime(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache, String username) {
		Set<AnimediaMALTitleReferences> newMatchedAnime = watchingTitlesFromCache.stream()
				.filter(x -> matchedAnimeFromCache
						.stream()
						.noneMatch(m -> m.getTitleOnMAL().equals(x.getTitle())) && !notFoundAnimeOnAnimediaRepository.exitsByTitle(x.getTitle()))
				.map(x -> getNewMatchedAnime(x, username))
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		matchedAnimeFromCache.addAll(newMatchedAnime);
	}

	private Set<AnimediaMALTitleReferences> getNewMatchedAnime(UserMALTitleInfo userMALTitleInfo, String username) {
		Set<UserMALTitleInfo> tempWatchingTitles = new LinkedHashSet<>();
		tempWatchingTitles.add(userMALTitleInfo);
		return seasonAndEpisodeChecker.getMatchedAnime(tempWatchingTitles,
				referencesManager.getMultiSeasonsReferences(),
				animediaService.getAnimediaSearchListFromGitHub(),
				username);
	}
}
