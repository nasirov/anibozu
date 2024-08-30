package nasirov.yv.anibozu.dto.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

/**
 * @author Nasirov Yuriy
 */
public record AnimeList(@NotEmpty List<Anime> animeList) {

	@Builder
	public record Anime(String name, int nextEpisode, int maxEpisodes, String posterUrl, String malUrl, List<EpisodeInfo> episodes) {}
}
