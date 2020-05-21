package nasirov.yv.data.nine_anime;

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
public class SearchDto {

	@JsonProperty("html")
	private String html;
}
