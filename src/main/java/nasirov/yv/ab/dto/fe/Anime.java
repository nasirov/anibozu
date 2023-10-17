package nasirov.yv.ab.dto.fe;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

	private String name;

	private String nextEpisode;

	private String maxEpisodes;

	private String posterUrl;

	private String malUrl;

	@Singular("fandubInfoList")
	private List<FandubInfo> fandubInfoList;
}
