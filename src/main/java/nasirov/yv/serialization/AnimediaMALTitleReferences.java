package nasirov.yv.serialization;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Class for anime references
 * Created by Хикка on 29.12.2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
	private String numberOfEpisodeForWatch;
	
	public AnimediaMALTitleReferences() {
	}
	
	public AnimediaMALTitleReferences(String url, String dataList, String firstEpisode, String titleOnMAL, String min, String max, String currentMax, String posterUrl, String finalUrl, String numberOfEpisodeForWatch) {
		this.url = url;
		this.dataList = dataList;
		this.firstEpisode = firstEpisode;
		this.titleOnMAL = titleOnMAL;
		this.min = min;
		this.max = max;
		this.currentMax = currentMax;
		this.posterUrl = posterUrl;
		this.finalUrl = finalUrl;
		this.numberOfEpisodeForWatch = numberOfEpisodeForWatch;
	}
	
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
		this.numberOfEpisodeForWatch = animediaMALTitleReference.getNumberOfEpisodeForWatch();
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDataList() {
		return dataList;
	}
	
	public void setDataList(String dataList) {
		this.dataList = dataList;
	}
	
	public String getFirstEpisode() {
		return firstEpisode;
	}
	
	public void setFirstEpisode(String firstEpisode) {
		this.firstEpisode = firstEpisode;
	}
	
	public String getTitleOnMAL() {
		return titleOnMAL;
	}
	
	public void setTitleOnMAL(String titleOnMAL) {
		this.titleOnMAL = titleOnMAL;
	}
	
	public String getMin() {
		return min;
	}
	
	public void setMin(String min) {
		this.min = min;
	}
	
	public String getMax() {
		return max;
	}
	
	public void setMax(String max) {
		this.max = max;
	}
	
	public String getCurrentMax() {
		return currentMax;
	}
	
	public void setCurrentMax(String currentMax) {
		this.currentMax = currentMax;
	}
	
	public String getPosterUrl() {
		return posterUrl;
	}
	
	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}
	
	public String getFinalUrl() {
		return finalUrl;
	}
	
	public void setFinalUrl(String finalUrl) {
		this.finalUrl = finalUrl;
	}
	
	public String getNumberOfEpisodeForWatch() {
		return numberOfEpisodeForWatch;
	}
	
	public void setNumberOfEpisodeForWatch(String numberOfEpisodeForWatch) {
		this.numberOfEpisodeForWatch = numberOfEpisodeForWatch;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AnimediaMALTitleReferences that = (AnimediaMALTitleReferences) o;
		if (!url.equals(that.url)) return false;
		if (!dataList.equals(that.dataList)) return false;
		if (!firstEpisode.equals(that.firstEpisode)) return false;
		if (!titleOnMAL.equals(that.titleOnMAL)) return false;
		if (min != null ? !min.equals(that.min) : that.min != null) return false;
		if (max != null ? !max.equals(that.max) : that.max != null) return false;
		if (currentMax != null ? !currentMax.equals(that.currentMax) : that.currentMax != null) return false;
		if (posterUrl != null ? !posterUrl.equals(that.posterUrl) : that.posterUrl != null) return false;
		if (finalUrl != null ? !finalUrl.equals(that.finalUrl) : that.finalUrl != null) return false;
		return numberOfEpisodeForWatch != null ? numberOfEpisodeForWatch.equals(that.numberOfEpisodeForWatch) : that.numberOfEpisodeForWatch == null;
	}
	
	@Override
	public int hashCode() {
		int result = url.hashCode();
		result = 31 * result + dataList.hashCode();
		result = 31 * result + firstEpisode.hashCode();
		result = 31 * result + titleOnMAL.hashCode();
		result = 31 * result + (min != null ? min.hashCode() : 0);
		result = 31 * result + (max != null ? max.hashCode() : 0);
		result = 31 * result + (currentMax != null ? currentMax.hashCode() : 0);
		result = 31 * result + (posterUrl != null ? posterUrl.hashCode() : 0);
		result = 31 * result + (finalUrl != null ? finalUrl.hashCode() : 0);
		result = 31 * result + (numberOfEpisodeForWatch != null ? numberOfEpisodeForWatch.hashCode() : 0);
		return result;
	}
	
	@Override
	public String toString() {
		return "AnimediaMALTitleReferences{" +
				"url='" + url + '\'' +
				", dataList='" + dataList + '\'' +
				", firstEpisode='" + firstEpisode + '\'' +
				", titleOnMAL='" + titleOnMAL + '\'' +
				", min='" + min + '\'' +
				", max='" + max + '\'' +
				", currentMax='" + currentMax + '\'' +
				", posterUrl='" + posterUrl + '\'' +
				", finalUrl='" + finalUrl + '\'' +
				", numberOfEpisodeForWatch='" + numberOfEpisodeForWatch + '\'' +
				'}';
	}
}
