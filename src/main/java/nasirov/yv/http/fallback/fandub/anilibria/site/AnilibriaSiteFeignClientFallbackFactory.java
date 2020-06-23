package nasirov.yv.http.fallback.fandub.anilibria.site;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.fandub.anilibria.site.AnilibriaSiteFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnilibriaSiteFeignClientFallbackFactory implements FallbackFactory<AnilibriaSiteFeignClient> {

	@Override
	public AnilibriaSiteFeignClient create(Throwable cause) {
		return url -> {
			log.error("AnimediaApiFeignClient fallback during call /{} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
