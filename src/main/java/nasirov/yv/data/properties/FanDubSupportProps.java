package nasirov.yv.data.properties;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nasirov Yuriy
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "application.fandub-support")
public class FanDubSupportProps {

	private Set<FanDubSource> disabledFandub = new LinkedHashSet<>();
}
