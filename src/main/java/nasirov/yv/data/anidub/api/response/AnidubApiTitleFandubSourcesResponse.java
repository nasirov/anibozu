package nasirov.yv.data.anidub.api.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.anidub.api.AnidubTitleFandubSource;

/**
 * Created by nasirov.yv
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnidubApiTitleFandubSourcesResponse {

	@JsonProperty(value = "sources")
	private List<AnidubTitleFandubSource> sources;
}
