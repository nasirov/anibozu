package nasirov.yv.data.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for github resources
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class GitHubResource {

	/**
	 * An anime id in MAL db
	 */
	@JsonProperty(value = "titleIdOnMal")
	private Integer titleIdOnMal;
}
