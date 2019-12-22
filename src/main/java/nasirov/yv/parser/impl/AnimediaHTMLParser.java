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
 * Html parser Created by nasirov.yv
 */
@Component
@Slf4j
public class AnimediaHTMLParser implements AnimediaHTMLParserI {

	private static final String ANIME_ID_REGEXP = "<ul role=\"tablist\" class=\"media__tabs__nav nav-tabs\" data-entry_id=\"(?<animeId>\\d+)\".*?";

	private static final String EPISODE_NUMBER_REGEXP =
			"^(?<description>[a-zA-Zа-яА-Я\\s]*)?\\s*(?<firstEpisodeInSeason>\\d{1,3})?(-" + "(?<joinedEpisode>\\d{1,3}))?(\\s\\(\\d{1,3}\\))?\\s*$";

	private static final Pattern EPISODE_NUMBER_PATTERN = Pattern.compile(EPISODE_NUMBER_REGEXP);

	private static final Pattern ANIME_ID_PATTERN = Pattern.compile(ANIME_ID_REGEXP);

	/**
	 * Extracts an episode number from an episode name e.g Серия 1 (64) -> 1 Серия 1-2 -> 1-2 Серия 1 -> 1 Серия -> 1 etc
	 *
	 * @param episodeName episode name {@link Response#getEpisodeName()}
	 * @return episode number
	 */
	@Override
	public String extractEpisodeNumber(String episodeName) {
		return getEpisodeNumber(episodeName);
	}

	/**
	 * Searches for an anime id on a title html page
	 *
	 * @param content html page
	 * @return anime id
	 */
	@Override
	public String getAnimeId(String content) {
		String animeId = null;
		Matcher matcher = ANIME_ID_PATTERN.matcher(ofNullable(content).orElse(""));
		if (matcher.find()) {
			animeId = matcher.group("animeId");
		}
		return animeId;
	}

	private String getEpisodeNumber(String episodeName) throws EpisodeNumberNotFoundException {
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
