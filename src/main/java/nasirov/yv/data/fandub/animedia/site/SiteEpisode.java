package nasirov.yv.data.fandub.animedia.site;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteEpisode {

	@JsonProperty(value = "title")
	private String episodeName;
}
