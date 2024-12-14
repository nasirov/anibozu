package nasirov.yv.anibozu.model;

import java.io.Serializable;
import lombok.Builder;

@Builder
public record AnimeDataKey(int malId, int episodeId) implements Serializable {}
