package nasirov.yv.anibozu.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

public record AnimeList(@NotEmpty List<Anime> list) {

	@Builder
	public record Anime(String name, int nextEpisode, int maxEpisodes, String posterUrl, String malUrl, boolean airing, int dub, int sub,
											@NotNull List<Episode> episodes) {}

	@Builder
	public record Episode(@NotNull String site, @NotNull String siteName, @NotNull String type, @NotNull String source, @NotNull String link,
												@NotNull String name) {}
}
