package nasirov.yv.anibozu.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;

@Builder
public record AnimeEpisodesData(int dub, int sub, @NotNull List<AnimeEpisodeData> list) implements Serializable {}
