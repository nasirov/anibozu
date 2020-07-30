package nasirov.yv.data.properties;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import nasirov.yv.fandub.dto.constant.FanDubSource;
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

	@NotEmpty
	private Map<FanDubSource, String> urls;
}
