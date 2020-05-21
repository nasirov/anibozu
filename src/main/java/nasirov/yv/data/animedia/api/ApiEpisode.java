package nasirov.yv.data.animedia.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEpisode {

	@JsonProperty(value = "name")
	private String episodeName;
}
