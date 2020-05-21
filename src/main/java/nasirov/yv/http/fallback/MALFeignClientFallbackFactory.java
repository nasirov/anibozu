package nasirov.yv.http.fallback;

import static nasirov.yv.data.mal.MALCategories.ANIME;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.google.common.collect.Lists;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MALSearchCategories;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.MALSearchTitleInfo;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.exception.mal.UnexpectedCallingException;
import nasirov.yv.http.feign.MALFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class MALFeignClientFallbackFactory implements FallbackFactory<MALFeignClient> {

	@Override
	public MALFeignClient create(Throwable cause) {
		int httpStatus = cause instanceof FeignException ? ((FeignException) cause).status() : 0;
		return new MALFeignClient() {
			@Override
			public ResponseEntity<String> getUserProfile(String username) {
				log.error("MALFeignClient fallback during call /profile/{} | Cause message [{}]", username, cause.getMessage());
				if (httpStatus == NOT_FOUND.value()) {
					return ResponseEntity.notFound()
							.build();
				}
				throw new UnexpectedCallingException(cause);
			}
			@Override
			public ResponseEntity<List<MalTitle>> getUserAnimeList(String username, int offset, int status) {
				log.error("MALFeignClient fallback during call /animelist/{}/load.json?offset={}&status={} | Cause message [{}]",
						username,
						offset,
						status,
						cause.getMessage());
				if (httpStatus == BAD_REQUEST.value()) {
					return ResponseEntity.badRequest()
							.build();
				}
				throw new UnexpectedCallingException(cause);
			}
			@Override
			public MALSearchResult searchTitleByName(String titleName) {
				log.error("MALFeignClient fallback during call /search/prefix.json?type=all&v=1&keyword={} | Cause message [{}]",
						titleName,
						cause.getMessage());
				if (httpStatus == BAD_REQUEST.value()) {
					return buildSafeMalSearchResult();
				}
				throw new UnexpectedCallingException(cause);
			}
		};
	}

	private MALSearchResult buildSafeMalSearchResult() {
		return new MALSearchResult(Lists.newArrayList(new MALSearchCategories(ANIME.getDescription(),
				Lists.newArrayList(new MALSearchTitleInfo(-1, "", "")))));
	}
}
