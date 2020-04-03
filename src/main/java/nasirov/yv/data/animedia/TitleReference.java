package nasirov.yv.data.animedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.github.GitHubResource;

/**
 * Dto adapter between MAL and Animedia
 * <p>
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleReference extends GitHubResource {

	/**
	 * URL on Animedia
	 */
	private String urlOnAnimedia;

	/**
	 * Data list number on Animedia
	 */
	private String dataListOnAnimedia;

	/**
	 * Animed ID on Animedia
	 */
	private String animeIdOnAnimedia;

	/**
	 * Min range episodes
	 */
	private String minOnAnimedia;

	/**
	 * Max range episodes
	 */
	private String maxOnAnimedia;

	/**
	 * Current max episode
	 */
	private String currentMaxOnAnimedia;

	/**
	 * Episodes range from min to max and contains joined episodes In order to create correct episode number for watch, we must check this episodes
	 * range for joined episodes because if it is joined than episode number in a final url will constant for all joined episodes availability For
	 * example, 215-216 (https://online.animedia.tv/ajax/episodes/9649/2/undefined) Final url for both episodes(1 video)
	 * https://online.animedia.tv/anime/one-piece-van-pis-tv/2/215
	 * <p>
	 * Not null only if regular reference contains joined episodes
	 */
	private List<String> episodesRangeOnAnimedia;

	/**
	 * Title on MAL
	 */
	private String titleNameOnMAL;

	/**
	 * Animed id in MAL db
	 */
	private Integer titleIdOnMAL;

	/**
	 * Min range episodes on MAL
	 */
	private String minOnMAL;

	/**
	 * Min range episodes on MAL
	 */
	private String maxOnMAL;
}
