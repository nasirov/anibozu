package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Хикка on 19.01.2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
