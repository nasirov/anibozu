package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.services")
@Configuration
@Validated
@Data
public class ServicesAvailability {

	@NotBlank
	private String resourcesLoaderEnabled;

	@NotBlank
	private String applicationLogoPrinterEnabled;

	public boolean getResourcesLoaderEnabled() {
		return Boolean.parseBoolean(this.resourcesLoaderEnabled);
	}

	public boolean getApplicationLogoPrinterEnabled() {
		return Boolean.parseBoolean(this.applicationLogoPrinterEnabled);
	}

}
