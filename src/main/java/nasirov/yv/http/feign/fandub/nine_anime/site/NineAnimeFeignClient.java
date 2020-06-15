package nasirov.yv.http.feign.fandub.nine_anime.site;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import nasirov.yv.data.fandub.nine_anime.SearchDto;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.nine_anime.site.NineAnimeFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "nine-anime-feign-client", configuration = FeignClientConfig.class, fallbackFactory = NineAnimeFeignClientFallbackFactory.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, br", "User-Agent=Mozilla"})
public interface NineAnimeFeignClient {

	@GetMapping(value = "/ajax/film/search", produces = APPLICATION_JSON_VALUE)
	SearchDto searchTitleByName(@RequestParam("keyword") String titleName);

	@GetMapping(value = "/ajax/film/servers/{dataId}", produces = APPLICATION_JSON_VALUE)
	SearchDto getTitleEpisodesInfo(@PathVariable("dataId") String dataId);
}
