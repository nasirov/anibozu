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
@ConfigurationProperties("application.cache")
public class CacheProps {

	@NotNull
	private ConfigurableCacheProps sse;

	@Data
	@Validated
	public static class ConfigurableCacheProps {

		@NotBlank
		private String name;

		@NotNull
		private Integer ttl;

		@NotNull
		private Integer maxEntityCount;
	}
}
