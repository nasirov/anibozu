package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.AnidubSiteFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnidubSiteFeignClientFallbackFactory implements FallbackFactory<AnidubSiteFeignClient> {

	@Override
	public AnidubSiteFeignClient create(Throwable cause) {
		return url -> {
			log.error("AnidubSiteFeignClient fallback during call /{} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
