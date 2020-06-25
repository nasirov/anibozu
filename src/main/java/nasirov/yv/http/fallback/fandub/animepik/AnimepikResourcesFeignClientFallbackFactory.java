package nasirov.yv.http.fallback.fandub.animepik;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.fandub.anime_pik.AnimepikEpisode;
import nasirov.yv.http.feign.fandub.animepik.AnimepikResourcesFeignClient;
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
			log.error("AnimepikResourcesFeignClient fallback during call /{}.txt | Cause message [{}]", titleId, cause.getMessage());
			return buildSafeResponse();
		};
	}

	private List<AnimepikEpisode> buildSafeResponse() {
		return Collections.emptyList();
	}
}
