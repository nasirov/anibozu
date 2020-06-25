package nasirov.yv.http.fallback.fandub.jisedai;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.fandub.jisedai.JisedaiFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class JisedaiFeignClientFallbackFactory implements FallbackFactory<JisedaiFeignClient> {

	@Override
	public JisedaiFeignClient create(Throwable cause) {
		return url -> {
			log.error("JisedaiFeignClient fallback during call {} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
