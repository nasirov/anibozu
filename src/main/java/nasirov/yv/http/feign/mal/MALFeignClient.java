package nasirov.yv.http.feign.mal;

import java.util.List;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.mal.MALFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "mal-feign-client", configuration = FeignClientConfig.class, fallbackFactory = MALFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface MALFeignClient {

	@GetMapping(value = "/profile/{username}", produces = "text/html; charset=UTF-8")
	ResponseEntity<String> getUserProfile(@PathVariable String username);

	@GetMapping(value = "/animelist/{username}/load.json", produces = "application/json; charset=UTF-8")
	ResponseEntity<List<MalTitle>> getUserAnimeList(@PathVariable String username, @RequestParam int offset, @RequestParam int status);

	@GetMapping(value = "/search/prefix.json?type=all&v=1", produces = "application/json; charset=UTF-8")
	MALSearchResult searchTitleByName(@RequestParam("keyword") String titleName);
}
