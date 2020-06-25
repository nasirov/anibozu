package nasirov.yv.data.fandub.animedia;

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
public class AnimediaEpisode {

	@JsonProperty(value = "title")
	private String episodeName;
}
