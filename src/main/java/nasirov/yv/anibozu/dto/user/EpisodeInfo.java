package nasirov.yv.anibozu.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record EpisodeInfo(@NotNull String animeSite, @NotNull String link, @NotNull String name, @NotNull List<String> extra) {}
