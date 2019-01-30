package nasirov.yv.serialization;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Класс с аниме информацией для поиска на animedia
 * Created by Хикка on 17.12.2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimediaTitleSearchInfo {
	/**
	 * Anime title
	 */
	@Getter
	private String title;
	
	/**
	 * Keywords for search
	 */
	@Getter
	private String keywords;
	
	/**
	 * Anime URL
	 */
	@Getter
	@Setter
	private String url;
	
	/**
	 * Poster URL
	 */
	@JsonProperty(value = "poster")
	@Getter
	@Setter
	private String posterUrl;

	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords.toLowerCase();
	}

}
