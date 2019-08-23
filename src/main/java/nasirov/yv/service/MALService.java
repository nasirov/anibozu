package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.mal.MALCategories;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.MALParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class MALService {

	private static final String LOAD_JSON = "/load.json";

	private static final String SEARCH_PREFIX_JSON = "search/prefix.json";

	private static final String PROFILE = "profile/";

	private static final String ANIME_LIST = "animelist/";

	private static final String STATUS_PARAMETER = "status";

	private static final String OFFSET_PARAMETER = "offset";

	private static final String MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_1 = "MAL account ";

	private static final String MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_2 = " is not found";

	private static final Pattern POSTER_URL_RESOLUTION_PATTERN = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");

	private static final Pattern S_VARIABLE_PATTERN = Pattern.compile("(\\?s=.+)");

	/**
	 * Lazy initialization limit of titles in json in html page
	 */
	private static final Integer MAX_OFFSET_FOR_LOAD_JSON = 300;

	private static final Integer INITIAL_OFFSET_FOR_LOAD_JSON = 0;

	private static final Map<String, String> QUERY_PARAMS_FOR_TITLE_NAME_CHECK = new LinkedHashMap<>(2);

	private static final Map<String, String> QUERY_PARAMS_FOR_LOAD_JSON = new LinkedHashMap<>(2);

	static {
		QUERY_PARAMS_FOR_LOAD_JSON.put(OFFSET_PARAMETER, INITIAL_OFFSET_FOR_LOAD_JSON.toString());
		QUERY_PARAMS_FOR_LOAD_JSON.put(STATUS_PARAMETER, WATCHING.getCode().toString());
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("type", "all");
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("v", "1");
	}

	private String myAnimeListNet;

	private Map<String, Map<String, String>> malRequestParameters;

	private Cache userMALCache;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private MALParser malParser;

	private CacheManager cacheManager;

	private UrlsNames urlsNames;

	@Autowired
	public MALService(HttpCaller httpCaller, @Qualifier(value = "malRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			MALParser malParser, CacheManager cacheManager, UrlsNames urlsNames) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.malParser = malParser;
		this.cacheManager = cacheManager;
		this.urlsNames = urlsNames;
	}

	@PostConstruct
	public void init() {
		malRequestParameters = requestParametersBuilder.build();
		userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		myAnimeListNet = urlsNames.getMalUrls().getMyAnimeListNet();
	}

	/**
	 * Searches for user watching titles
	 *
	 * @param username the MAL username
	 * @return user watching titles
	 * @throws WatchingTitlesNotFoundException if number of watching titles is not found or == 0
	 * @throws MALUserAccountNotFoundException if username doesn't exist
	 */
	public Set<UserMALTitleInfo> getWatchingTitles(String username) throws WatchingTitlesNotFoundException, MALUserAccountNotFoundException {
		HttpResponse malResponseWithUserProfile = httpCaller.call(myAnimeListNet + PROFILE + username, HttpMethod.GET, malRequestParameters);
		if (!isUserExist(malResponseWithUserProfile)) {
			throw new MALUserAccountNotFoundException(MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_1 + username + MAL_ACCOUNT_IS_NOT_FOUND_ERROR_MSG_PART_2);
		}
		Integer numberOfUserWatchingTitles = malParser.getNumWatchingTitles(malResponseWithUserProfile);
		if (numberOfUserWatchingTitles == null) {
			throw new WatchingTitlesNotFoundException("Watching titles number is not found for " + username + " !");
		}
		if (numberOfUserWatchingTitles.equals(0)) {
			throw new WatchingTitlesNotFoundException("Not found watching titles for " + username + " !");
		}
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
		userMALCache.putIfAbsent(username, resultWatchingTitles);
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
	public boolean isWatchingTitlesUpdated(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache) {
		boolean isWatchingTitlesUpdated = false;
		for (UserMALTitleInfo userMALTitleInfoNew : watchingTitlesNew) {
			Integer numWatchedEpisodesNew = userMALTitleInfoNew.getNumWatchedEpisodes();
			UserMALTitleInfo userMALTitleInfoFromCache = watchingTitlesFromCache.stream()
					.filter(set -> set.getTitle().equalsIgnoreCase(userMALTitleInfoNew.getTitle())).findFirst().orElse(null);
			if (userMALTitleInfoFromCache == null) {
				isWatchingTitlesUpdated = true;
				watchingTitlesFromCache.add(userMALTitleInfoNew);
			} else if (!userMALTitleInfoFromCache.getNumWatchedEpisodes().equals(numWatchedEpisodesNew)) {
				isWatchingTitlesUpdated = true;
				userMALTitleInfoFromCache.setNumWatchedEpisodes(numWatchedEpisodesNew);
			}
		}
		Iterator<UserMALTitleInfo> iterator = watchingTitlesFromCache.iterator();
		while (iterator.hasNext()) {
			UserMALTitleInfo userMALTitleInfoFromCache = iterator.next();
			UserMALTitleInfo userMALTitleInfoNew = watchingTitlesNew.stream()
					.filter(set -> set.getTitle().equalsIgnoreCase(userMALTitleInfoFromCache.getTitle())).findFirst().orElse(null);
			if (userMALTitleInfoNew == null) {
				isWatchingTitlesUpdated = true;
				iterator.remove();
			}
		}
		return isWatchingTitlesUpdated;
	}

	/**
	 * Compares number of watched episodes fresh and cached watching titles
	 *
	 * @param watchingTitlesNew       fresh mal user title info
	 * @param watchingTitlesFromCache cached mal user title info
	 * @return collection with watching titles which number of watched episodes was updated
	 */
	public Set<UserMALTitleInfo> getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Set<UserMALTitleInfo> watchingTitlesNew,
			Set<UserMALTitleInfo> watchingTitlesFromCache) {
		Set<UserMALTitleInfo> result = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfoNew : watchingTitlesNew) {
			Integer numWatchedEpisodesNew = userMALTitleInfoNew.getNumWatchedEpisodes();
			UserMALTitleInfo userMALTitleInfoFromCache = watchingTitlesFromCache.stream()
					.filter(set -> set.getTitle().equalsIgnoreCase(userMALTitleInfoNew.getTitle())).findFirst().orElse(null);
			if (userMALTitleInfoFromCache != null && !userMALTitleInfoFromCache.getNumWatchedEpisodes().equals(numWatchedEpisodesNew)) {
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
	public boolean isTitleExist(String titleOnMAL) {
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("keyword", titleOnMAL);
		HttpResponse malResponse = httpCaller
				.call(URLBuilder.build(myAnimeListNet + SEARCH_PREFIX_JSON, QUERY_PARAMS_FOR_TITLE_NAME_CHECK), HttpMethod.GET, malRequestParameters);
		if (malResponse.getStatus() == HttpStatus.OK.value()) {
			MALSearchResult malSearchResult = WrappedObjectMapper.unmarshal(malResponse.getContent(), MALSearchResult.class);
			return 1 == malSearchResult.getCategories().stream().filter(categories -> categories.getType().equals(MALCategories.ANIME.getDescription()))
					.flatMap(categories -> categories.getItems().stream()).filter(title -> title.getName().equalsIgnoreCase(titleOnMAL) && title.getType().equals(MALCategories.ANIME.getDescription())).count();
		}
		return false;
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
		title.setAnimeUrl(myAnimeListNet + title.getAnimeUrl().substring(1));
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
	private List<UserMALTitleInfo> getJsonTitlesAndUnmarshal(Integer currentOffset, String username) {
		QUERY_PARAMS_FOR_LOAD_JSON.put(OFFSET_PARAMETER, currentOffset.toString());
		HttpResponse response = httpCaller
				.call(URLBuilder.build(myAnimeListNet + ANIME_LIST + username + LOAD_JSON, QUERY_PARAMS_FOR_LOAD_JSON), HttpMethod.GET,
						malRequestParameters);
		return WrappedObjectMapper.unmarshal(response.getContent(), UserMALTitleInfo.class, ArrayList.class);
	}

	/**
	 * Checks username for existence
	 *
	 * @param malProfileResponse the MAL response
	 * @return true if MAL profile response status != 404, else false
	 */
	private boolean isUserExist(HttpResponse malProfileResponse) {
		return !malProfileResponse.getStatus().equals(HttpStatus.NOT_FOUND.value());
	}
}
