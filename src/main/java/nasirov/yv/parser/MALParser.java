package nasirov.yv.parser;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.UserMALTitleInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Parser MAL html
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class MALParser {

	private static final String JSON_ANIME_LIST = "<table class=\"list-table\" data-items=\"(?<jsonAnimeList>.*)\">";

	private static final String USER_ANIME_LIST_PRIVATE_ACCESS = "Access to this list has been restricted by the owner";

	private static final String NUMBER_OF_WATCHING_TITLES = "Watching</a><span class=\"di-ib fl-r lh10\">(?<numWatchingTitles>\\d*?)</span>";

	/**
	 * Searches for the user anime list
	 *
	 * @param response the mal response
	 * @param collection any collection
	 * @param <T> a class extends collection
	 * @return collection with user anime titles
	 * @throws MALUserAnimeListAccessException if the user anime list has private access
	 * @throws JSONNotFoundException if the json anime list is not found
	 */
	public <T extends Collection> T getUserTitlesInfo(@NotNull HttpResponse response, @NotNull Class<T> collection)
			throws MALUserAnimeListAccessException, JSONNotFoundException {
		return WrappedObjectMapper.unmarshal(getJsonAnimeListFromHtml(response.getContent()), UserMALTitleInfo.class, collection);
	}

	/**
	 * Searches "Currently Watching" titles in the user profile html
	 *
	 * @param response the mal response
	 * @return the number of watching titles
	 * @throws MALUserAccountNotFoundException if user is not found
	 */
	public String getNumWatchingTitles(@NotNull HttpResponse response) throws MALUserAccountNotFoundException {
		if (!isAccountExist(response)) {
			throw new MALUserAccountNotFoundException("MAL User Account is Not Found!");
		}
		Pattern pattern = Pattern.compile(NUMBER_OF_WATCHING_TITLES);
		Matcher matcher = pattern.matcher(response.getContent());
		if (matcher.find()) {
			return matcher.group("numWatchingTitles");
		}
		return null;
	}

	/**
	 * Check user
	 *
	 * @param response mal response
	 * @return true if user exists
	 */
	private boolean isAccountExist(@NotNull HttpResponse response) {
		return !response.getStatus().equals(HttpStatus.NOT_FOUND.value());
	}

	/**
	 * Search in mal html json anime list
	 *
	 * @param content mal html
	 * @return string json anime list
	 * @throws JSONNotFoundException if json not found
	 * @throws MALUserAnimeListAccessException if user anime list has private access
	 */
	private String getJsonAnimeListFromHtml(@NotEmpty String content) throws JSONNotFoundException, MALUserAnimeListAccessException {
		String jsonAnimeList;
		Pattern pattern = Pattern.compile(JSON_ANIME_LIST);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			jsonAnimeList = matcher.group("jsonAnimeList").replaceAll("&quot;", "\"").replaceAll("&#039;", "'");
		} else if (content.contains(USER_ANIME_LIST_PRIVATE_ACCESS)) {
			throw new MALUserAnimeListAccessException(USER_ANIME_LIST_PRIVATE_ACCESS);
		} else {
			throw new JSONNotFoundException("JSON not found");
		}
		return jsonAnimeList;
	}
}
