package nasirov.yv.serialization;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Хикка on 19.01.2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anime {
	/**
	 * Anime id in local list
	 */
	private String id;
	
	/**
	 * Anime full url
	 * http://online.animedia.tv/anime/realnaya-devushka/1/1
	 */
	private String fullUrl;
	
	/**
	 * Anime root url
	 * anime/realnaya-devushka
	 */
	private String rootUrl;
	
	public Anime(String id, String fullUrl, String rootUrl) {
		this.id = id;
		this.fullUrl = fullUrl;
		this.rootUrl = rootUrl;
	}
	
	public Anime() {
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getFullUrl() {
		return fullUrl;
	}
	
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
	
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}
	
	public String getRootUrl() {
		return rootUrl;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Anime anime = (Anime) o;
		if (!id.equals(anime.id)) return false;
		if (!fullUrl.equals(anime.fullUrl)) return false;
		return rootUrl.equals(anime.rootUrl);
	}
	
	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + fullUrl.hashCode();
		result = 31 * result + rootUrl.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "Anime{" +
				"id=" + id +
				", fullUrl='" + fullUrl + '\'' +
				'}';
	}
}
