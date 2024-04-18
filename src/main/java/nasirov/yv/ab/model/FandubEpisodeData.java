package nasirov.yv.ab.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

/**
 * @author Nasirov Yuriy
 */
@Builder
public record FandubEpisodeData(@NotNull String fandubSource, @NotNull String fandubSourceCanonicalName, @NotNull String episodeUrl,
																@NotNull String episodeName, @NotEmpty List<String> types) {}
