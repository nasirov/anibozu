package nasirov.yv.ab.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

/**
 * @author Nasirov Yuriy
 */
@Builder
public record EpisodeInfo(@NotNull String fandubSource, @NotNull String link, @NotNull String name, @NotNull List<String> extra) {}
