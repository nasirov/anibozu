package nasirov.yv.http.fallback.fandub.anidub;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.fandub.anidub.AnidubFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnidubFeignClientFallbackFactory implements FallbackFactory<AnidubFeignClient> {

	@Override
	public AnidubFeignClient create(Throwable cause) {
		return url -> {
			log.error("AnidubFeignClient fallback during call /{} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
