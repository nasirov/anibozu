package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.api.AnimediaApiResponse;
import nasirov.yv.http.feign.AnimediaApiFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnimediaApiFeignClientFallbackFactory implements FallbackFactory<AnimediaApiFeignClient> {

	@Override
	public AnimediaApiFeignClient create(Throwable cause) {
		return new AnimediaApiFeignClient() {
			@Override
			public Set<AnimediaSearchListTitle> getAnimediaSearchList(String part) {
				log.error("AnimediaApiFeignClient fallback during call /api/anime-list/{}\n{}", part, cause.getMessage());
				return Collections.emptySet();
			}
			@Override
			public AnimediaApiResponse getTitleInfo(String animeId) {
				log.error("AnimediaApiFeignClient fallback during call /api/mobile-anime/{}\n{}", animeId, cause.getMessage());
				return buildSafeResponseEntity();
			}
			@Override
			public AnimediaApiResponse getDataListInfo(String animeId, String dataList) {
				log.error("AnimediaApiFeignClient fallback during call /api/mobile-anime/{}/{}\n{}", animeId, dataList, cause.getMessage());
				return buildSafeResponseEntity();
			}
		};
	}

	private AnimediaApiResponse buildSafeResponseEntity() {
		return AnimediaApiResponse.builder()
				.response(Collections.emptyList())
				.build();
	}
}
