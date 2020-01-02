package nasirov.yv.data.mal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MAL User Anime List Info animelist/USERNAME Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMALTitleInfo {

	/**
	 * Animed id in MAL db
	 */
	@JsonProperty("anime_id")
	private Integer animeId;

	/**
	 * Number of watched episodes
	 */
	@JsonProperty(value = "num_watched_episodes")
	private Integer numWatchedEpisodes;

	/**
	 * Anime title
	 */
	@JsonProperty(value = "anime_title")
	private String title;

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

	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
}
