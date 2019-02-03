package nasirov.yv.service;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.exception.WatchingTitlesNotFoundException;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.MALParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.research.ws.wadl.HTTPMethods.GET;
import static nasirov.yv.enums.MALAnimeStatus.WATCHING;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class MALService {
	@Value("${cache.userMAL.name}")
	private String userMALCacheName;
	
	@Value("${urls.myAnimeList.net}")
	private String myAnimeListNet;
	
	private static final String LOAD_JSON = "load.json";
	
	private static final String PROFILE = "profile/";
	
	private static final String ANIME_LIST = "animelist/";
	
	private static final String STATUS = "status";
	
	/**
	 * Max number of rows on html page
	 */
	private static final Integer MAX_NUMBER_OF_TITLE_IN_HTML = 300;
	
	private HttpCaller httpCaller;
	
	private RequestParametersBuilder requestParametersBuilder;
	
	private WrappedObjectMapper wrappedObjectMapper;
	
	private MALParser malParser;
	
	private URLBuilder urlBuilder;
	
	private CacheManager cacheManager;
	
	@Autowired
	public MALService(HttpCaller httpCaller,
					  @Qualifier(value = "malRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
					  WrappedObjectMapper wrappedObjectMapper,
					  MALParser malParser,
					  URLBuilder urlBuilder,
					  CacheManager cacheManager) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.wrappedObjectMapper = wrappedObjectMapper;
		this.malParser = malParser;
		this.urlBuilder = urlBuilder;
		this.cacheManager = cacheManager;
	}
	
	/**
	 * Searches for user watching titles
	 *
	 * @param username the MAL username
	 * @return the watching titles
	 */
	public Set<UserMALTitleInfo> getWatchingTitles(String username) throws MALUserAccountNotFoundException, WatchingTitlesNotFoundException, MALUserAnimeListAccessException, JSONNotFoundException {
		Map<String, Map<String, String>> malRequestParameters = requestParametersBuilder.build();
		//get a number of user watching titles
		Integer numWatchingTitlesInteger = getNumberOfWatchingTitles(username, malRequestParameters);
		//запрос на страницу с текущими аниме
		//суть в том, что в один json с аниме инфой MAL добавляет только 300 аниме, а остальные нужно подгружать дополнительными запросами
		//делается первый стандартный запрос  на максимальное количество, т.е. 300
		//стандартный запрос animelist/testAccForDev
		Set<Set<UserMALTitleInfo>> titleJson = new LinkedHashSet<>();
		String targetUrl = urlBuilder.build(myAnimeListNet + ANIME_LIST + username, new HashMap<String, String>() {{
			put(STATUS, WATCHING.getCode().toString());
		}});
		titleJson.add(malParser.getUserTitlesInfo(httpCaller.call(targetUrl, GET, malRequestParameters), LinkedHashSet.class));
		Integer diff;
		//потом проверяем по количеству текущих аниме количество недогруженных аниме
		if (numWatchingTitlesInteger > MAX_NUMBER_OF_TITLE_IN_HTML) {
			Set<UserMALTitleInfo> firstJson = getAllWatchingTitles(MAX_NUMBER_OF_TITLE_IN_HTML, malRequestParameters, username);
			titleJson.add(firstJson);
			diff = numWatchingTitlesInteger - MAX_NUMBER_OF_TITLE_IN_HTML;
			int nextRequestCount = 2;
			while (diff > MAX_NUMBER_OF_TITLE_IN_HTML) {
				Set<UserMALTitleInfo> additionalJson = getAllWatchingTitles((numWatchingTitlesInteger * nextRequestCount), malRequestParameters, username);
				titleJson.add(additionalJson);
				nextRequestCount++;
				diff -= MAX_NUMBER_OF_TITLE_IN_HTML;
			}
		}
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		for (Set<UserMALTitleInfo> set : titleJson) {
			watchingTitles.addAll(set);
		}
		changePosterUrl(watchingTitles);
		changeAnimeUrl(watchingTitles);
		Cache userMALCache = cacheManager.getCache(userMALCacheName);
		userMALCache.putIfAbsent(username, watchingTitles);
		return watchingTitles;
	}
	
	/**
	 * Compare cached and fresh user watching titles
	 * @param watchingTitlesNew fresh watching titles
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
			UserMALTitleInfo userMALTitleInfoNew = watchingTitlesNew.stream().filter(set -> set.getTitle().equalsIgnoreCase(userMALTitleInfoFromCache.getTitle())).findFirst().orElse(null);
			if (userMALTitleInfoNew == null) {
				isWatchingTitlesUpdated = true;
				iterator.remove();
			}
		}
		return isWatchingTitlesUpdated;
	}
	
	/**
	 * Converts and sets poster URL from
	 * https://cdn.myanimelist.net/r/96x136/images/anime/7/86743.jpg?s=50f775b44d0a2317e9337a4eaaac6100
	 * to
	 * https://cdn.myanimelist.net/images/anime/7/86743.jpg
	 * <p>
	 * because last url provided better quality image
	 *
	 * @param watchingTitles the user mal anime list
	 */
	private void changePosterUrl(Set<UserMALTitleInfo> watchingTitles) {
		String changedPosterUrl = "";
		Pattern pattern;
		Matcher matcher;
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			pattern = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");
			matcher = pattern.matcher(userMALTitleInfo.getPosterUrl());
			if (matcher.find()) {
				changedPosterUrl = matcher.replaceAll("");
			}
			pattern = Pattern.compile("(\\?s=.+)");
			matcher = pattern.matcher(changedPosterUrl);
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
	private void changeAnimeUrl(Set<UserMALTitleInfo> watchingTitles) {
		watchingTitles.forEach(set -> set.setAnimeUrl(myAnimeListNet + set.getAnimeUrl()));
	}
	
	/**
	 * Searches for number of watching titles
	 *
	 * @param username             the  mal username
	 * @param malRequestParameters the http parameters
	 * @return the number of watching titles
	 * @throws MALUserAccountNotFoundException if user is not found
	 * @throws WatchingTitlesNotFoundException if number of watching titles is not found or == 0
	 */
	private Integer getNumberOfWatchingTitles(String username, Map<String, Map<String, String>> malRequestParameters) throws MALUserAccountNotFoundException, WatchingTitlesNotFoundException {
		String numWatchingTitles = malParser.getNumWatchingTitles(httpCaller.call(myAnimeListNet + PROFILE + username, GET, malRequestParameters));
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
	 * @param malRequestParameters     the http params
	 * @param username                 the mal username
	 * @return the set with the user anime titles
	 */
	private Set<UserMALTitleInfo> getAllWatchingTitles(Integer numWatchingTitlesInteger, Map<String, Map<String, String>> malRequestParameters, String username) {
		Map<String, String> queryParameters = new LinkedHashMap<>();
		queryParameters.put("offset", numWatchingTitlesInteger.toString());
		queryParameters.put(STATUS, WATCHING.getCode().toString());
		HttpResponse response = httpCaller.call(urlBuilder.build(myAnimeListNet + ANIME_LIST + username + "/" + LOAD_JSON, queryParameters), GET, malRequestParameters);
		return wrappedObjectMapper.unmarshal(response.getContent(), UserMALTitleInfo.class, LinkedHashSet.class);
	}
}
