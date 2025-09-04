package nasirov.yv.anibozu.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnimeEpisodeData(@NotNull String site, @NotNull String siteName, @NotNull String type, @NotNull String source, @NotNull String link,
															 @NotNull String name) {}
