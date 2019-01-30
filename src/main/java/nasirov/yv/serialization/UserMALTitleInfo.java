package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * MAL User Anime List Info
 * animelist/USERNAME
 * Created by Хикка on 19.12.2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMALTitleInfo {
	@Id
	@GeneratedValue
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
	
	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
}
