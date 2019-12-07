package nasirov.yv.http.feign;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.http.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-feign-client", configuration = FeignClientConfig.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, deflate, br"})
public interface AnimediaFeignClient {

	@GetMapping(produces = "text/html; charset=UTF-8")
	ResponseEntity<String> getAnimediaMainPage();

	@GetMapping(value = "/ajax/anime_list", produces = "text/javascript;charset=UTF-8")
	ResponseEntity<Set<AnimediaTitleSearchInfo>> getAnimediaSearchList();

	@GetMapping(value = "/{animeUrl}", produces = "text/html; charset=UTF-8")
	ResponseEntity<String> getAnimePageWithDataLists(@PathVariable String animeUrl);

	@GetMapping(value = "/ajax/episodes/{animeId}/{dataList}/undefined", produces = "text/html; charset=UTF-8")
	ResponseEntity<String> getDataListWithEpisodes(@PathVariable String animeId, @PathVariable String dataList);
}
