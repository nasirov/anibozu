package nasirov.yv.data.properties;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.services")
@Configuration
@Validated
@Data
public class ServicesAvailability {

	@NotNull
	private Boolean resourcesLoaderEnabled;

	@NotNull
	private Boolean applicationLogoPrinterEnabled;

}
