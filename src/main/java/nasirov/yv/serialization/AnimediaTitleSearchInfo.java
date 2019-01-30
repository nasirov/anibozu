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
//
//	public String getTitle() {
//		return title;
//	}
//
	public void setTitle(String title) {
		this.title = title.toLowerCase();
	}
//
//	public String getKeywords() {
//		return keywords;
//	}
//
	public void setKeywords(String keywords) {
		this.keywords = keywords.toLowerCase();
	}
//
//	public String getUrl() {
//		return url;
//	}
//
//	public void setUrl(String url) {
//		this.url = url;
//	}
//
//	public String getPosterUrl() {
//		return posterUrl;
//	}
//
//	public void setPosterUrl(String posterUrl) {
//		this.posterUrl = posterUrl;
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//		AnimediaTitleSearchInfo that = (AnimediaTitleSearchInfo) o;
//		if (!title.equals(that.title)) return false;
//		if (!keywords.equals(that.keywords)) return false;
//		if (!url.equals(that.url)) return false;
//		return posterUrl.equals(that.posterUrl);
//	}
//
//	@Override
//	public int hashCode() {
//		int result = title.hashCode();
//		result = 31 * result + keywords.hashCode();
//		result = 31 * result + url.hashCode();
//		result = 31 * result + posterUrl.hashCode();
//		return result;
//	}
//
//	@Override
//	public String toString() {
//		return "AnimediaTitleSearchInfo{" +
//				"title='" + title + '\'' +
//				", keywords='" + keywords + '\'' +
//				", url='" + url + '\'' +
//				", posterUrl='" + posterUrl + '\'' +
//				'}';
//	}
}
