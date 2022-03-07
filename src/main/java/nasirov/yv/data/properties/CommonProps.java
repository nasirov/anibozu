package nasirov.yv.data.properties;

import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nasirov Yuriy
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("application.common")
public class CommonProps {

	@NotNull
	private Map<FanDubSource, Boolean> enableBuildUrlInRuntime;
}