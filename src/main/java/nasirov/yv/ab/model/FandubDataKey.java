package nasirov.yv.ab.model;

import java.io.Serializable;
import lombok.Builder;

/**
 * @author Nasirov Yuriy
 */
@Builder
public record FandubDataKey(int malId, int episodeId) implements Serializable {}
