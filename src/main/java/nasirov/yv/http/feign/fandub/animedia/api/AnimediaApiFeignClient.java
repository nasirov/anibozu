package nasirov.yv.http.feign.fandub.animedia.api;

import java.util.Set;
import nasirov.yv.data.animedia.api.DataListInfoResponse;
import nasirov.yv.data.animedia.api.SearchListTitle;
import nasirov.yv.data.animedia.api.TitleInfoResponse;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.animedia.api.AnimediaApiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "animedia-api-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimediaApiFeignClientFallbackFactory.class)
@RequestMapping(headers = {"Accept-Encoding=gzip, br"})
public interface AnimediaApiFeignClient {

	@GetMapping(value = "/api/anime-list/{part}", produces = "application/json; charset=utf-8")
	Set<SearchListTitle> getAnimediaSearchList(@PathVariable String part);

	@GetMapping(value = "/api/mobile-anime/{animeId}", produces = "application/json; charset=utf-8")
	TitleInfoResponse getTitleInfo(@PathVariable String animeId);

	@GetMapping(value = "/api/mobile-anime/{animeId}/{dataList}", produces = "application/json; charset=utf-8")
	DataListInfoResponse getDataListInfo(@PathVariable String animeId, @PathVariable String dataList);
}
