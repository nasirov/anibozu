package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

	/**
	 * Anime id in the local list
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
