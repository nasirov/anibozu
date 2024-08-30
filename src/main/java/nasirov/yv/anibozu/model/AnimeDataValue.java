package nasirov.yv.anibozu.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Nasirov Yuriy
 */
public record AnimeDataValue(@NotNull List<AnimeEpisodeData> episodes) implements Serializable {}
