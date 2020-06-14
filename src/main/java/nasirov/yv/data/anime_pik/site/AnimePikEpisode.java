package nasirov.yv.data.anime_pik.site;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimePikEpisode {

	@JsonProperty(value = "title")
	private String name;
}
