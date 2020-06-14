package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anime_pik.api.AnimePikEpisode;
import nasirov.yv.http.feign.AnimePikResourcesFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimePikResourcesFeignClientFallbackFactory implements FallbackFactory<AnimePikResourcesFeignClient> {

	@Override
	public AnimePikResourcesFeignClient create(Throwable cause) {
		return titleId -> {
			log.error("AnimePikResourcesFeignClient fallback during call /{}.txt | Cause message [{}]", titleId, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private List<AnimePikEpisode> buildSafeResponse() {
		return Collections.emptyList();
	}
}
