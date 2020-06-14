package nasirov.yv.data.anime_pik.api;

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
public class AnimepikTitle extends GitHubResource {

	@JsonProperty(value = "id")
	private Integer id;

	@JsonProperty(value = "title")
	private TitleName titleName;

	@JsonProperty(value = "url")
	private String url;
}
