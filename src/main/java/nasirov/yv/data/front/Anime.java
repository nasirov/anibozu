package nasirov.yv.data.front;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;

import lombok.Builder;
import lombok.Data;
import nasirov.yv.data.constants.FunDubSource;

/**
 * Created by nasirov.yv
 */
@Data
@Builder
public class Anime {

	/**
	 * Target FunDub
	 */
	private FunDubSource funDubSource;

	/**
	 * Title name on MAL
	 */
	private String titleName;

	/**
	 * Link of available episode
	 */
	private String link;

	/**
	 * Number of available episode
	 */
	private String episode;

	public boolean isNotFound() {
		return nonNull(link) && NOT_FOUND_ON_FUNDUB_SITE_URL.equals(link);
	}

	public boolean isAvailable() {
		return nonNull(link) && !FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE.equals(link);
	}
}
