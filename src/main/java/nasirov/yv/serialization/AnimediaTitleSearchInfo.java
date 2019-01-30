package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Класс с аниме информацией для поиска на animedia
 * Created by Хикка on 17.12.2018.
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
	
	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords.toLowerCase();
	}
}
