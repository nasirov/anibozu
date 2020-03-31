package nasirov.yv.data.anidub.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.anidub.AnidubTitleFandubSource;

/**
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnidubApiTitleFandubSourceTypesResponse {

	@JsonProperty(value = "types")
	private List<AnidubTitleFandubSource> types;
}
