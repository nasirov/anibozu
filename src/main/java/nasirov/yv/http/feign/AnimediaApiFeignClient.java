package nasirov.yv.http.feign;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.api.AnimediaApiResponse;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.AnimediaApiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-api-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimediaApiFeignClientFallbackFactory.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, deflate, br"})
public interface AnimediaApiFeignClient {

	@GetMapping(value = "/api/anime-list/{part}", produces = "application/json; charset=utf-8")
	Set<AnimediaSearchListTitle> getAnimediaSearchList(@PathVariable String part);

	@GetMapping(value = "/api/mobile-anime/{animeId}", produces = "application/json; charset=utf-8")
	AnimediaApiResponse getTitleInfo(@PathVariable String animeId);

	@GetMapping(value = "/api/mobile-anime/{animeId}/{dataList}", produces = "application/json; charset=utf-8")
	AnimediaApiResponse getDataListInfo(@PathVariable String animeId, @PathVariable String dataList);
}
