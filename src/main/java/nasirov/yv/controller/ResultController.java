package nasirov.yv.controller;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.exception.WatchingTitlesNotFoundException;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nasirov.yv
 */
@Controller
@Validated
@Slf4j
public class ResultController {
	private static final String ERROR_MSG = "errorMsg";
	
	private static final String RESULT = "result";
	
	@Value("${cache.userMAL.name}")
	private String userMALCacheName;
	
	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;
	
	@Value("${cache.matchedReferences.name}")
	private String matchedReferencesCacheName;
	
	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;
	
	private MALService malService;
	
	private AnimediaService animediaService;
	
	private ReferencesManager referencesManager;
	
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	private CacheManager cacheManager;
	
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;
	
	@Autowired
	public ResultController(MALService malService,
							AnimediaService animediaService,
							ReferencesManager referencesManager,
							SeasonAndEpisodeChecker seasonAndEpisodeChecker,
							CacheManager cacheManager,
							NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository) {
		this.malService = malService;
		this.animediaService = animediaService;
		this.referencesManager = referencesManager;
		this.seasonAndEpisodeChecker = seasonAndEpisodeChecker;
		this.cacheManager = cacheManager;
		this.notFoundAnimeOnAnimediaRepository = notFoundAnimeOnAnimediaRepository;
	}
	
	@PostMapping("/result")
	public String checkResult(@Size(min = 2, max = 16, message = "MAL username must be between 2 and 16 characters")
							  @RequestParam(value = "username") String username, Model model) {
		model.addAttribute("username", username);
		Set<UserMALTitleInfo> watchingTitles;
		String errorMsg;
		try {
			watchingTitles = malService.getWatchingTitles(username);
		} catch (MALUserAccountNotFoundException e) {
			errorMsg = "MAL account " + username + " is not found";
			return handleError(errorMsg, model, e);
		} catch (WatchingTitlesNotFoundException e) {
			errorMsg = e.getMessage();
			return handleError(errorMsg, model, e);
		} catch (MALUserAnimeListAccessException e) {
			errorMsg = "Anime list " + username + " has private access!";
			return handleError(errorMsg, model, e);
		} catch (JSONNotFoundException e) {
			errorMsg = "The application supports only default mal anime list view with wrapped json data! Json anime list is not found for " + username;
			return handleError(errorMsg, model, e);
		}
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		Cache matchedReferencesCache = cacheManager.getCache(matchedReferencesCacheName);
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		Set<UserMALTitleInfo> watchingTitlesFromCache = userMALCache.get(username, LinkedHashSet.class);
		Set<AnimediaMALTitleReferences> matchedAnimeFromCache = userMatchedAnimeCache.get(username, LinkedHashSet.class);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache = currentlyUpdatedTitlesCache.get(currentlyUpdatedTitlesCacheName, ArrayList.class);
		if (watchingTitlesFromCache != null && matchedAnimeFromCache != null && currentlyUpdatedTitlesFromCache != null) {
			List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
			return handleCachedUser(currentlyUpdatedTitles, currentlyUpdatedTitlesFromCache, matchedAnimeFromCache, watchingTitlesFromCache, watchingTitles, model);
		}
		return handleNewUser(matchedReferencesCache, username, watchingTitles, matchedAnimeFromCache, model);
	}
	
	private String handleCachedUser(List<AnimediaMALTitleReferences> currentlyUpdatedTitles,
									List<AnimediaMALTitleReferences> currentlyUpdatedTitlesFromCache,
									Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
									Set<UserMALTitleInfo> watchingTitlesFromCache,
									Set<UserMALTitleInfo> watchingTitles,
									Model model) {
		List<AnimediaMALTitleReferences> differences = animediaService.checkCurrentlyUpdatedTitles(currentlyUpdatedTitles, currentlyUpdatedTitlesFromCache);
		if (!differences.isEmpty()) {
			for (AnimediaMALTitleReferences currentlyUpdatedTitle : differences) {
				long count = matchedAnimeFromCache.stream()
						.filter(set -> set.getUrl().equals(currentlyUpdatedTitle.getUrl())
								&& set.getDataList().equals(currentlyUpdatedTitle.getDataList()))
						.count();
				if (count != 0) {
					referencesManager.updateCurrentMax(matchedAnimeFromCache, currentlyUpdatedTitle);
					seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFromCache, currentlyUpdatedTitle, matchedAnimeFromCache);
				}
			}
		}
		boolean isWatchingTitlesUpdated = malService.isWatchingTitlesUpdated(watchingTitles, watchingTitlesFromCache);
		return enrichModel(matchedAnimeFromCache, watchingTitlesFromCache, model, isWatchingTitlesUpdated);
	}
	
	private String handleNewUser(Cache matchedReferencesCache,
								 String username,
								 Set<UserMALTitleInfo> watchingTitles,
								 Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
								 Model model) {
		animediaService.getCurrentlyUpdatedTitles();
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		Set<AnimediaMALTitleReferences> matchedReferencesFromCache = matchedReferencesCache.get(username, LinkedHashSet.class);
		Set<AnimediaMALTitleReferences> matchedReferences;
		if (matchedReferencesFromCache == null) {
			matchedReferences = referencesManager.getMatchedReferences(allReferences, watchingTitles);
			long start = System.nanoTime();
			referencesManager.updateReferences(matchedReferences);
			long end = System.nanoTime();
			log.info("Elapsed time for update references {}", end - start);
			matchedReferencesCache.put(username, matchedReferences);
		} else {
			matchedReferences = matchedReferencesFromCache;
		}
		Set<AnimediaMALTitleReferences> matchedAnime = matchedAnimeFromCache != null ? matchedAnimeFromCache : seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, animediaSearchList, username);
		return enrichModel(matchedAnime, watchingTitles, model, false);
	}
	
	private String enrichModel(Set<AnimediaMALTitleReferences> matchedAnime, Set<UserMALTitleInfo> watchingTitles, Model model, boolean isWatchingTitlesUpdated) {
		if (isWatchingTitlesUpdated) {
			Iterator<AnimediaMALTitleReferences> iterator = matchedAnime.iterator();
			while (iterator.hasNext()) {
				AnimediaMALTitleReferences animediaMALTitleReferences = iterator.next();
				UserMALTitleInfo userMALTitleInfo = watchingTitles.stream().filter(set -> set.getTitle().equals(animediaMALTitleReferences.getTitleOnMAL())).findFirst().orElse(null);
				if (userMALTitleInfo == null) {
					iterator.remove();
				}
			}
		}
		List<AnimediaMALTitleReferences> newEpisodeAvailable = matchedAnime.stream().filter(set -> !set.getFinalUrl().equals("")).collect(Collectors.toList());
		List<AnimediaMALTitleReferences> newEpisodeNotAvailable = matchedAnime.stream().filter(set -> set.getFinalUrl().equals("")).collect(Collectors.toList());
		Set<UserMALTitleInfo> notFoundAnimeOnAnimedia = new LinkedHashSet<>(notFoundAnimeOnAnimediaRepository.findAll());
		Set<UserMALTitleInfo> matchedNotFoundAnimeOnAnimedia = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			notFoundAnimeOnAnimedia.stream().filter(set -> set.getTitle().equals(userMALTitleInfo.getTitle())).forEach(matchedNotFoundAnimeOnAnimedia::add);
		}
		model.addAttribute("newEpisodeAvailable", newEpisodeAvailable);
		model.addAttribute("newEpisodeNotAvailable", newEpisodeNotAvailable);
		model.addAttribute("matchedNotFoundAnimeOnAnimedia", matchedNotFoundAnimeOnAnimedia);
		return RESULT;
	}
	
	private String handleError(String errorMsg, Model model, Exception exception) {
		log.error(errorMsg, exception);
		model.addAttribute(ERROR_MSG, errorMsg);
		return RESULT;
	}
}
