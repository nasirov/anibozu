package nasirov.yv.data.animedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Anime information for search on animedia Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimediaSearchListTitle {

	/**
	 * Animed ID on Animedia
	 */
	private String animeId;

	/**
	 * Anime URL on Animedia
	 */
	private String url;
}
