package nasirov.yv.ab.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Nasirov Yuriy
 */
public record FandubDataValue(@NotNull List<FandubEpisodeData> episodes) implements Serializable {}
