package nasirov.yv.parser.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static org.apache.logging.log4j.util.Strings.EMPTY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.AnidubParserI;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnidubParser implements AnidubParserI {

	private static final Pattern EPISODE_PATTERN = Pattern.compile("^(?<episode>\\d{1,3})");

	private static final Pattern BROKEN_URL_PATTERN = Pattern.compile("(?<url>http.?://video\\.sibnet\\.ru/shell\\.php\\?videoid=\\d+)\"");

	@Override
	public Integer extractEpisodeNumber(String episodeName) {
		Matcher matcher = EPISODE_PATTERN.matcher(ofNullable(episodeName).orElse(EMPTY));
		String episode = matcher.find() ? matcher.group("episode") : FIRST_EPISODE;
		return Integer.valueOf(episode);
	}

	@Override
	public String fixBrokenUrl(String url) {
		Matcher matcher = BROKEN_URL_PATTERN.matcher(url);
		return matcher.find() ? matcher.group("url") : url;
	}
}
