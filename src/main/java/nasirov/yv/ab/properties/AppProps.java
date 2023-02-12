package nasirov.yv.ab.properties;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
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
@ConfigurationProperties(prefix = "application")
public class AppProps {

	@NotNull
	private CacheProps cacheProps;

	@NotNull
	private Set<FandubSource> enabledFandubSources;

	@NotNull
	private Set<FandubSource> ignoreNextEpisodeForWatch;

	@NotNull
	private MalProps malProps;

	public void setEnabledFandubSources(Set<FandubSource> enabledFandubSources) {
		this.enabledFandubSources = enabledFandubSources.stream()
				.sorted(Comparator.comparing(FandubSource::name))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
