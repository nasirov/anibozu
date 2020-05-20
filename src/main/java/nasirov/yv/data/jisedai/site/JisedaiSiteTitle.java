package nasirov.yv.data.jisedai.site;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.github.GitHubResource;

/**
 * Created by nasirov.yv
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JisedaiSiteTitle implements GitHubResource {

	@JsonProperty(value = "id")
	private Integer id;

	@JsonProperty(value = "url")
	private String url;

	@JsonProperty(value = "titleIdOnMal")
	private Integer titleIdOnMal;
}
