package nasirov.yv.data.properties;

import javax.validation.constraints.Min;
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
@ConfigurationProperties("application.sse")
public class SseProps {

	@Min(1)
	@NotNull
	private Integer timeoutInMin;
}
