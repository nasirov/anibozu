package nasirov.yv.data.properties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.animedia")
public class AnimediaProps {

	@Min(1)
	@NotNull
	private Integer searchListMaxSize;

	@Min(1)
	@NotNull
	private Integer requestLimit;
}
