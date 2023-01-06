package nasirov.yv.ac.dto.fe;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import nasirov.yv.starter.common.constant.FandubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleDto {

	private TitleType type;

	private String nameOnMal;

	private String episodeNumberOnMal;

	private String posterUrlOnMal;

	private String animeUrlOnMal;

	@Singular("fandubToUrl")
	private Map<FandubSource, String> fandubToUrl;

	@Singular("fandubToEpisodeName")
	private Map<FandubSource, String> fandubToEpisodeName;
}