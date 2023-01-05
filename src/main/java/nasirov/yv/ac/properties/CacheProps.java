package nasirov.yv.ac.properties;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Nasirov Yuriy
 */
@Data
public class CacheProps {

	private boolean cacheOnStartup;

	@NotBlank
	private String githubCacheName;

	@NotBlank
	private String githubCacheKey;
}
