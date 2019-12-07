package nasirov.yv.service.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALCategories;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.http.feign.MALFeignClient;
import nasirov.yv.parser.MALParser;
import nasirov.yv.service.MALServiceI;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MALService implements MALServiceI {

	private static final Pattern POSTER_URL_RESOLUTION_PATTERN = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");

	private static final Pattern S_VARIABLE_PATTERN = Pattern.compile("(\\?s=.+)");

	/**
	 * Lazy initialization limit of titles in json in html page
	 */
	private static final Integer MAX_OFFSET_FOR_LOAD_JSON = 300;

	private static final Integer INITIAL_OFFSET_FOR_LOAD_JSON = 0;

	private final MALFeignClient malFeignClient;

	private final MALParser malParser;

	private final UrlsNames urlsNames;

	private String myAnimeListNet;

	@PostConstruct
	public void init() {
		myAnimeListNet = urlsNames.getMalUrls()
				.getMyAnimeListNet();
	}

	/**
	 * Searches for user watching titles
	 *
	 * @param username the MAL username
	 * @return user watching titles
	 * @throws WatchingTitlesNotFoundException if number of watching titles is not found or == 0
	 * @throws MALUserAccountNotFoundException if username doesn't exist
	 */
	@Override
	@CachePut(value = "userMALCache", key = "#username", condition = "#root.caches[0].get(#username) == null")
	public Set<UserMALTitleInfo> getWatchingTitles(String username)
			throws WatchingTitlesNotFoundException, MALUserAccountNotFoundException, MALUserAnimeListAccessException {
		ResponseEntity<String> malResponseWithUserProfile = malFeignClient.getUserProfile(username);
		String userProfile = malResponseWithUserProfile.getBody();
		validateUserAccountExistence(malResponseWithUserProfile, username);
		Integer numberOfUserWatchingTitles = malParser.getNumWatchingTitles(userProfile);
		validateNumberOfUserWatchingTitles(numberOfUserWatchingTitles, username);
		Set<UserMALTitleInfo> resultWatchingTitles = new LinkedHashSet<>(numberOfUserWatchingTitles);
		int performedRequestCount = 0;
		//performedRequestCount == 0 -> first request for titles 1 - 300 https://myanimelist.net/animelist/username/load.json?offset=0&status=1
		//performedRequestCount == 1 -> second request for titles 301 - 600 https://myanimelist.net/animelist/username/load.json?offset=300&status=1
		//etc
		do {
			int actualOffset;
			int offsetStep;
			if (performedRequestCount == 0) {
				actualOffset = INITIAL_OFFSET_FOR_LOAD_JSON;
				offsetStep = INITIAL_OFFSET_FOR_LOAD_JSON;
			} else {
				actualOffset = MAX_OFFSET_FOR_LOAD_JSON * performedRequestCount;
				offsetStep = MAX_OFFSET_FOR_LOAD_JSON;
			}
			List<UserMALTitleInfo> tempWatchingTitles = getJsonTitlesAndUnmarshal(actualOffset, username);
			tempWatchingTitles.forEach(title -> {
				changePosterUrl(title);
				changeAnimeUrl(title);
				changeTitleName(title);
				resultWatchingTitles.add(title);
			});
			numberOfUserWatchingTitles -= offsetStep;
			performedRequestCount++;
		} while (numberOfUserWatchingTitles > MAX_OFFSET_FOR_LOAD_JSON);
		return resultWatchingTitles;
	}

	/**
	 * Compares cached and fresh user watching titles and: update num of watched episodes add new title in cache if it doesn't exist in
	 * watchingTitlesFromCache remove title from cache if it doesn't exist in watchingTitlesNew
	 *
	 * @param watchingTitlesNew       fresh watching titles
	 * @param watchingTitlesFromCache cached watching titles
	 * @return true if user anime list updated
	 */
	@Override
	public boolean isWatchingTitlesUpdated(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		boolean isSomeTitleHasUpdatedNumOfWatchedEpisodes = updateNumOfWatchedEpisodesForCachedTitles(watchingTitlesNew, watchingTitlesFromCache);
		boolean isNewWatchingTitlesPresents = addNewWatchingTitlesToCached(watchingTitlesNew, watchingTitlesFromCache);
		boolean isSomeCachedTitleRemoved = pruneCachedWatchingTitles(watchingTitlesNew, watchingTitlesFromCache);
		return isSomeTitleHasUpdatedNumOfWatchedEpisodes || isNewWatchingTitlesPresents || isSomeCachedTitleRemoved;
	}

	/**
	 * Compares number of watched episodes fresh and cached watching titles
	 *
	 * @param watchingTitlesNew       fresh mal user title info
	 * @param watchingTitlesFromCache cached mal user title info
	 * @return collection with watching titles which number of watched episodes was updated
	 */
	@Override
	public Set<UserMALTitleInfo> getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Set<UserMALTitleInfo> watchingTitlesNew,
			Set<UserMALTitleInfo> watchingTitlesFromCache) {
		Set<UserMALTitleInfo> result = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfoNew : watchingTitlesNew) {
			Integer numWatchedEpisodesNew = userMALTitleInfoNew.getNumWatchedEpisodes();
			UserMALTitleInfo userMALTitleInfoFromCache = watchingTitlesFromCache.stream()
					.filter(set -> set.getTitle()
							.equalsIgnoreCase(userMALTitleInfoNew.getTitle()))
					.findFirst()
					.orElse(null);
			if (userMALTitleInfoFromCache != null && !userMALTitleInfoFromCache.getNumWatchedEpisodes()
					.equals(numWatchedEpisodesNew)) {
				result.add(userMALTitleInfoNew);
			}
		}
		return result;
	}

	/**
	 * Checks MAL title name for existence
	 *
	 * @param titleOnMAL the MAL title name
	 * @return true if MAL response contain one anime title with equals titleOnMAL, else false
	 */
	@Override
	public boolean isTitleExist(String titleOnMAL) {
		ResponseEntity<MALSearchResult> malResponse = malFeignClient.searchTitleByName(titleOnMAL);
		return malResponse.getStatusCode() == HttpStatus.OK && ofNullable(malResponse.getBody()).orElseGet(MALSearchResult::new)
				.getCategories()
				.stream()
				.filter(categories -> categories.getType()
						.equals(MALCategories.ANIME.getDescription()))
				.flatMap(categories -> categories.getItems()
						.stream())
				.filter(title -> title.getName()
						.equalsIgnoreCase(titleOnMAL) && title.getType()
						.equals(MALCategories.ANIME.getDescription()))
				.count() == 1;
	}

	/**
	 * Converts and sets poster URL from https://cdn.myanimelist.net/r/96x136/images/anime/7/86743.jpg?s=50f775b44d0a2317e9337a4eaaac6100 to
	 * https://cdn.myanimelist.net/images/anime/7/86743.jpg
	 * <p>
	 * because last url provides better quality image
	 *
	 * @param title the MAL title
	 */
	private void changePosterUrl(UserMALTitleInfo title) {
		String changedPosterUrl = "";
		Matcher matcher = POSTER_URL_RESOLUTION_PATTERN.matcher(title.getPosterUrl());
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll("");
		}
		matcher = S_VARIABLE_PATTERN.matcher(changedPosterUrl);
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll("");
		}
		title.setPosterUrl(changedPosterUrl);
	}

	/**
	 * Sets full anime url
	 *
	 * @param title the MAL title
	 */
	private void changeAnimeUrl(UserMALTitleInfo title) {
		title.setAnimeUrl(myAnimeListNet + title.getAnimeUrl()
				.substring(1));
	}

	/**
	 * Sets unescaped title name
	 *
	 * @param title the MAL title
	 */
	private void changeTitleName(UserMALTitleInfo title) {
		title.setTitle(HtmlUtils.htmlUnescape(title.getTitle()));
	}

	/**
	 * Searches for additional json anime list and unmarshal https://myanimelist.net/animelist/username/load .json?offset=currentOffset&status=1
	 *
	 * @param currentOffset the number of watching titles
	 * @param username      the MAL username
	 * @return the set with the user anime titles
	 */
	private List<UserMALTitleInfo> getJsonTitlesAndUnmarshal(Integer currentOffset, String username) throws MALUserAnimeListAccessException {
		ResponseEntity<List<UserMALTitleInfo>> malResponse = malFeignClient.getUserAnimeList(username, currentOffset, WATCHING.getCode());
		checkUserAnimeListAccess(malResponse, username);
		return ofNullable(malResponse.getBody()).orElseGet(Collections::emptyList);
	}

	private void checkUserAnimeListAccess(ResponseEntity<List<UserMALTitleInfo>> malResponse, String username) throws MALUserAnimeListAccessException {
		if (malResponse.getStatusCode()
				.equals(HttpStatus.BAD_REQUEST)) {
			throw new MALUserAnimeListAccessException("Anime list " + username + " has private access!");
		}
	}

	/**
	 * Checks a response status from MAL
	 *
	 * @param malResponseWithUserProfile a MAL response with an user profile
	 * @param username                   the MAL username
	 * @throws MALUserAccountNotFoundException if a response status == 404
	 */
	private void validateUserAccountExistence(ResponseEntity<String> malResponseWithUserProfile, String username)
			throws MALUserAccountNotFoundException {
		if (malResponseWithUserProfile.getStatusCode()
				.equals(HttpStatus.NOT_FOUND)) {
			throw new MALUserAccountNotFoundException("MAL account " + username + " is not found");
		}
	}

	/**
	 * Checks a number of user watching titles
	 *
	 * @param numberOfUserWatchingTitles a number of user watching titles
	 * @param username                   the MAL username
	 * @throws WatchingTitlesNotFoundException if the number == null or 0
	 */
	private void validateNumberOfUserWatchingTitles(Integer numberOfUserWatchingTitles, String username) throws WatchingTitlesNotFoundException {
		if (numberOfUserWatchingTitles == null) {
			throw new WatchingTitlesNotFoundException("Watching titles number is not found for " + username + " !");
		}
		if (numberOfUserWatchingTitles.equals(0)) {
			throw new WatchingTitlesNotFoundException("Not found watching titles for " + username + " !");
		}
	}

	private boolean updateNumOfWatchedEpisodesForCachedTitles(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		boolean isSomeTitleHasUpdatedNumOfWatchedEpisodes = false;
		for (UserMALTitleInfo userMALTitleInfoNew : watchingTitlesNew) {
			Integer numWatchedEpisodesNew = userMALTitleInfoNew.getNumWatchedEpisodes();
			UserMALTitleInfo userMALTitleInfoFromCache = watchingTitlesFromCache.stream()
					.filter(set -> set.getTitle()
							.equalsIgnoreCase(userMALTitleInfoNew.getTitle()))
					.findFirst()
					.orElse(null);
			if (userMALTitleInfoFromCache != null && !userMALTitleInfoFromCache.getNumWatchedEpisodes()
					.equals(numWatchedEpisodesNew)) {
				userMALTitleInfoFromCache.setNumWatchedEpisodes(numWatchedEpisodesNew);
				isSomeTitleHasUpdatedNumOfWatchedEpisodes = true;
			}
		}
		return isSomeTitleHasUpdatedNumOfWatchedEpisodes;
	}

	private boolean addNewWatchingTitlesToCached(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		Set<UserMALTitleInfo> newWatchingTitles = watchingTitlesNew.stream()
				.filter(x -> watchingTitlesFromCache.stream()
						.noneMatch(set -> set.getTitle()
								.equalsIgnoreCase(x.getTitle())))
				.collect(Collectors.toSet());
		return watchingTitlesFromCache.addAll(newWatchingTitles);
	}

	private boolean pruneCachedWatchingTitles(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		return watchingTitlesFromCache.removeIf(x -> watchingTitlesNew.stream()
				.noneMatch(set -> set.getTitle()
						.equalsIgnoreCase(x.getTitle())));
	}
}
