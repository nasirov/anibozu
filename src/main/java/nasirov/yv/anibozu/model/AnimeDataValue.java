package nasirov.yv.anibozu.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public record AnimeDataValue(@NotNull List<AnimeEpisodeData> episodes) implements Serializable {}
