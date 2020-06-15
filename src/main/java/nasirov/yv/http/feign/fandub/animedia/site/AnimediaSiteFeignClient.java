package nasirov.yv.http.feign.fandub.animedia.site;

import java.util.List;
import nasirov.yv.data.fandub.animedia.site.SiteEpisode;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.animedia.site.AnimediaSiteFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-site-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimediaSiteFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface AnimediaSiteFeignClient {

	@GetMapping(value = "/ajax/search_result_search_page_2/P{offset}search&limit={limit}", produces = "text/html; charset=UTF-8")
	String getAnimediaSearchList(@PathVariable("offset") int offset, @PathVariable("limit") int limit);

	@GetMapping(value = "/{animeUrl}", produces = "text/html; charset=UTF-8")
	String getAnimePage(@PathVariable("animeUrl") String animeUrl);

	@GetMapping(value = "/embeds/playlist-j.txt/{animeId}/{dataList}", produces = "text/html; charset=UTF-8")
	List<SiteEpisode> getEpisodes(@PathVariable("animeId") String animeId, @PathVariable("dataList") String dataList);
}