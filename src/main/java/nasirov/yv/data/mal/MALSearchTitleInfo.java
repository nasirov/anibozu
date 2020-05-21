package nasirov.yv.data.mal;

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
public class MALSearchTitleInfo {

	@JsonProperty("id")
	private Integer animeId;

	@JsonProperty("type")
	private String type;

	@JsonProperty("name")
	private String name;
}
