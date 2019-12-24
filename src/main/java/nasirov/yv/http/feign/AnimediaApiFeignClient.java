package nasirov.yv.http.feign;

import nasirov.yv.data.animedia.api.AnimediaApiResponse;
import nasirov.yv.http.config.FeignClientConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-api-feign-client", configuration = FeignClientConfig.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, deflate, br"})
public interface AnimediaApiFeignClient {

	@Cacheable(value = "animediaApi", key = "#animeId")
	@GetMapping(value = "/api/mobile-anime/{animeId}", produces = "application/json; charset=utf-8")
	ResponseEntity<AnimediaApiResponse> getTitleInfo(@PathVariable String animeId);

	@Cacheable(value = "animediaApi", key = "T(java.lang.String).valueOf(T(java.util.Objects).hash(#animeId, #dataList))")
	@GetMapping(value = "/api/mobile-anime/{animeId}/{dataList}", produces = "application/json; charset=utf-8")
	ResponseEntity<AnimediaApiResponse> getDataListInfo(@PathVariable String animeId, @PathVariable String dataList);

	@GetMapping(value = "/api/mobile-episode-new", produces = "application/json; charset=utf-8")
	ResponseEntity<AnimediaApiResponse> getNewEpisodes();
}
