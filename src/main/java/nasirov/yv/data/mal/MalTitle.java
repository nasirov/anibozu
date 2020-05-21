package nasirov.yv.data.mal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * MAL User Anime List Info animelist/USERNAME
 * <p>
 * Created by nasirov.yv
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MalTitle {

	/**
	 * Animed id in MAL db
	 */
	@JsonProperty("anime_id")
	private Integer id;

	/**
	 * Number of watched episodes
	 */
	@JsonProperty(value = "num_watched_episodes")
	private Integer numWatchedEpisodes;

	/**
	 * Anime title
	 */
	@JsonProperty(value = "anime_title")
	private String name;

	/**
	 * Poster URL
	 */
	@JsonProperty(value = "anime_image_path")
	private String posterUrl;

	/**
	 * Anime URL
	 */
	@JsonProperty(value = "anime_url")
	private String animeUrl;

	public void setName(String name) {
		this.name = StringUtils.lowerCase(name);
	}
}
