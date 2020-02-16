package nasirov.yv.data.front;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import nasirov.yv.data.constants.FunDubSource;

/**
 * Dto holds processing result information and handles by freemarker via templates
 * <p>
 * Created by nasirov.yv
 */
@Data
@Builder
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
	private String posterUrlOnMAL;

	/**
	 * Anime URL on MyAnimeList
	 */
	private String animeUrlOnMAL;

	/**
	 * Map with URLs to fundub sites
	 */
	@Singular
	private Map<FunDubSource, String> funDubUrls;

	public String getUrlByFunDubSourceName(String funDubSourceName) {
		return funDubUrls.getOrDefault(FunDubSource.getFunDubSourceByName(funDubSourceName), "");
	}

	public boolean isAvailable(String funDubSourceName) {
		String url = extractUrl(funDubSourceName);
		return !FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(url) && !NOT_FOUND_ON_FUNDUB_SITE_URL.equals(url);
	}

	public boolean isNotAvailable(String funDubSourceName) {
		return FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(extractUrl(funDubSourceName));
	}

	public boolean isNotFound(String funDubSourceName) {
		return NOT_FOUND_ON_FUNDUB_SITE_URL.equals(extractUrl(funDubSourceName));
	}

	private String extractUrl(String funDubSourceName) {
		return funDubUrls.getOrDefault(FunDubSource.getFunDubSourceByName(funDubSourceName), "default");
	}
}
