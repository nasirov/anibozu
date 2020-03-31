package nasirov.yv.http.feign;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.GitHubFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "github-feign-client", configuration = FeignClientConfig.class, fallbackFactory = GitHubFeignClientFallbackFactory.class)
@RequestMapping(value = "/nasirov/anime-checker-resources/master", headers = {"Accept-Encoding=gzip, br"})
public interface GitHubFeignClient {

	@GetMapping(value = "/{resourceName}", produces = "text/plain; charset=utf-8")
	String getResource(@RequestHeader(AUTHORIZATION) String token, @PathVariable String resourceName);
}
