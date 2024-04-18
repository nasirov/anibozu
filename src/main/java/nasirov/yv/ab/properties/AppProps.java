package nasirov.yv.ab.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
@ConfigurationProperties(prefix = "application")
public class AppProps {

	@NotNull
	private Mal mal;

	@NotNull
	private Security security;

	@Data
	public static class Mal {

		@NotBlank
		private String url;

		@NotNull
		private Integer limit;
	}

	@Data
	public static class Security {

		@NotNull
		private Admin admin;

		@Data
		public static class Admin {

			@NotBlank
			private String username;

			@NotBlank
			private String password;

			@NotEmpty
			private List<String> roles;
		}
	}
}
