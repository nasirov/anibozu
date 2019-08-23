package nasirov.yv.controller;

import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedOnMAL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.MALService;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
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

	private final MALService malService;

	private final AnimediaService animediaService;

	private final ReferencesManager referencesManager;

	private final SeasonAndEpisodeChecker seasonAndEpisodeChecker;

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
			return handleCachedUser(watchingTitlesFromCache, matchedAnimeFromCache, currentlyUpdatedTitlesFromCache, watchingTitles, model, username);
		}
		return handleNewUser(username, watchingTitles, model);
	}

	private String handleCachedUser(Set<UserMALTitleInfo> watchingTitlesFromCache, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache, Set<UserMALTitleInfo> watchingTitles, Model model, String username) {
		List<AnimediaMALTitleReferences> differences = animediaService
				.checkCurrentlyUpdatedTitles(animediaService.getCurrentlyUpdatedTitles(), currentlyUpdatedTitlesFromCache);
		updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(differences, matchedAnimeFromCache, watchingTitlesFromCache);
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
			updateWatchingTitlesAndMatchedReferences(matchedAnimeFromCache, watchingTitlesFromCache, username);
		}
		return enrichModel(matchedAnimeFromCache, watchingTitlesFromCache, model);
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
		List<AnimediaMALTitleReferences> newEpisodeAvailable = matchedAnime.stream()
				.filter(set -> !set.getFinalUrl().equals(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)).collect(Collectors.toList());
		List<AnimediaMALTitleReferences> newEpisodeNotAvailable = matchedAnime.stream()
				.filter(set -> set.getFinalUrl().equals(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)).collect(Collectors.toList());
		Set<UserMALTitleInfo> notFoundAnimeOnAnimedia = new LinkedHashSet<>(notFoundAnimeOnAnimediaRepository.findAll());
		Set<UserMALTitleInfo> matchedNotFoundAnimeOnAnimedia = new LinkedHashSet<>();
		watchingTitles.forEach(title -> notFoundAnimeOnAnimedia.stream().filter(set -> set.getTitle().equals(title.getTitle()))
				.forEach(matchedNotFoundAnimeOnAnimedia::add));
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

	private void updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(List<AnimediaMALTitleReferences> differences,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		if (!differences.isEmpty()) {
			for (AnimediaMALTitleReferences currentlyUpdatedTitle : differences) {
				long count = matchedAnimeFromCache.stream()
						.filter(title -> title.getUrl().equals(currentlyUpdatedTitle.getUrl()) && title.getDataList().equals(currentlyUpdatedTitle.getDataList())
								&& !isTitleConcretizedOnMAL(title)).count();
				if (count != 0) {
					referencesManager.updateCurrentMax(matchedAnimeFromCache, currentlyUpdatedTitle);
					seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFromCache, currentlyUpdatedTitle, matchedAnimeFromCache);
				}
			}
		}
	}

	private void updateWatchingTitlesAndMatchedReferences(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache, String username) {
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		Iterator<AnimediaMALTitleReferences> iterator = matchedAnimeFromCache.iterator();
		while (iterator.hasNext()) {
			AnimediaMALTitleReferences animediaMALTitleReferences = iterator.next();
			UserMALTitleInfo removedFromAnimeList = watchingTitlesFromCache.stream()
					.filter(set -> set.getTitle().equals(animediaMALTitleReferences.getTitleOnMAL())).findFirst().orElse(null);
			if (removedFromAnimeList == null) {
				iterator.remove();
			}
		}
		for (UserMALTitleInfo watchingTitle : watchingTitlesFromCache) {
			AnimediaMALTitleReferences animediaMALTitleReferences = matchedAnimeFromCache.stream()
					.filter(set -> set.getTitleOnMAL().equals(watchingTitle.getTitle())).findAny().orElse(null);
			if (animediaMALTitleReferences == null && !notFoundAnimeOnAnimediaRepository.exitsByTitle(watchingTitle.getTitle())) {
				Set<UserMALTitleInfo> tempWatchingTitles = new LinkedHashSet<>();
				tempWatchingTitles.add(watchingTitle);
				Set<AnimediaMALTitleReferences> newMatchedAnime = seasonAndEpisodeChecker
						.getMatchedAnime(tempWatchingTitles, referencesManager.getMultiSeasonsReferences(), animediaSearchList, username);
				matchedAnimeFromCache.addAll(newMatchedAnime);
			}
		}
	}

	private boolean isUserCached(Set<UserMALTitleInfo> watchingTitlesFromCache, Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache) {
		return watchingTitlesFromCache != null && matchedAnimeFromCache != null && currentlyUpdatedTitlesFromCache != null;
	}
}
