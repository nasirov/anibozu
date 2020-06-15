package nasirov.yv.data.fandub.animedia.api;

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
public class DataList {

	@JsonProperty(value = "seasonId")
	private String dataListId;
}
