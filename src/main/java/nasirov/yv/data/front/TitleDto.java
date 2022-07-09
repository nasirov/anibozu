package nasirov.yv.data.front;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_URL;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleDto {

	/**
	 * Anime name on MAL
	 */
	private String animeName;

	/**
	 * Target episode number on MAL
	 */
	private String malEpisodeNumber;

	/**
	 * Poster URL on MyAnimeList
	 */
	private String posterUrlOnMal;

	/**
	 * Anime URL on MyAnimeList
	 */
	private String animeUrlOnMal;

	/**
	 * Map with target episode URLs to fandub sites
	 */
	@Singular
	private Map<FanDubSource, String> fanDubUrls;

	/**
	 * Map with target episode names to fandub sites
	 */
	@Singular
	private Map<FanDubSource, String> fanDubEpisodeNames;

	public boolean isAvailable(String fanDubSourceName) {
		String url = extractUrl(fanDubSourceName);
		return !NOT_AVAILABLE_EPISODE_URL.equals(url) && !TITLE_NOT_FOUND_EPISODE_URL.equals(url);
	}

	public boolean isNotAvailable(String fanDubSourceName) {
		return NOT_AVAILABLE_EPISODE_URL.equals(extractUrl(fanDubSourceName));
	}

	private String extractUrl(String fanDubSourceName) {
		return fanDubUrls.getOrDefault(FanDubSource.getFanDubSourceByName(fanDubSourceName), "default");
	}
}
