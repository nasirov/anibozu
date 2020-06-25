package nasirov.yv.http.feign.fandub.anidub;

import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.fandub.anidub.AnidubFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "anidub-feign-client", configuration = FeignClientConfig.class, fallbackFactory = AnidubFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface AnidubFeignClient {

	@GetMapping("/{url}")
	String getTitlePage(@PathVariable String url);
}
