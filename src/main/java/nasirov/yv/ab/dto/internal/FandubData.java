package nasirov.yv.ab.dto.internal;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisode;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FandubData {

	private Map<FandubKey, Map<Integer, List<FandubEpisode>>> episodes;
}
