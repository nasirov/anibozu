package nasirov.yv.data.properties;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("application.common")
public class CommonProps {

	@NotNull
	private Boolean enableBuildUrlInRuntime;
}