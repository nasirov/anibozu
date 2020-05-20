package nasirov.yv.http.feign;

import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.JisedaiSiteFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "jisedai-site-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		JisedaiSiteFeignClientFallbackFactory.class)
@RequestMapping(headers = "Accept-Encoding=gzip, br")
public interface JisedaiSiteFeignClient {

	@GetMapping(value = "/{url}", produces = "text/html; charset=utf-8")
	String getTitlePage(@PathVariable String url);
}
