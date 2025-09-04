package nasirov.yv.anibozu.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

public record AnimeList(@NotEmpty List<Anime> list) {

	@Builder
	public record Anime(String name, int nextEpisode, int maxEpisodes, String posterUrl, String malUrl, boolean airing,
											@NotNull Episodes episodes) {}
}
