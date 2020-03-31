package nasirov.yv.http.fallback;

import static org.apache.logging.log4j.util.Strings.EMPTY;

import feign.hystrix.FallbackFactory;
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
		return (token, resourceName) -> {
			log.error("GitHubFeignClient fallback during call /nasirov/anime-checker-resources/master/{} | Cause message [{}]", resourceName,
					cause.getMessage());
			return EMPTY;
		};
	}
}
