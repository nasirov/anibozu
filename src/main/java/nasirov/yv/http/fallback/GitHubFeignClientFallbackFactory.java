package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.GitHubFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class GitHubFeignClientFallbackFactory implements FallbackFactory<GitHubFeignClient> {

	@Override
	public GitHubFeignClient create(Throwable cause) {
		return x -> {
			log.error("GitHubFeignClient fallback during call /nasirov/anime-checker-resources/master/references.json | Cause message [{}]",
					cause.getMessage());
			return Collections.emptySet();
		};
	}
}
