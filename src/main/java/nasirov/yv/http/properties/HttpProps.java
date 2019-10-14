package nasirov.yv.http.properties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
@ConfigurationProperties(prefix = "application.http")
public class HttpProps {

	@NotNull
	@Min(1000)
	@Max(5000)
	private Integer connectTimeout;

	@NotNull
	@Min(1000)
	@Max(5000)
	private Integer readTimeout;

	@NotNull
	@Min(1)
	@Max(5)
	private Integer maxAttempts;

	@NotNull
	@Min(1)
	@Max(5)
	private Integer maxWaitTime;

	@NotNull
	private Boolean followRedirects;
}
