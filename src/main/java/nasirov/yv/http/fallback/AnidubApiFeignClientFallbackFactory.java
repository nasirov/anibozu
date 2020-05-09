package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.api.response.AnidubApiSearchListResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleEpisodesResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleFandubSourceTypesResponse;
import nasirov.yv.data.anidub.api.response.AnidubApiTitleFandubSourcesResponse;
import nasirov.yv.http.feign.AnidubApiFeignClient;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnidubApiFeignClientFallbackFactory implements FallbackFactory<AnidubApiFeignClient> {

	@Override
	public AnidubApiFeignClient create(Throwable cause) {
		return new AnidubApiFeignClient() {
			@Override
			public AnidubApiSearchListResponse getAnidubSearchList(int page, Integer status) {
				log.error("AnidubApiFeignClient fallback during call /filter/{}?status={} | Cause message [{}]", page, status, cause.getMessage());
				return AnidubApiSearchListResponse.builder()
						.titles(Collections.emptyList())
						.build();
			}
			@Override
			public AnidubApiTitleFandubSourceTypesResponse getAvailableFandubs(int titleId) {
				log.error("AnidubApiFeignClient fallback during call /episode/{} | Cause message [{}]", titleId, cause.getMessage());
				return AnidubApiTitleFandubSourceTypesResponse.builder()
						.types(Collections.emptyList())
						.build();
			}
			@Override
			public AnidubApiTitleFandubSourcesResponse getFandubEpisodesSources(int titleId, int fandubSourceId) {
				log.error("AnidubApiFeignClient fallback during call /episode/{}/{} | Cause message [{}]", titleId, fandubSourceId, cause.getMessage());
				return AnidubApiTitleFandubSourcesResponse.builder()
						.sources(Collections.emptyList())
						.build();
			}
			@Override
			public AnidubApiTitleEpisodesResponse getTitleEpisodes(int titleId, int fandubSourceId, int sourceId) {
				log.error("AnidubApiFeignClient fallback during call /episode/{}/{}/{} | Cause message [{}]",
						titleId,
						fandubSourceId,
						sourceId,
						cause.getMessage());
				return AnidubApiTitleEpisodesResponse.builder()
						.episodes(Collections.emptyList())
						.build();
			}
		};
	}
}
