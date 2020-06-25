package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.github.resources")
public class GitHubResourceProps {

	@NotBlank
	private String animediaTitles;

	@NotBlank
	private String anidubTitles;

	@NotBlank
	private String jisedaiTitles;

	@NotBlank
	private String animepikTitles;

	@NotBlank
	private String anilibriaTitles;
}
