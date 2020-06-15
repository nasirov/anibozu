package nasirov.yv.data.fandub.anime_pik.api;

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
public class TitleName {

	@JsonProperty(value = "EN")
	private List<String> enNames;
}
