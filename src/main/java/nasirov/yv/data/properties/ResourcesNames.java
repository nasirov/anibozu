package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
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
@ConfigurationProperties(prefix = "application.logo")
public class ResourcesNames {

	@NotBlank
	private String name;
}
