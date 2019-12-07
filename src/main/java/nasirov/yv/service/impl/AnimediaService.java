package nasirov.yv.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE;
import static org.springframework.web.util.UriUtils.encodePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnimediaFeignClient;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.service.AnimediaServiceI;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaService implements AnimediaServiceI {

	private static final String POSTER_URL_LOW_QUALITY_QUERY_PARAMETER = "h=70&q=50";

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";

	private final AnimediaFeignClient animediaFeignClient;

	private final GitHubFeignClient gitHubFeignClient;

	private final AnimediaHTMLParser animediaHTMLParser;

	private final CacheManager cacheManager;

	private final UrlsNames urlsNames;

	private Cache currentlyUpdatedTitlesCache;

	private String onlineAnimediaTv;

	@PostConstruct
	public void init() {
		currentlyUpdatedTitlesCache = cacheManager.getCache(CURRENTLY_UPDATED_TITLES_CACHE);
		onlineAnimediaTv = urlsNames.getAnimediaUrls()
				.getOnlineAnimediaTv();
	}

	/**
	 * Searches for fresh animedia search list from animedia
	 *
	 * @return the list with title search info on animedia
	 */
	@Override
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromAnimedia() {
		ResponseEntity<Set<AnimediaTitleSearchInfo>> animediaRespone = animediaFeignClient.getAnimediaSearchList();
		Set<AnimediaTitleSearchInfo> animediaSearchList = ofNullable(animediaRespone.getBody()).orElseGet(Collections::emptySet);
		animediaSearchList.forEach(title -> {
			title.setUrl(encodePath(title.getUrl()
					.replaceAll(onlineAnimediaTv, ""), UTF_8));
			title.setPosterUrl(title.getPosterUrl()
					.replace(POSTER_URL_LOW_QUALITY_QUERY_PARAMETER, POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER));
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
		ResponseEntity<Set<AnimediaTitleSearchInfo>> animediaResponse = gitHubFeignClient.getAnimediaSearchList();
		return ofNullable(animediaResponse.getBody()).orElseGet(Collections::emptySet);
	}

	/**
	 * Searches for currently updated titles on animedia
	 *
	 * @return list of currently updated titles
	 */
	@Override
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles() {
		ResponseEntity<String> animediaResponse = animediaFeignClient.getAnimediaMainPage();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaHTMLParser.getCurrentlyUpdatedTitlesList(animediaResponse.getBody());
		currentlyUpdatedTitlesCache.putIfAbsent(CURRENTLY_UPDATED_TITLES_CACHE, currentlyUpdatedTitles);
		return currentlyUpdatedTitles;
	}

	/**
	 * Compare cached currently updated titles and fresh refresh cache with difference
	 *
	 * @param fresh     fresh updated titles
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
