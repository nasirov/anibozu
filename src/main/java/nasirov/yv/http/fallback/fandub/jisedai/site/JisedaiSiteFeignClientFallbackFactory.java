package nasirov.yv.http.fallback.fandub.jisedai.site;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.feign.fandub.jisedai.site.JisedaiSiteFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class JisedaiSiteFeignClientFallbackFactory implements FallbackFactory<JisedaiSiteFeignClient> {

	@Override
	public JisedaiSiteFeignClient create(Throwable cause) {
		return url -> {
			log.error("JisedaiSiteFeignClient fallback during call {} | Cause message [{}]", url, cause.getMessage());
			return StringUtils.EMPTY;
		};
	}
}
