package nasirov.yv.data.fandub.animedia;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Anime information for search on Animedia
 * <p>
 * Created by nasirov.yv
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimediaSearchListTitle {

	/**
	 * Animed ID on Animedia
	 * <p>
	 * e.g 1234
	 */
	private String animeId;

	/**
	 * Anime URL on Animedia
	 * <p>
	 * e.g anime/some-anime
	 */
	private String url;

	/**
	 * Data Lists
	 * <p>
	 * e.g. [1,2,7]
	 */
	private List<String> dataLists;
}
