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
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.urls")
public class UrlsNames {

	@NotNull
	private AnimediaUrls animediaUrls;

	@NotNull
	private MALUrls malUrls;

	@NotNull
	private NineAnimeUrls nineAnimeUrls;

	@NotNull
	private AnidubUrls anidubUrls;

	@Data
	@Validated
	public static class AnimediaUrls {

		@NotBlank
		private String onlineAnimediaTv;
	}

	@Data
	@Validated
	public static class MALUrls {

		@NotBlank
		private String myAnimeListNet;

		@NotBlank
		private String cdnMyAnimeListNet;
	}

	@Data
	@Validated
	public static class NineAnimeUrls {

		@NotBlank
		private String nineAnimeTo;
	}

	@Data
	@Validated
	public static class AnidubUrls {

		@NotBlank
		private String anidubSiteUrl;
	}
}
