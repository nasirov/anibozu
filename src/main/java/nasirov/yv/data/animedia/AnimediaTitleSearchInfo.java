package nasirov.yv.data.animedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Anime information for search on animedia
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimediaTitleSearchInfo {

	/**
	 * Anime title
	 */
	private String title;

	/**
	 * Keywords for search
	 */
	private String keywords;

	/**
	 * Anime URL
	 */
	private String url;

	/**
	 * Poster URL
	 */
	@JsonProperty(value = "poster")
	private String posterUrl;

	public AnimediaTitleSearchInfo(AnimediaTitleSearchInfo animediaTitleSearchInfo) {
		this.title = animediaTitleSearchInfo.title;
		this.keywords = animediaTitleSearchInfo.keywords;
		this.url = animediaTitleSearchInfo.url;
		this.posterUrl = animediaTitleSearchInfo.posterUrl;
	}

	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords.toLowerCase();
	}
}
