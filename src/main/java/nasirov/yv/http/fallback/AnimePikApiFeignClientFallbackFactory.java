package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anime_pik.site.AnimePikApiResponse;
import nasirov.yv.http.feign.AnimePikApiFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimePikApiFeignClientFallbackFactory implements FallbackFactory<AnimePikApiFeignClient> {

	@Override
	public AnimePikApiFeignClient create(Throwable cause) {
		return id -> {
			log.error("AnimePikApiFeignClient fallback during call /api/anime/?format=json&last={} | Cause message [{}]", id, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private AnimePikApiResponse buildSafeResponse() {
		return new AnimePikApiResponse(Collections.emptyList());
	}
}
