package nasirov.yv.http.feign.fandub.jisedai;

import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.jisedai.JisedaiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "jisedai-feign-client", configuration = FeignClientConfig.class, fallbackFactory = JisedaiFeignClientFallbackFactory.class)
@RequestMapping(headers = "Accept-Encoding=gzip, br")
public interface JisedaiFeignClient {

	@GetMapping(value = "/{url}", produces = "text/html; charset=utf-8")
	String getTitlePage(@PathVariable String url);
}
