package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */

@ConfigurationProperties(prefix = "application.urls")
@Configuration
@Validated
@Data
public class UrlsNames {

	@NotNull
	private AnimediaUrls animediaUrls;

	@NotNull
	private MALUrls malUrls;

	@Data
	public static class AnimediaUrls {

		@NotBlank
		private String onlineAnimediaTv;
	}

	@Data
	public static class MALUrls {

		@NotBlank
		private String myAnimeListNet;

		@NotBlank
		private String cdnMyAnimeListNet;
	}
}
