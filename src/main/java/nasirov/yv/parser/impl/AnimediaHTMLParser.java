package nasirov.yv.parser.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static org.apache.commons.lang.StringUtils.isNotBlank;

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
@Component
@Slf4j
public class AnimediaHTMLParser implements AnimediaHTMLParserI {

	private static final String EPISODE_NUMBER_REGEXP = "^(?<description>[a-zA-Zа-яА-Я\\s]*)?\\s*(?<firstEpisodeInSeason>\\d{1,3})?(-"
			+ "(?<joinedEpisode>\\d{1,3}))?(\\s\\(\\d{1,3}\\))?\\s*$";

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
		String episodes = null;
		if (matcher.find()) {
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			String joinedEpisode = matcher.group("joinedEpisode");
			if (firstEpisodeInSeason != null) {
				if (joinedEpisode != null) {
					episodes = firstEpisodeInSeason + "-" + joinedEpisode;
				} else {
					episodes = firstEpisodeInSeason;
				}
			} else if (isNotBlank(description)) {
				episodes = FIRST_EPISODE;
			}
		}
		return ofNullable(episodes).orElseThrow(() -> new EpisodeNumberNotFoundException(
				"Episode number was not found! Check EPISODE_NUMBER_REGEXP for episode name: " + episodeName));
	}
}
