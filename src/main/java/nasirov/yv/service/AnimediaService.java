package nasirov.yv.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nasirov.yv.data.constants.CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE;
import static nasirov.yv.parser.WrappedObjectMapper.unmarshal;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriUtils.encodePath;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class AnimediaService implements AnimediaServiceI {

	private static final String POSTER_URL_LOW_QUALITY_QUERY_PARAMETER = "h=70&q=50";

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";

	private Map<String, Map<String, String>> animediaRequestParameters;

	private Cache currentlyUpdatedTitlesCache;

	private String onlineAnimediaTv;

	private String animediaSearchListFromGitHubFullUrl;

	private String animediaSearchListFromAnimediaFullUrl;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	private CacheManager cacheManager;

	private UrlsNames urlsNames;


	@Autowired
	public AnimediaService(HttpCaller httpCaller,
			@Qualifier(value = "animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder, AnimediaHTMLParser
			animediaHTMLParser,
			CacheManager cacheManager, UrlsNames urlsNames) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.cacheManager = cacheManager;
		this.urlsNames = urlsNames;
	}

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
		currentlyUpdatedTitlesCache = cacheManager.getCache(CURRENTLY_UPDATED_TITLES_CACHE);
		onlineAnimediaTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
		animediaSearchListFromGitHubFullUrl = urlsNames.getGitHubUrls().getRawGithubusercontentComAnimediaSearchList();
		animediaSearchListFromAnimediaFullUrl = urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeList();
	}

	/**
	 * Searches for fresh animedia search list from animedia
	 *
	 * @return the list with title search info on animedia
	 */
	@Override
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromAnimedia() {
		HttpResponse animediaResponse = httpCaller.call(animediaSearchListFromAnimediaFullUrl, GET, animediaRequestParameters);
		Set<AnimediaTitleSearchInfo> animediaSearchList = unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		animediaSearchList.forEach(title -> {
			title.setUrl(encodePath(title.getUrl().replaceAll(onlineAnimediaTv, ""), UTF_8));
			title.setPosterUrl(title.getPosterUrl().replace(POSTER_URL_LOW_QUALITY_QUERY_PARAMETER, POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER));
		});
		return animediaSearchList;
	}

	/**
	 * Searches for fresh animedia search list from github
	 *
	 * @return the list with title search info on animedia
	 */
	@Override
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromGitHub() {
		HttpResponse animediaResponse = httpCaller.call(animediaSearchListFromGitHubFullUrl, GET, animediaRequestParameters);
		return unmarshal(animediaResponse.getContent(), AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}

	/**
	 * Searches for currently updated titles on animedia
	 *
	 * @return list of currently updated titles
	 */
	@Override
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles() {
		HttpResponse animediaResponse = httpCaller.call(onlineAnimediaTv, GET, animediaRequestParameters);
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaHTMLParser.getCurrentlyUpdatedTitlesList(animediaResponse);
		currentlyUpdatedTitlesCache.putIfAbsent(CURRENTLY_UPDATED_TITLES_CACHE, currentlyUpdatedTitles);
		return currentlyUpdatedTitles;
	}

	/**
	 * Compare cached currently updated titles and fresh refresh cache with difference
	 *
	 * @param fresh fresh updated titles
	 * @param fromCache updated titles from cache
	 * @return list of differences between fresh and cached
	 */
	@Override
	public List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles(List<AnimediaMALTitleReferences> fresh,
			List<AnimediaMALTitleReferences> fromCache) {
		List<AnimediaMALTitleReferences> differences = new ArrayList<>();
		if (fromCache.isEmpty() || fresh.isEmpty()) {
			return differences;
		} else {
			AnimediaMALTitleReferences firstCurrentlyUpdatedTitleFromCache = fromCache.get(0);
			for (AnimediaMALTitleReferences freshCurrentlyUpdatedTitle : fresh) {
				if (firstCurrentlyUpdatedTitleFromCache.equals(freshCurrentlyUpdatedTitle)) {
					break;
				} else {
					differences.add(freshCurrentlyUpdatedTitle);
				}
			}
			currentlyUpdatedTitlesCache.put(CURRENTLY_UPDATED_TITLES_CACHE, fresh);
			return differences;
		}
	}

}
