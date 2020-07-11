package nasirov.yv.data.fandub.animedia;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nasirov.yv.data.github.GitHubResource;

/**
 * Dto adapter between MAL and Animedia
 * <p>
 * Created by nasirov.yv
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AnimediaTitle extends GitHubResource {

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
	 * Not null only if a regular title contains joined episodes
	 */
	private List<String> episodesRangeOnAnimedia;

	/**
	 * Title on MAL
	 */
	private String titleNameOnMAL;

	/**
	 * Min range episodes on MAL
	 */
	private String minOnMAL;

	/**
	 * Min range episodes on MAL
	 */
	private String maxOnMAL;
}
