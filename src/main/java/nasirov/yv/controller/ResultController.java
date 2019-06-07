package nasirov.yv.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.exception.WatchingTitlesNotFoundException;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.serialization.MALUser;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.MALService;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class ResultController {

	private static final String ERROR_MSG = "errorMsg";

	private static final String RESULT_VIEW = "result";

	private static final String ERROR_VIEW = "error";

	private static final String NOT_SUPPORTED_ANIME_LIST_ERROR_MSG =
			"The application supports only default mal anime list view with wrapped json " + "data! Json anime " + "list is not found for ";

	private static final String ANIME_LIST_HAS_PRIVATE_ACCESS_ERROR_MSG_PART_1 = "Anime list ";

	private static final String ANIME_LIST_HAS_PRIVATE_ACCESS_ERROR_MSG_PART_2 = " has private access!";

	private static final String MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_1 = "MAL account ";

	private static final String MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_2 = " is not found";

	private static final String MODEL_ATTRIBUTE_USERNAME = "username";

	private static final String MODEL_ATTRIBUTE_NEW_EPISODE_AVAILABLE = "newEpisodeAvailable";

	private static final String MODEL_ATTRIBUTE_NEW_EPISODE_NOT_AVAILABLE = "newEpisodeNotAvailable";

	private static final String MODEL_ATTRIBUTE_NOT_FOUND_ON_ANIMEDIA = "matchedNotFoundAnimeOnAnimedia";

	@Value("${cache.userMAL.name}")
	private String userMALCacheName;

	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;

	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;

	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;

	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;

	private MALService malService;

	private AnimediaService animediaService;

	private ReferencesManager referencesManager;

	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;

	private CacheManager cacheManager;

	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@Autowired
	public ResultController(MALService malService, AnimediaService animediaService, ReferencesManager referencesManager,
			SeasonAndEpisodeChecker seasonAndEpisodeChecker, CacheManager cacheManager,
			NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository) {
		this.malService = malService;
		this.animediaService = animediaService;
		this.referencesManager = referencesManager;
		this.seasonAndEpisodeChecker = seasonAndEpisodeChecker;
		this.cacheManager = cacheManager;
		this.notFoundAnimeOnAnimediaRepository = notFoundAnimeOnAnimediaRepository;
	}

	@PostMapping(value = "/result")
	public String checkResult(@Valid MALUser malUser, Model model) {
		String username = malUser.getUsername();
		model.addAttribute(MODEL_ATTRIBUTE_USERNAME, username);
		Set<UserMALTitleInfo> watchingTitles;
		String errorMsg;
		try {
			watchingTitles = malService.getWatchingTitles(username);
		} catch (MALUserAccountNotFoundException e) {
			errorMsg = MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_1 + username + MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_2;
			return handleError(errorMsg, model, e);
		} catch (WatchingTitlesNotFoundException e) {
			errorMsg = e.getMessage();
			return handleError(errorMsg, model, e);
		} catch (MALUserAnimeListAccessException e) {
			errorMsg = ANIME_LIST_HAS_PRIVATE_ACCESS_ERROR_MSG_PART_1 + username + ANIME_LIST_HAS_PRIVATE_ACCESS_ERROR_MSG_PART_2;
			return handleError(errorMsg, model, e);
		} catch (JSONNotFoundException e) {
			errorMsg = NOT_SUPPORTED_ANIME_LIST_ERROR_MSG + username;
			return handleError(errorMsg, model, e);
		}
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		Cache matchedReferencesCache = cacheManager.getCache(matchedReferencesCacheName);
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(username, LinkedHashSet.class);
		Set<AnimediaMALTitleReferences> matchedAnimeFromCache = userMatchedAnimeCache.get(username, LinkedHashSet.class);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache = currentlyUpdatedTitlesCache
				.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		if (watchingTitlesFromCache != null && matchedAnimeFromCache != null && currentlyUpdatedTitlesFromCache != null) {
			return handleCachedUser(currentlyUpdatedTitlesFromCache, matchedAnimeFromCache, watchingTitlesFromCache, watchingTitles, model, username);
		}
		return handleNewUser(matchedReferencesCache, username, watchingTitles, matchedAnimeFromCache, model);
	}

	private String handleCachedUser(List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<UserMALTitleInfo> watchingTitlesFromCache, Set<UserMALTitleInfo> watchingTitles,
			Model model, String username) {
		List<AnimediaMALTitleReferences> differences = animediaService
				.checkCurrentlyUpdatedTitles(animediaService.getCurrentlyUpdatedTitles(), currentlyUpdatedTitlesFromCache);
		updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(differences, matchedAnimeFromCache, watchingTitlesFromCache);
		Set<UserMALTitleInfo> watchingTitlesWithUpdatedNumberOfWatchedEpisodes = malService
				.getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(watchingTitles, watchingTitlesFromCache);
		if (!watchingTitlesWithUpdatedNumberOfWatchedEpisodes.isEmpty()) {
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesWithUpdatedNumberOfWatchedEpisodes,
					matchedAnimeFromCache,
					animediaService.getAnimediaSearchList(),
					username);
		}
		boolean isWatchingTitlesUpdated = malService.isWatchingTitlesUpdated(watchingTitles, watchingTitlesFromCache);
		if (isWatchingTitlesUpdated) {
			updateWatchingTitlesAndMatchedReferences(matchedAnimeFromCache, watchingTitlesFromCache, username);
		}
		return enrichModel(matchedAnimeFromCache, watchingTitlesFromCache, model);
	}

	private void updateCurrentMaxEpisodeNumberForWatchAndFinalUrl(List<AnimediaMALTitleReferences> differences,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		if (!differences.isEmpty()) {
			for (AnimediaMALTitleReferences currentlyUpdatedTitle : differences) {
				long count = matchedAnimeFromCache.stream()
						.filter(set -> set.getUrl().equals(currentlyUpdatedTitle.getUrl()) && set.getDataList().equals(currentlyUpdatedTitle.getDataList())
								&& set.getMinConcretizedEpisodeOnMAL() == null && set.getMaxConcretizedEpisodeOnMAL() == null).count();
				if (count != 0) {
					referencesManager.updateCurrentMax(matchedAnimeFromCache, currentlyUpdatedTitle);
					seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFromCache, currentlyUpdatedTitle, matchedAnimeFromCache);
				}
			}
		}
	}

	private void updateWatchingTitlesAndMatchedReferences(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			Set<UserMALTitleInfo> watchingTitlesFromCache, String username) {
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaSearchListCache.get(animediaSearchListCacheName, LinkedHashSet.class);
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

	private String handleNewUser(Cache matchedReferencesCache, String username, Set<UserMALTitleInfo> watchingTitles,
			Set<AnimediaMALTitleReferences> matchedAnimeFromCache, Model model) {
		animediaService.getCurrentlyUpdatedTitles();
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaSearchListCache.get(animediaSearchListCacheName, LinkedHashSet.class);
		Set<AnimediaMALTitleReferences> matchedReferences;
		matchedReferences = referencesManager.getMatchedReferences(referencesManager.getMultiSeasonsReferences(), watchingTitles);
		referencesManager.updateReferences(matchedReferences);
		matchedReferencesCache.put(username, matchedReferences);
		Set<AnimediaMALTitleReferences> matchedAnime = matchedAnimeFromCache != null ? matchedAnimeFromCache
				: seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, animediaSearchList, username);
		return enrichModel(matchedAnime, watchingTitles, model);
	}

	private String enrichModel(Set<AnimediaMALTitleReferences> matchedAnime, Set<UserMALTitleInfo> watchingTitles, Model model) {
		List<AnimediaMALTitleReferences> newEpisodeAvailable = matchedAnime.stream().filter(set -> !set.getFinalUrl().equals(""))
				.collect(Collectors.toList());
		List<AnimediaMALTitleReferences> newEpisodeNotAvailable = matchedAnime.stream().filter(set -> set.getFinalUrl().equals(""))
				.collect(Collectors.toList());
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
		model.addAttribute(ERROR_MSG, errorMsg);
		return ERROR_VIEW;
	}
}
