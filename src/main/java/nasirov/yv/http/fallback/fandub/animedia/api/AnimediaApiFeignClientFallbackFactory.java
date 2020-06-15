package nasirov.yv.http.fallback.fandub.animedia.api;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.fandub.animedia.api.DataListInfoResponse;
import nasirov.yv.data.fandub.animedia.api.SearchListTitle;
import nasirov.yv.data.fandub.animedia.api.TitleInfo;
import nasirov.yv.data.fandub.animedia.api.TitleInfoResponse;
import nasirov.yv.http.feign.fandub.animedia.api.AnimediaApiFeignClient;
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
			public Set<SearchListTitle> getAnimediaSearchList(String part) {
				log.error("AnimediaApiFeignClient fallback during call /api/anime-list/{} | Cause message [{}]", part, cause.getMessage());
				return Collections.emptySet();
			}
			@Override
			public TitleInfoResponse getTitleInfo(String animeId) {
				log.error("AnimediaApiFeignClient fallback during call /api/mobile-anime/{} | Cause message [{}]", animeId, cause.getMessage());
				return buildAnimediaApiTitleInfoResponse();
			}
			@Override
			public DataListInfoResponse getDataListInfo(String animeId, String dataList) {
				log.error("AnimediaApiFeignClient fallback during call /api/mobile-anime/{}/{} | Cause message [{}]", animeId, dataList, cause.getMessage());
				return buildAnimediaApiResponse();
			}
		};
	}

	private TitleInfoResponse buildAnimediaApiTitleInfoResponse() {
		return TitleInfoResponse.builder()
				.titleInfo(TitleInfo.builder()
						.dataLists(Collections.emptyList())
						.build())
				.build();
	}

	private DataListInfoResponse buildAnimediaApiResponse() {
		return DataListInfoResponse.builder()
				.episodes(Collections.emptyList())
				.build();
	}
}
