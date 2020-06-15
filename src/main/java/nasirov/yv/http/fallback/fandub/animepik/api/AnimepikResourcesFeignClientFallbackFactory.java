package nasirov.yv.http.fallback.fandub.animepik.api;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anime_pik.api.AnimepikEpisode;
import nasirov.yv.http.feign.fandub.animepik.api.AnimepikResourcesFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimepikResourcesFeignClientFallbackFactory implements FallbackFactory<AnimepikResourcesFeignClient> {

	@Override
	public AnimepikResourcesFeignClient create(Throwable cause) {
		return titleId -> {
			log.error("AnimePikResourcesFeignClient fallback during call /{}.txt | Cause message [{}]", titleId, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private List<AnimepikEpisode> buildSafeResponse() {
		return Collections.emptyList();
	}
}
