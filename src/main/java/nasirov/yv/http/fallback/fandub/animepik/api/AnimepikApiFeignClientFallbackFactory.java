package nasirov.yv.http.fallback.fandub.animepik.api;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anime_pik.api.AnimepikApiResponse;
import nasirov.yv.http.feign.fandub.animepik.api.AnimepikApiFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimepikApiFeignClientFallbackFactory implements FallbackFactory<AnimepikApiFeignClient> {

	@Override
	public AnimepikApiFeignClient create(Throwable cause) {
		return id -> {
			log.error("AnimePikApiFeignClient fallback during call /api/anime/?format=json&last={} | Cause message [{}]", id, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private AnimepikApiResponse buildSafeResponse() {
		return new AnimepikApiResponse(Collections.emptyList());
	}
}
