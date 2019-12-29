package nasirov.yv.data.animedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.animedia.api.Season;

/**
 * Anime information for search on animedia
 *
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimediaSearchListTitle {

	/**
	 * Animed ID on Animedia
	 * e.g 1234
	 */
	@JsonProperty("animeId")
	private String animeId;

	/**
	 * Anime URL on Animedia
	 *
	 * e.g anime/some-anime
	 */
	@JsonProperty("url")
	private String url;

	/**
	 * Seasons list
	 * <p>
	 * e.g "season": [ { "displayName": "1 Сезон", "seasonId": 1 } ]
	 * <p>
	 * or if announcement "season": null
	 */
	@JsonProperty("season")
	private List<Season> seasons;
}
