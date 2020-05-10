package nasirov.yv.data.anidub.site;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnidubSiteTitle implements GitHubResource {

	@JsonProperty(value = "url")
	private String url;

	@JsonProperty(value = "titleIdOnMal")
	private Integer titleIdOnMal;
}
