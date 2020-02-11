package nasirov.yv.data.front;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import nasirov.yv.data.constants.FunDubSource;

/**
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

	public boolean isNotFound(String funDubSourceName) {
		String url = extractUrl(funDubSourceName);
		return isNull(url) || NOT_FOUND_ON_FUNDUB_SITE_URL.equals(url);
	}

	public boolean isAvailable(String funDubSourceName) {
		String url = extractUrl(funDubSourceName);
		return nonNull(url) && !FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(url) && !NOT_FOUND_ON_FUNDUB_SITE_URL.equals(url);
	}

	private String extractUrl(String funDubSourceName) {
		return funDubUrls.get(FunDubSource.getFunDubSourceByName(funDubSourceName));
	}
}
