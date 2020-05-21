package nasirov.yv.data.animedia.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto for animedia api deserialization
 * <p>
 * Created by nasirov.yv
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchListTitle {

	/**
	 * Animed ID on Animedia
	 * <p>
	 * e.g 1234
	 */
	@JsonProperty("animeId")
	private String animeId;

	/**
	 * Anime URL on Animedia
	 * <p>
	 * e.g anime/some-anime
	 */
	@JsonProperty("url")
	private String url;

	/**
	 * Seasons list
	 * <p>
	 * e.g "season": [ { "seasonId": 1 } ]
	 * <p>
	 * or if announcement "season": null
	 */
	@JsonProperty("season")
	private List<DataList> dataLists;
}
