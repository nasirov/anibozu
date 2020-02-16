package nasirov.yv.http.feign;

import java.util.List;
import nasirov.yv.data.animedia.site.SiteEpisode;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.AnimediaSiteFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-site-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimediaSiteFeignClientFallbackFactory.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, br"})
public interface AnimediaSiteFeignClient {

	@GetMapping(value = "/ajax/search_result_search_page_2/P{offset}search&limit={limit}", produces = "text/html; charset=UTF-8")
	String getAnimediaSearchList(@PathVariable("offset") int offset, @PathVariable("limit") int limit);

	@GetMapping(value = "/{animeUrl}", produces = "text/html; charset=UTF-8")
	String getAnimePage(@PathVariable("animeUrl") String animeUrl);

	@GetMapping(value = "/embeds/playlist-j.txt/{animeId}/{dataList}", produces = "text/html; charset=UTF-8")
	List<SiteEpisode> getEpisodes(@PathVariable("animeId") String animeId, @PathVariable("dataList") String dataList);
}