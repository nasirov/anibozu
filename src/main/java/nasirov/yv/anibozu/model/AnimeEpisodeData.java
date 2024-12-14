package nasirov.yv.anibozu.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record AnimeEpisodeData(@NotNull String animeSite, @NotNull String link, @NotNull String name, @NotEmpty List<String> extra) {}
