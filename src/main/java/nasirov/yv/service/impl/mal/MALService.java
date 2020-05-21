package nasirov.yv.service.impl.mal;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static nasirov.yv.data.mal.MALCategories.ANIME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALSearchCategories;
import nasirov.yv.data.mal.MALSearchTitleInfo;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.MalProps;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.http.feign.MALFeignClient;
import nasirov.yv.parser.MALParserI;
import nasirov.yv.service.MALServiceI;
import org.springframework.cache.annotation.Cacheable;
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

	private final MALFeignClient malFeignClient;

	private final MALParserI malParser;

	private final UrlsNames urlsNames;

	private final MalProps malProps;

	private int offsetStep;

	@PostConstruct
	public void init() {
		offsetStep = malProps.getOffsetStep();
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
	@Cacheable(value = "mal", key = "#username", unless = "#result?.isEmpty()")
	public List<MalTitle> getWatchingTitles(String username)
			throws WatchingTitlesNotFoundException, MALUserAccountNotFoundException, MALUserAnimeListAccessException {
		log.debug("Trying to get watching titles for [{}]...", username);
		String userProfile = extractUserProfile(username);
		int numberOfUserWatchingTitles = extractNumberOfWatchingTitles(userProfile, username);
		List<MalTitle> watchingTitles = new ArrayList<>(numberOfUserWatchingTitles);
		for (int offset = 0; offset < numberOfUserWatchingTitles; offset += offsetStep) {
			watchingTitles.addAll(getJsonTitlesAndUnmarshal(offset, username));
		}
		List<MalTitle> result = formatWatchingTitles(watchingTitles);
		log.debug("Got [{}] watching titles for [{}].", result.size(), username);
		return result;
	}

	/**
	 * Checks MAL title name for existence
	 *
	 * @param titleNameOnMal MAL title name
	 * @param titleIdOnMal   title id in MAL db
	 * @return true if MAL response contain one anime title with equals titleNameOnMal and titleIdOnMal, else false
	 */
	@Override
	public boolean isTitleExist(String titleNameOnMal, Integer titleIdOnMal) {
		return malFeignClient.searchTitleByName(titleNameOnMal)
				.getCategories()
				.stream()
				.filter(this::isAnimeCategory)
				.map(MALSearchCategories::getItems)
				.flatMap(List::stream)
				.anyMatch(title -> isTargetTitle(titleNameOnMal, titleIdOnMal, title));
	}

	private String extractUserProfile(String username) throws MALUserAccountNotFoundException {
		ResponseEntity<String> malResponseWithUserProfile = malFeignClient.getUserProfile(username);
		String userProfile = malResponseWithUserProfile.getBody();
		validateUserAccountExistence(malResponseWithUserProfile, username);
		return userProfile;
	}

	private int extractNumberOfWatchingTitles(String userProfile, String username) throws WatchingTitlesNotFoundException {
		int numberOfUserWatchingTitles = malParser.getNumWatchingTitles(userProfile);
		validateNumberOfUserWatchingTitles(numberOfUserWatchingTitles, username);
		return numberOfUserWatchingTitles;
	}

	private List<MalTitle> formatWatchingTitles(List<MalTitle> watchingTitles) {
		return watchingTitles.stream()
				.map(this::changePosterUrl)
				.map(this::changeAnimeUrl)
				.map(this::changeTitleName)
				.collect(Collectors.toList());
	}

	private boolean isAnimeCategory(MALSearchCategories categories) {
		return categories.getType()
				.equals(ANIME.getDescription());
	}

	private boolean isTargetTitle(String titleNameOnMal, Integer titleIdOnMal, MALSearchTitleInfo malSearchTitleInfo) {
		return malSearchTitleInfo.getType()
				.equals(ANIME.getDescription()) && malSearchTitleInfo.getName()
				.equalsIgnoreCase(titleNameOnMal) && malSearchTitleInfo.getAnimeId()
				.equals(titleIdOnMal);
	}

	/**
	 * Changes and sets poster URL from https://cdn.myanimelist.net/r/96x136/images/anime/7/86743.jpg?s=50f775b44d0a2317e9337a4eaaac6100 to
	 * <p>
	 * https://cdn.myanimelist.net/images/anime/7/86743.jpg
	 * <p>
	 * because last url provides better quality image
	 *
	 * @param title MAL title
	 */
	private MalTitle changePosterUrl(MalTitle title) {
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
		return title;
	}

	/**
	 * Sets full anime url
	 *
	 * @param title a MAL title
	 */
	private MalTitle changeAnimeUrl(MalTitle title) {
		title.setAnimeUrl(urlsNames.getMalUrls()
				.getMyAnimeListNet() + title.getAnimeUrl());
		return title;
	}

	/**
	 * Sets unescaped title name
	 *
	 * @param title a MAL title
	 */
	private MalTitle changeTitleName(MalTitle title) {
		title.setName(HtmlUtils.htmlUnescape(title.getName()));
		return title;
	}

	/**
	 * Searches for additional json anime list and unmarshal https://myanimelist.net/animelist/username/load.json?offset=currentOffset&status=1
	 *
	 * @param currentOffset the number of watching titles
	 * @param username      the MAL username
	 * @return the set with the user anime titles
	 */
	private List<MalTitle> getJsonTitlesAndUnmarshal(Integer currentOffset, String username) throws MALUserAnimeListAccessException {
		ResponseEntity<List<MalTitle>> malResponse = malFeignClient.getUserAnimeList(username, currentOffset, WATCHING.getCode());
		checkUserAnimeListAccess(malResponse, username);
		return ofNullable(malResponse.getBody()).orElseGet(Collections::emptyList);
	}

	private void checkUserAnimeListAccess(ResponseEntity<List<MalTitle>> malResponse, String username) throws MALUserAnimeListAccessException {
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
	 * @throws WatchingTitlesNotFoundException if numberOfUserWatchingTitles == 0
	 */
	private void validateNumberOfUserWatchingTitles(int numberOfUserWatchingTitles, String username) throws WatchingTitlesNotFoundException {
		if (numberOfUserWatchingTitles == 0) {
			throw new WatchingTitlesNotFoundException("Not found watching titles for " + username + " !");
		}
	}
}
