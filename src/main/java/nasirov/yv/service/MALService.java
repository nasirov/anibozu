package nasirov.yv.service;

import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALCategories;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.exception.mal.JSONNotFoundException;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.MALParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

	private static final String STATUS = "status";

	/**
	 * Lazy initialization limit of titles in json in html page
	 */
	private static final Integer MAX_NUMBER_OF_TITLES_IN_HTML = 300;

	private static final Map<String, String> QUERY_PARAMS_FOR_ANIME_LIST = new LinkedHashMap<>();

	private static final Map<String, String> QUERY_PARAMS_FOR_TITLE_NAME_CHECK = new LinkedHashMap<>();

	static {
		QUERY_PARAMS_FOR_ANIME_LIST.put(STATUS, WATCHING.getCode().toString());
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("type", "all");
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("v", "1");
	}

	@Value("${cache.userMAL.name}")
	private String userMALCacheName;

	@Value("${urls.myAnimeList.net}")
	private String myAnimeListNet;

	private Map<String, Map<String, String>> malRequestParameters;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private MALParser malParser;

	private CacheManager cacheManager;

	@Autowired
	public MALService(HttpCaller httpCaller, @Qualifier(value = "malRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			MALParser malParser, CacheManager cacheManager) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.malParser = malParser;
		this.cacheManager = cacheManager;
	}

	@PostConstruct
	public void init() {
		malRequestParameters = requestParametersBuilder.build();
	}
	/**
	 * Searches for user watching titles
	 *
	 * @param username the MAL username
	 * @return the watching titles
	 */
	public Set<UserMALTitleInfo> getWatchingTitles(@NotEmpty String username)
			throws MALUserAccountNotFoundException, WatchingTitlesNotFoundException, MALUserAnimeListAccessException, JSONNotFoundException {
		//get amount of user watching titles
		Integer numWatchingTitlesInteger = getNumberOfWatchingTitles(username, malRequestParameters);
		//get html with currently watching titles
		//html with currently watching titles provides lazy json initialization with limit 300 entity
		//if user watching titles > 300 then we have to do additional request(s)
		//first request = 300 entity
		//first request example animelist/username?status=1
		Set<Set<UserMALTitleInfo>> titleJson = new LinkedHashSet<>();
		String targetUrl = URLBuilder.build(myAnimeListNet + ANIME_LIST + username, QUERY_PARAMS_FOR_ANIME_LIST);
		titleJson.add(malParser.getUserTitlesInfo(httpCaller.call(targetUrl, HttpMethod.GET, malRequestParameters), LinkedHashSet.class));
		Integer diff;
		//check for missing titles
		if (numWatchingTitlesInteger > MAX_NUMBER_OF_TITLES_IN_HTML) {
			Set<UserMALTitleInfo> firstJson = getAllWatchingTitles(MAX_NUMBER_OF_TITLES_IN_HTML, username);
			titleJson.add(firstJson);
			diff = numWatchingTitlesInteger - MAX_NUMBER_OF_TITLES_IN_HTML;
			int nextRequestCount = 2;
			while (diff > MAX_NUMBER_OF_TITLES_IN_HTML) {
				Set<UserMALTitleInfo> additionalJson = getAllWatchingTitles((MAX_NUMBER_OF_TITLES_IN_HTML * nextRequestCount), username);
				titleJson.add(additionalJson);
				nextRequestCount++;
				diff -= MAX_NUMBER_OF_TITLES_IN_HTML;
			}
		}
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		for (Set<UserMALTitleInfo> set : titleJson) {
			changePosterUrl(set);
			changeAnimeUrl(set);
			unescapeHtmlCharactersInTitleName(set);
			watchingTitles.addAll(set);
		}
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		userMALCache.putIfAbsent(username, watchingTitles);
		return watchingTitles;
	}

	/**
	 * Compares cached and fresh user watching titles and:
	 * update num of watched episodes
	 * add new title in cache if it doesn't exist in watchingTitlesFromCache
	 * remove title from cache if it doesn't exist in watchingTitlesNew
	 *
	 * @param watchingTitlesNew fresh watching titles
	 * @param watchingTitlesFromCache cached watching titles
	 * @return true if user anime list updated
	 */
	public boolean isWatchingTitlesUpdated(@NotNull Set<UserMALTitleInfo> watchingTitlesNew, @NotNull Set<UserMALTitleInfo> watchingTitlesFromCache) {
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
	 * @param watchingTitlesNew fresh mal user title info
	 * @param watchingTitlesFromCache cached mal user title info
	 * @return collection with watching titles which number of watched episodes was updated
	 */
	public Set<UserMALTitleInfo> getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(@NotNull Set<UserMALTitleInfo> watchingTitlesNew,
			@NotNull Set<UserMALTitleInfo> watchingTitlesFromCache) {
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

	public boolean isTitleExist(String titleOnMAL) {
		QUERY_PARAMS_FOR_TITLE_NAME_CHECK.put("keyword", titleOnMAL);
		HttpResponse malResponse = httpCaller
				.call(URLBuilder.build(myAnimeListNet + SEARCH_PREFIX_JSON, QUERY_PARAMS_FOR_TITLE_NAME_CHECK), HttpMethod.GET, malRequestParameters);
		if (malResponse.getStatus() == HttpStatus.OK.value()) {
			MALSearchResult malSearchResult = WrappedObjectMapper.unmarshal(malResponse.getContent(), MALSearchResult.class);
			return 1 == malSearchResult.getCategories().stream().filter(categories -> categories.getType().equals(MALCategories.ANIME.getDescription()))
					.flatMap(categories -> categories.getItems().stream())
					.filter(title -> title.getName().equalsIgnoreCase(titleOnMAL) && title.getType().equals(MALCategories.ANIME.getDescription())).count();
		}
		return false;
	}

	/**
	 * Converts and sets poster URL from
	 * https://cdn.myanimelist.net/r/96x136/images/anime/7/86743.jpg?s=50f775b44d0a2317e9337a4eaaac6100
	 * to
	 * https://cdn.myanimelist.net/images/anime/7/86743.jpg
	 * <p>
	 * because last url provides better quality image
	 *
	 * @param watchingTitles the user mal anime list
	 */
	private void changePosterUrl(@NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		String changedPosterUrl = "";
		Pattern resolutionPattern = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");
		Pattern sVariablePattern = Pattern.compile("(\\?s=.+)");
		Matcher matcher;
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			matcher = resolutionPattern.matcher(userMALTitleInfo.getPosterUrl());
			if (matcher.find()) {
				changedPosterUrl = matcher.replaceAll("");
			}
			matcher = sVariablePattern.matcher(changedPosterUrl);
			if (matcher.find()) {
				changedPosterUrl = matcher.replaceAll("");
			}
			userMALTitleInfo.setPosterUrl(changedPosterUrl);
		}
	}

	/**
	 * Sets full anime url
	 *
	 * @param watchingTitles the user mal anime list
	 */
	private void changeAnimeUrl(@NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		watchingTitles.forEach(set -> set.setAnimeUrl(myAnimeListNet + set.getAnimeUrl().substring(1)));
	}

	private void unescapeHtmlCharactersInTitleName(@NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		watchingTitles.forEach(title -> title.setTitle(HtmlUtils.htmlUnescape(title.getTitle())));
	}

	/**
	 * Searches for number of watching titles
	 *
	 * @param username the  mal username
	 * @param malRequestParameters the http parameters
	 * @return the number of watching titles
	 * @throws MALUserAccountNotFoundException if user is not found
	 * @throws WatchingTitlesNotFoundException if number of watching titles is not found or == 0
	 */
	private Integer getNumberOfWatchingTitles(@NotEmpty String username, @NotNull Map<String, Map<String, String>> malRequestParameters)
			throws MALUserAccountNotFoundException, WatchingTitlesNotFoundException {
		String numWatchingTitles = malParser
				.getNumWatchingTitles(httpCaller.call(myAnimeListNet + PROFILE + username, HttpMethod.GET, malRequestParameters));
		Integer numWatchingTitlesInteger;
		if (numWatchingTitles != null) {
			numWatchingTitlesInteger = Integer.parseInt(numWatchingTitles);
			if (numWatchingTitlesInteger.equals(0)) {
				throw new WatchingTitlesNotFoundException("Not found watching titles for " + username + " !");
			}
		} else {
			throw new WatchingTitlesNotFoundException("Watching titles number is not found for " + username + " !");
		}
		return numWatchingTitlesInteger;
	}

	/**
	 * Searches for additional json anime list and unmarshal
	 * https://myanimelist.net/animelist/username/load.json?offset=numWatchingTitlesInteger&status=1
	 *
	 * @param numWatchingTitlesInteger the number of watching titles
	 * @param username the mal username
	 * @return the set with the user anime titles
	 */
	private Set<UserMALTitleInfo> getAllWatchingTitles(@NotEmpty Integer numWatchingTitlesInteger, @NotEmpty String username) {
		Map<String, String> queryParameters = new LinkedHashMap<>();
		queryParameters.put("offset", numWatchingTitlesInteger.toString());
		queryParameters.put(STATUS, WATCHING.getCode().toString());
		HttpResponse response = httpCaller
				.call(URLBuilder.build(myAnimeListNet + ANIME_LIST + username + LOAD_JSON, queryParameters), HttpMethod.GET, malRequestParameters);
		return WrappedObjectMapper.unmarshal(response.getContent(), UserMALTitleInfo.class, LinkedHashSet.class);
	}
}
