package nasirov.yv.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Parser MAL html Created by nasirov.yv
 */
@Component
@Slf4j
public class MALParser {

	private static final Pattern NUMBER_OF_WATCHING_TITLES_PATTERN = Pattern.compile(
			"Watching</a><span class=\"di-ib fl-r lh10\">(?<numWatchingTitles>[\\d,]*?)</span>");

	/**
	 * Searches "Currently Watching" titles in the user profile html
	 *
	 * @param userProfile an user profile
	 * @return the number of watching titles
	 */
	public Integer getNumWatchingTitles(String userProfile) {
		Integer numberOfWatchingTitles = null;
		Matcher matcher = NUMBER_OF_WATCHING_TITLES_PATTERN.matcher(userProfile);
		if (matcher.find()) {
			numberOfWatchingTitles = Integer.parseInt(matcher.group("numWatchingTitles")
					.replace(",", ""));
		}
		return numberOfWatchingTitles;
	}
}
