package nasirov.yv.http.fallback.fandub.anilibria;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.fandub.anilibria.AnilibriaFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnilibriaFeignClientFallbackFactory implements FallbackFactory<AnilibriaFeignClient> {

	@Override
	public AnilibriaFeignClient create(Throwable cause) {
		return url -> {
			log.error("AnilibriaFeignClient fallback during call /{} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
