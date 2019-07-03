package nasirov.yv.data.mal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * MAL User Anime List Info
 * animelist/USERNAME
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMALTitleInfo {

	@Id
	@GeneratedValue
	@EqualsAndHashCode.Exclude
	private int id;

	/**
	 * Anime Status
	 */
	private Integer status;

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
	 * Max number of episodes
	 */
	@JsonProperty(value = "anime_num_episodes")
	private Integer animeNumEpisodes;

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

	public UserMALTitleInfo(UserMALTitleInfo userMALTitleInfo) {
		this.id = userMALTitleInfo.getId();
		this.status = userMALTitleInfo.getStatus();
		this.numWatchedEpisodes = userMALTitleInfo.getNumWatchedEpisodes();
		this.title = userMALTitleInfo.getTitle();
		this.animeNumEpisodes = userMALTitleInfo.getAnimeNumEpisodes();
		this.posterUrl = userMALTitleInfo.getPosterUrl();
		this.animeUrl = userMALTitleInfo.getAnimeUrl();
	}

	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
}