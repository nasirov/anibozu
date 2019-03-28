package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Anime references
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimediaMALTitleReferences {

	/**
	 * URL on Animedia
	 */
	private String url;

	/**
	 * Data list number on Animedia
	 */
	private String dataList;

	/**
	 * First episode in Animedia
	 */
	private String firstEpisode;

	/**
	 * Title on MAL
	 */
	private String titleOnMAL;

	/**
	 * Min range episodes
	 */
	private String min;

	/**
	 * Max range episodes
	 */
	private String max;

	/**
	 * Current max episode
	 */
	private String currentMax;

	/**
	 * Poster URL
	 */
	private String posterUrl;

	/**
	 * URL for new episode
	 */
	private String finalUrl;

	/**
	 * Next episode for watch
	 */
	private String episodeNumberForWatch;

	public AnimediaMALTitleReferences(AnimediaMALTitleReferences animediaMALTitleReference) {
		this.url = animediaMALTitleReference.getUrl();
		this.dataList = animediaMALTitleReference.getDataList();
		this.firstEpisode = animediaMALTitleReference.getFirstEpisode();
		this.titleOnMAL = animediaMALTitleReference.getTitleOnMAL();
		this.min = animediaMALTitleReference.getMin();
		this.max = animediaMALTitleReference.getMax();
		this.currentMax = animediaMALTitleReference.getCurrentMax();
		this.posterUrl = animediaMALTitleReference.getPosterUrl();
		this.finalUrl = animediaMALTitleReference.getFinalUrl();
		this.episodeNumberForWatch = animediaMALTitleReference.getEpisodeNumberForWatch();
	}
}
