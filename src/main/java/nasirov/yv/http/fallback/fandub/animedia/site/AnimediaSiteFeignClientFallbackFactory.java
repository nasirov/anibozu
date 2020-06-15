package nasirov.yv.http.fallback.fandub.animedia.site;

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.site.SiteEpisode;
import nasirov.yv.http.feign.fandub.animedia.site.AnimediaSiteFeignClient;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Configuration
public class AnimediaSiteFeignClientFallbackFactory implements FallbackFactory<AnimediaSiteFeignClient> {

	@Override
	public AnimediaSiteFeignClient create(Throwable cause) {
		return new AnimediaSiteFeignClient() {
			@Override
			public String getAnimediaSearchList(int offset, int limit) {
				log.error("AnimediaApiFeignClient fallback during call /ajax/search_result_search_page_2/P{}search&limit={} | Cause message [{}]",
						offset,
						limit,
						cause.getMessage());
				return buildSafeBody();
			}
			@Override
			public String getAnimePage(String animeUrl) {
				log.error("AnimediaApiFeignClient fallback during call /{} | Cause message [{}]", animeUrl, cause.getMessage());
				return buildSafeBody();
			}
			@Override
			public List<SiteEpisode> getEpisodes(String animeId, String dataList) {
				log.error("AnimediaApiFeignClient fallback during call /embeds/playlist-j.txt/{}/{} | Cause message [{}]",
						animeId,
						dataList,
						cause.getMessage());
				return Collections.emptyList();
			}
		};
	}

	private String buildSafeBody() {
		return "";
	}
}
