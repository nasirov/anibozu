package nasirov.yv.ab.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.exception.MalException;
import nasirov.yv.ab.exception.UnexpectedCallingException;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.MalAnimeServiceI;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import nasirov.yv.starter.common.dto.mal.WatchingStatus;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MalAnimeService implements MalAnimeServiceI {

	private static final ResponseEntity<List<MalAnime>> ANIME_LIST_FALLBACK = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());

	private static final String ERROR_MESSAGE_PREFIX = "Sorry, ";

	private static final String ERROR_MESSAGE_SUFFIX = " Please, try again later.";

	private final HttpRequestServiceI httpRequestService;

	private final AppProps appProps;

	@Override
	public Mono<List<MalAnime>> getAnimeList(String username) {
		return httpRequestService.performHttpRequest(buildAnimeListRequest(username))
				.doOnNext(x -> validateMalResponse(x.getStatusCode(), username))
				.mapNotNull(ResponseEntity::getBody)
				.doOnSubscribe(x -> log.info("Get anime for [{}]", username))
				.doOnSuccess(x -> log.info("Got [{}] anime for [{}]", x.size(), username));
	}

	private HttpRequestServiceDto<ResponseEntity<List<MalAnime>>> buildAnimeListRequest(String username) {
		return HttpRequestServiceDto.<ResponseEntity<List<MalAnime>>>builder()
				.url(appProps.getMalProps().getUrl() + "/animelist/" + username + "/load.json?offset=0&status=" + WatchingStatus.WATCHING.getCode())
				.clientResponseFunction(x -> {
					Mono<ResponseEntity<List<MalAnime>>> result;
					HttpStatusCode responseHttpStatus = x.statusCode();
					if (responseHttpStatus == HttpStatus.OK) {
						result = x.toEntity(new ParameterizedTypeReference<>() {});
					} else {
						result = Mono.just(ResponseEntity.status(responseHttpStatus).body(List.of()));
					}
					return result;
				})
				.fallback(ANIME_LIST_FALLBACK)
				.build();
	}

	private void validateMalResponse(HttpStatusCode responseStatus, String username) {
		if (HttpStatus.BAD_REQUEST.equals(responseStatus)) {
			throw new MalException(username + "'s anime list is private or does not exist.", HttpStatus.BAD_REQUEST);
		} else if (HttpStatus.FORBIDDEN.equals(responseStatus) || HttpStatus.TOO_MANY_REQUESTS.equals(responseStatus)
							 || HttpStatus.METHOD_NOT_ALLOWED.equals(responseStatus)) {
			throw new MalException(ERROR_MESSAGE_PREFIX + username + ", but MAL has restricted our access to it." + ERROR_MESSAGE_SUFFIX,
					HttpStatus.FORBIDDEN);
		} else if (HttpStatus.SERVICE_UNAVAILABLE.equals(responseStatus)) {
			throw new MalException(ERROR_MESSAGE_PREFIX + username + ", but MAL is being unavailable now." + ERROR_MESSAGE_SUFFIX,
					HttpStatus.SERVICE_UNAVAILABLE);
		} else {
			if (responseStatus != HttpStatus.OK) {
				throw new UnexpectedCallingException();
			}
		}
	}
}
