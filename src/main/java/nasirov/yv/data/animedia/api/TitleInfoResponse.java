package nasirov.yv.data.animedia.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleInfoResponse {

	@JsonProperty(value = "response")
	private TitleInfo titleInfo;

	public void setTitleInfo(List<TitleInfo> titleInfo) {
		this.titleInfo = Iterables.get(titleInfo, 0);
	}
}