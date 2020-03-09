package nasirov.yv.data.properties;

import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;
import nasirov.yv.data.constants.FanDubSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.fandub")
public class FanDubProps {

	@NotNull
	private Set<FanDubSource> disabled;
}
