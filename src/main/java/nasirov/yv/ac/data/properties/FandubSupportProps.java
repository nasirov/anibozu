package nasirov.yv.ac.data.properties;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import nasirov.yv.starter.common.constant.FandubSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nasirov Yuriy
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.fandub-support")
public class FandubSupportProps {

	@NotEmpty
	private Set<FandubSource> enabled;

	public void setEnabled(Set<FandubSource> enabled) {
		this.enabled = enabled.stream()
				.sorted(Comparator.comparing(FandubSource::name))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
