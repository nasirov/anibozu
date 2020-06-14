package nasirov.yv.data.anime_pik.site;

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
public class AnimePikApiResponse {

	@JsonProperty(value = "releases")
	private List<AnimePikTitle> titles;
}
