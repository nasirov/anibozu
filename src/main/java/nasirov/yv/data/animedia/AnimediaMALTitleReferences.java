package nasirov.yv.data.animedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	/**
	 * Episodes range from min to max
	 * It can contain joined episodes
	 * In order to create correct episode number for watch, we must check this episodes range for joined episodes
	 * because if it is joined than episode number in a final url will constant for all joined episodes availability
	 * For example, 215-216 (https://online.animedia.tv/ajax/episodes/9649/2/undefined)
	 * Final url for both episodes(1 video) https://online.animedia.tv/anime/one-piece-van-pis-tv/2/215
	 */
	private List<String> episodesRange;

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
		this.episodesRange = animediaMALTitleReference.getEpisodesRange();
	}
}
