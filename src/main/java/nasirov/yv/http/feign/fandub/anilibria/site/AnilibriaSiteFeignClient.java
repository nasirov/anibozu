package nasirov.yv.http.feign.fandub.anilibria.site;

import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.anilibria.site.AnilibriaSiteFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "anilibria-site-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnilibriaSiteFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface AnilibriaSiteFeignClient {

	@GetMapping(value = "/{url}", produces = "text/html; charset=UTF-8")
	String getTitlePage(@PathVariable(name = "url") String url);
}
