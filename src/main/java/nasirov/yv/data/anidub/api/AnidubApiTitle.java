package nasirov.yv.data.anidub.api;

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
public class AnidubApiTitle extends GitHubResource {

	@JsonProperty(value = "id")
	private Integer id;

	@JsonProperty(value = "status")
	private AnidubTitleStatus status;

	@JsonProperty(value = "category")
	private AnidubTitleCategory category;

	@JsonProperty(value = "title_original")
	private String originalName;

	@JsonProperty(value = "title_ru")
	private String ruName;

	@JsonProperty(value = "titleIdOnMAL")
	private Integer titleIdOnMal;
}
