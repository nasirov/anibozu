package nasirov.yv.parser.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.api.Response;
import nasirov.yv.exception.animedia.EpisodeNumberNotFoundException;
import nasirov.yv.parser.AnimediaHTMLParserI;
import org.springframework.stereotype.Component;

/**
 * Html parser
 * <p>
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimediaHTMLParser implements AnimediaHTMLParserI {

	private static final String EPISODE_NUMBER_REGEXP = "^([a-zA-Zа-яА-Я.\\s]*)?\\s*(?<episode>\\d{1,3}(.\\d{1,3})?)?(.*)?$";

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
	 * @param episodeName episode name {@link Response#getEpisodeName()}
	 * @return episode number
	 */
	@Override
	public String extractEpisodeNumber(String episodeName) {
		return getEpisodeNumber(episodeName);
	}

	private String getEpisodeNumber(String episodeName) {
		Matcher matcher = EPISODE_NUMBER_PATTERN.matcher(episodeName);
		String result = null;
		if (matcher.find()) {
			String episode = matcher.group("episode");
			if (episode != null) {
				result = episode;
				if (episode.contains("-")) {
					log.debug("Parsed joined episode [{}] from [{}]", result, episodeName);
				} else {
					log.debug("Parsed regular episode number [{}] from [{}]", result, episodeName);
				}
			} else {
				result = FIRST_EPISODE;
				log.debug("Parsed episode without episode number [{}] from [{}]", result, episodeName);
			}
		}
		return ofNullable(result).orElseThrow(() -> new EpisodeNumberNotFoundException(
				"Episode number was not found! Check EPISODE_NUMBER_REGEXP for episode name: " + episodeName));
	}
}
