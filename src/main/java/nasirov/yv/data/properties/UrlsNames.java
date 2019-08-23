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

	@NotNull
	private GitHubUrls gitHubUrls;

	@Data
	public static class AnimediaUrls {

		@NotBlank
		private String onlineAnimediaTv;

		@NotBlank
		private String onlineAnimediaAnimeList;

		@NotBlank
		private String onlineAnimediaAnimeEpisodesList;

		@NotBlank
		private String onlineAnimediaAnimeEpisodesPostfix;
	}

	@Data
	public static class MALUrls {

		@NotBlank
		private String myAnimeListNet;

		@NotBlank
		private String cdnMyAnimeListNet;
	}

	@Data
	public static class GitHubUrls {

		@NotBlank
		private String rawGithubusercontentComAnimediaSearchList;

		@NotBlank
		private String rawGithubusercontentComReferences;
	}
}
