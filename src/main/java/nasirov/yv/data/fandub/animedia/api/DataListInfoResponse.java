package nasirov.yv.data.fandub.animedia.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class DataListInfoResponse {

	@JsonProperty(value = "response")
	private List<ApiEpisode> episodes;
}
