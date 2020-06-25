package nasirov.yv.http.fallback.fandub.animepik;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.fandub.anime_pik.AnimepikResponse;
import nasirov.yv.http.feign.fandub.animepik.AnimepikFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimepikFeignClientFallbackFactory implements FallbackFactory<AnimepikFeignClient> {

	@Override
	public AnimepikFeignClient create(Throwable cause) {
		return id -> {
			log.error("AnimepikFeignClient fallback during call /api/anime/?format=json&last={} | Cause message [{}]", id, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private AnimepikResponse buildSafeResponse() {
		return new AnimepikResponse(Collections.emptyList());
	}
}
