package nasirov.yv.http.feign;

import nasirov.yv.data.anime_pik.api.AnimePikApiResponse;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.AnimePikApiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "anime-pik-api-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimePikApiFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface AnimePikApiFeignClient {

	@GetMapping("/api/anime/?format=json")
	AnimePikApiResponse getAnimePikSearchList(@RequestParam(value = "last") int id);
}
