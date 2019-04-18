package nasirov.yv.serialization;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
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
	 * First episode on Animedia
	 */
	private String firstEpisode;

	/**
	 * Title on MAL
	 */
	private String titleOnMAL;

	/**
	 * Min range episodes
	 */
	private String minConcretizedEpisodeOnAnimedia;

	/**
	 * Max range episodes
	 */
	private String maxConcretizedEpisodeOnAnimedia;

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

	/**
	 * Min range episodes on MAL
	 */
	private String minConcretizedEpisodeOnMAL;

	/**
	 * Min range episodes on MAL
	 */
	private String maxConcretizedEpisodeOnMAL;

	public AnimediaMALTitleReferences(AnimediaMALTitleReferences animediaMALTitleReference) {
		this.url = animediaMALTitleReference.getUrl();
		this.dataList = animediaMALTitleReference.getDataList();
		this.firstEpisode = animediaMALTitleReference.getFirstEpisode();
		this.titleOnMAL = animediaMALTitleReference.getTitleOnMAL();
		this.minConcretizedEpisodeOnAnimedia = animediaMALTitleReference.getMinConcretizedEpisodeOnAnimedia();
		this.maxConcretizedEpisodeOnAnimedia = animediaMALTitleReference.getMaxConcretizedEpisodeOnAnimedia();
		this.currentMax = animediaMALTitleReference.getCurrentMax();
		this.posterUrl = animediaMALTitleReference.getPosterUrl();
		this.finalUrl = animediaMALTitleReference.getFinalUrl();
		this.episodeNumberForWatch = animediaMALTitleReference.getEpisodeNumberForWatch();
		this.minConcretizedEpisodeOnMAL = animediaMALTitleReference.getMinConcretizedEpisodeOnMAL();
		this.maxConcretizedEpisodeOnMAL = animediaMALTitleReference.getMaxConcretizedEpisodeOnMAL();
	}
}
