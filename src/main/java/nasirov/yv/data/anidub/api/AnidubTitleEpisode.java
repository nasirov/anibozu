package nasirov.yv.data.anidub.api;

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
public class AnidubTitleEpisode {

	@JsonProperty(value = "name")
	private String name;

	@JsonProperty(value = "url")
	private String url;
}
