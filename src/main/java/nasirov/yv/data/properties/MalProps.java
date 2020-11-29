package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nasirov Yuriy
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.mal")
public class MalProps {

	@NotNull
	private Integer offsetStep;

	@NotBlank
	private String url;
}
