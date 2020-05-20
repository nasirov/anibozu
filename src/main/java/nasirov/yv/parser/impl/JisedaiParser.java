package nasirov.yv.parser.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static org.apache.logging.log4j.util.Strings.EMPTY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.JisedaiParserI;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class JisedaiParser implements JisedaiParserI {

	private static final Pattern EPISODE_PATTERN = Pattern.compile("^(?<episode>\\d{1,3})");

	@Override
	public Integer extractEpisodeNumber(String episodeName) {
		Matcher matcher = EPISODE_PATTERN.matcher(ofNullable(episodeName).orElse(EMPTY));
		String episode = matcher.find() ? matcher.group("episode") : getDefaultEpisodeNumber(episodeName);
		return Integer.valueOf(episode);
	}

	private String getDefaultEpisodeNumber(String episodeName) {
		log.error("Cannot parse an episode number from [{}]", episodeName);
		return FIRST_EPISODE;
	}
}
