package nasirov.yv.http.fallback;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import feign.FeignException;
import feign.hystrix.FallbackFactory;
import java.util.List;
import nasirov.yv.data.mal.MALSearchResult;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.UnexpectedCallingException;
import nasirov.yv.http.feign.MALFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
public class MALFeignClientFallbackFactory implements FallbackFactory<MALFeignClient> {

	@Override
	public MALFeignClient create(Throwable cause) {
		int httpStatus = cause instanceof FeignException ? ((FeignException) cause).status() : 0;
		return new MALFeignClient() {
			@Override
			public ResponseEntity<String> getUserProfile(String username) {
				if (httpStatus == NOT_FOUND.value()) {
					return ResponseEntity.notFound()
							.build();
				}
				throw new UnexpectedCallingException(cause);
			}
			@Override
			public ResponseEntity<List<UserMALTitleInfo>> getUserAnimeList(String username, int offset, int status) {
				if (httpStatus == BAD_REQUEST.value()) {
					return ResponseEntity.badRequest()
							.build();
				}
				throw new UnexpectedCallingException(cause);
			}
			@Override
			public ResponseEntity<MALSearchResult> searchTitleByName(String titleName) {
				if (httpStatus == BAD_REQUEST.value()) {
					return ResponseEntity.badRequest()
							.build();
				}
				throw new UnexpectedCallingException(cause);
			}
		};
	}
}