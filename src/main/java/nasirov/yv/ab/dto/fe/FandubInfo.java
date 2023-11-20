package nasirov.yv.ab.dto.fe;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.constant.FandubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FandubInfo {

	@NotNull
	private FandubSource fandubSource;

	@NotNull
	private String fandubSourceCanonicalName;

	@NotNull
	private String episodeUrl;

	@NotNull
	private String episodeName;

	@NotNull
	private List<String> types;
}
