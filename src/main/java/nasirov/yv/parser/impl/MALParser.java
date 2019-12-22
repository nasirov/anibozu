package nasirov.yv.parser.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.MALParserI;
import org.springframework.stereotype.Component;

/**
 * Parser MAL html Created by nasirov.yv
 */
@Component
@Slf4j
public class MALParser implements MALParserI {

	private static final String NUMBER_OF_WATCHING_TITLES_REGEXP = "Watching</a><span class=\"di-ib fl-r lh10\">(?<numWatchingTitles>[\\d,]*?)</span>";

	private static final Pattern NUMBER_OF_WATCHING_TITLES_PATTERN = Pattern.compile(NUMBER_OF_WATCHING_TITLES_REGEXP);

	/**
	 * Searches for "Currently Watching" titles in an user profile html
	 *
	 * @param userProfile html with an user profile
	 * @return number of watching titles
	 */
	@Override
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
