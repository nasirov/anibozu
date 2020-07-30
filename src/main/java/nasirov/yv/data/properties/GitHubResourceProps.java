package nasirov.yv.data.properties;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.github")
public class GitHubResourceProps {

	@NotEmpty
	private Map<FanDubSource, String> resourcesNames;
}
