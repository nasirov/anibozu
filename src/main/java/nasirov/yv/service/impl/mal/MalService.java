package nasirov.yv.service.impl.mal;

import static java.util.Optional.ofNullable;
import static nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus.WATCHING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.data.mal.MalUserInfo.MalUserInfoBuilder;
import nasirov.yv.data.properties.MalProps;
import nasirov.yv.exception.mal.AbstractMalException;
import nasirov.yv.exception.mal.MalForbiddenException;
import nasirov.yv.exception.mal.MalUserAccountNotFoundException;
import nasirov.yv.exception.mal.MalUserAnimeListAccessException;
import nasirov.yv.exception.mal.UnexpectedCallingException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.mal.MalFeignClient;
import nasirov.yv.parser.MalParserI;
import nasirov.yv.service.MalServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Nasirov Yuriy
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MalService implements MalServiceI {

	private static final Pattern POSTER_URL_RESOLUTION_PATTERN = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");

	private static final Pattern S_VARIABLE_PATTERN = Pattern.compile("(\\?s=.+)");

	private final MalFeignClient malFeignClient;

	private final MalParserI malParser;

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
	 * @throws MalUserAccountNotFoundException if username doesn't exist
	 */
	@Override
	public List<MalTitle> getWatchingTitles(String username) throws AbstractMalException {
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
	 * Builds dto with a MAL user info
	 *
	 * @param username a MAL username
	 * @return dto with a mal user info
	 */
	@Override
	@Cacheable(value = "mal", key = "#username", sync = true)
	public MalUserInfo getMalUserInfo(String username) {
		log.debug("Trying to build MalUserInfo for [{}]", username);
		MalUserInfoBuilder malUserInfoBuilder = MalUserInfo.builder()
				.username(username);
		List<MalTitle> watchingTitles = Collections.emptyList();
		String errorMessage = null;
		try {
			watchingTitles = getWatchingTitles(username);
		} catch (AbstractMalException e) {
			errorMessage = e.getMessage();
		} catch (Exception e) {
			errorMessage = "Sorry, " + username + ", unexpected error has occurred.";
		}
		if (Objects.nonNull(errorMessage)) {
			log.error(errorMessage);
		}
		malUserInfoBuilder.malTitles(watchingTitles);
		malUserInfoBuilder.errorMessage(errorMessage);
		log.debug("Built MalUserInfo for [{}]", username);
		return malUserInfoBuilder.build();
	}

	private String extractUserProfile(String username) throws MalUserAccountNotFoundException, MalForbiddenException {
		ResponseEntity<String> malResponseWithUserProfile = malFeignClient.getUserProfile(username);
		validateMalResponse(malResponseWithUserProfile, username);
		return malResponseWithUserProfile.getBody();
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
		title.setAnimeUrl(malProps.getUrl() + title.getAnimeUrl());
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
	private List<MalTitle> getJsonTitlesAndUnmarshal(Integer currentOffset, String username) throws MalUserAnimeListAccessException {
		ResponseEntity<List<MalTitle>> malResponse = malFeignClient.getUserAnimeList(username, currentOffset, WATCHING.getCode());
		checkUserAnimeListAccess(malResponse, username);
		return ofNullable(malResponse.getBody()).orElseGet(Collections::emptyList);
	}

	private void checkUserAnimeListAccess(ResponseEntity<List<MalTitle>> malResponse, String username) throws MalUserAnimeListAccessException {
		if (malResponse.getStatusCode()
				.equals(HttpStatus.BAD_REQUEST)) {
			throw new MalUserAnimeListAccessException("Anime list " + username + " has private access!");
		}
		validateMalResponse(username, malResponse.getStatusCode());
	}

	/**
	 * Checks a response status from MAL
	 *
	 * @param malResponseWithUserProfile a MAL response with an user profile
	 * @param username                   the MAL username
	 * @throws MalUserAccountNotFoundException if a response status == 404
	 * @throws MalForbiddenException           if a response status == 403
	 */
	private void validateMalResponse(ResponseEntity<String> malResponseWithUserProfile, String username)
			throws MalUserAccountNotFoundException, MalForbiddenException {
		HttpStatus responseStatus = malResponseWithUserProfile.getStatusCode();
		if (responseStatus.equals(HttpStatus.NOT_FOUND)) {
			throw new MalUserAccountNotFoundException("MAL account " + username + " is not found");
		}
		if (responseStatus.equals(HttpStatus.FORBIDDEN)) {
			throw new MalForbiddenException("Sorry, " + username + ", but MAL rejected our requests with status 403.");
		}
		validateMalResponse(username, responseStatus);
	}

	private void validateMalResponse(String username, HttpStatus responseStatus) {
		if (!responseStatus.equals(HttpStatus.OK)) {
			throw new UnexpectedCallingException("Sorry, " + username + ", unexpected error has occurred.");
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
