package nasirov.yv.parser.impl;

import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.AnimediaEpisodeParserI;
import org.springframework.stereotype.Component;

/**
 * Html parser
 * <p>
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimediaEpisodeParser implements AnimediaEpisodeParserI {

	private static final String EPISODE_NUMBER_REGEXP = "(?<episode>\\d{1,3}([.-]\\d{1,3})?)";

	private static final Pattern EPISODE_NUMBER_PATTERN = Pattern.compile(EPISODE_NUMBER_REGEXP);

	/**
	 * Extracts an episode number from an episode name
	 * <p>
	 * e.g Серия 1 (64) -> 1
	 * <p>
	 * Серия 1-2 -> 1-2
	 * <p>
	 * Серия 1 -> 1
	 * <p>
	 * Серия -> 1
	 * <p>
	 * etc
	 *
	 * @param episodeName episode name {@link AnimediaEpisode#getEpisodeName()}
	 * @return episode number
	 */
	@Override
	public String extractEpisodeNumber(String episodeName) {
		Matcher matcher = EPISODE_NUMBER_PATTERN.matcher(episodeName);
		return matcher.find() ? matcher.group("episode") : getDefaultEpisodeNumber(episodeName);
	}

	private String getDefaultEpisodeNumber(String episodeName) {
		log.error("Cannot parse an episode number from [{}]", episodeName);
		return FIRST_EPISODE;
	}
}
