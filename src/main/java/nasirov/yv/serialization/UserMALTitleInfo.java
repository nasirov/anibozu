package nasirov.yv.serialization;

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
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Integer getNumWatchedEpisodes() {
		return numWatchedEpisodes;
	}
	
	public void setNumWatchedEpisodes(Integer numWatchedEpisodes) {
		this.numWatchedEpisodes = numWatchedEpisodes;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
	
	public Integer getAnimeNumEpisodes() {
		return animeNumEpisodes;
	}
	
	public void setAnimeNumEpisodes(Integer animeNumEpisodes) {
		this.animeNumEpisodes = animeNumEpisodes;
	}
	
	public String getPosterUrl() {
		return posterUrl;
	}
	
	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}
	
	public String getAnimeUrl() {
		return animeUrl;
	}
	
	public void setAnimeUrl(String animeUrl) {
		this.animeUrl = animeUrl;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserMALTitleInfo that = (UserMALTitleInfo) o;
		if (id != that.id) return false;
		if (!status.equals(that.status)) return false;
		if (!numWatchedEpisodes.equals(that.numWatchedEpisodes)) return false;
		if (!title.equals(that.title)) return false;
		if (!animeNumEpisodes.equals(that.animeNumEpisodes)) return false;
		if (!posterUrl.equals(that.posterUrl)) return false;
		return animeUrl.equals(that.animeUrl);
	}
	
	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + status.hashCode();
		result = 31 * result + numWatchedEpisodes.hashCode();
		result = 31 * result + title.hashCode();
		result = 31 * result + animeNumEpisodes.hashCode();
		result = 31 * result + posterUrl.hashCode();
		result = 31 * result + animeUrl.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "UserMALTitleInfo{" +
				"id=" + id +
				", status=" + status +
				", numWatchedEpisodes=" + numWatchedEpisodes +
				", title='" + title + '\'' +
				", animeNumEpisodes=" + animeNumEpisodes +
				", posterUrl='" + posterUrl + '\'' +
				", animeUrl='" + animeUrl + '\'' +
				'}';
	}
}
