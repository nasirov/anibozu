package nasirov.yv.http.feign;

import nasirov.yv.data.anidub.api.response.AnidubApiSearchListResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleEpisodesResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleFandubSourceTypesResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleFandubSourcesResponse;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.AnidubApiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "anidub-api-feign-client", configuration = FeignClientConfig.class, fallbackFactory = AnidubApiFeignClientFallbackFactory.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, br", "User-Agent=okhttp/4.1.0"}, produces = "application/json;charset=UTF-8")
public interface AnidubApiFeignClient {

	@GetMapping(value = "/filter/{page}")
	AnidubApiSearchListResponse getAnidubSearchList(@PathVariable int page, @RequestParam(required = false) Integer status);

	@GetMapping(value = "/episode/{titleId}")
	AnidubApiTitleFandubSourceTypesResponse getAvailableFandubs(@PathVariable int titleId);

	@GetMapping(value = "/episode/{titleId}/{fandubSourceId}")
	AnidubApiTitleFandubSourcesResponse getFandubEpisodesSources(@PathVariable int titleId, @PathVariable int fandubSourceId);

	@GetMapping(value = "/episode/{titleId}/{fandubSourceId}/{sourceId}")
	AnidubApiTitleEpisodesResponse getTitleEpisodes(@PathVariable int titleId, @PathVariable int fandubSourceId, @PathVariable int sourceId);
}
