package nasirov.yv.data.fandub.anilibria.site;

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
public class AnilibriaSiteTitle extends GitHubResource {

	@JsonProperty(value = "url")
	private String url;

	@JsonProperty(value = "fullName")
	private String fullName;

	@JsonProperty(value = "ruName")
	private String ruName;

	@JsonProperty(value = "enName")
	private String enName;
}
