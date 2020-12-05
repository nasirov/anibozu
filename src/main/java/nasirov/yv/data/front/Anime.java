package nasirov.yv.data.front;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;

/**
 * Dto holds processing result information and handles by freemarker via templates
 * <p>
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

	/**
	 * Anime name on MAL
	 */
	private String animeName;

	/**
	 * Number of available episode
	 */
	private String episode;

	/**
	 * Poster URL on MyAnimeList
	 */
	private String posterUrlOnMal;

	/**
	 * Anime URL on MyAnimeList
	 */
	private String animeUrlOnMal;

	/**
	 * Map with URLs to fandub sites
	 */
	@Singular
	private Map<FanDubSource, String> fanDubUrls;

	public boolean isAvailable(String fanDubSourceName) {
		String url = extractUrl(fanDubSourceName);
		return !FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(url) && !NOT_FOUND_ON_FANDUB_SITE_URL.equals(url);
	}

	public boolean isNotAvailable(String fanDubSourceName) {
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(extractUrl(fanDubSourceName));
	}

	private String extractUrl(String fanDubSourceName) {
		return fanDubUrls.getOrDefault(FanDubSource.getFanDubSourceByName(fanDubSourceName), "default");
	}
}
