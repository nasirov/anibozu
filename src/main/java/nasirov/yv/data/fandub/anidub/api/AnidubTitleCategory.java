package nasirov.yv.data.fandub.anidub.api;

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
public class AnidubTitleCategory {

	@JsonProperty(value = "id")
	private Integer id;

	@JsonProperty(value = "name")
	private String name;
}
