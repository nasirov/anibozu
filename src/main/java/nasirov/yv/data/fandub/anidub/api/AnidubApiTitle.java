package nasirov.yv.data.fandub.anidub.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nasirov.yv.data.github.GitHubResource;

/**
 * Created by nasirov.yv
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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
}
