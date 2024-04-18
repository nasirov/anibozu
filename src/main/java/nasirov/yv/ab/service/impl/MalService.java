package nasirov.yv.ab.service.impl;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.exception.MalException;
import nasirov.yv.ab.exception.UnexpectedCallingException;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.MalAnimeFilterI;
import nasirov.yv.ab.service.MalAnimeFormatterI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import nasirov.yv.starter.common.dto.mal.WatchingStatus;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MalService implements MalServiceI {

	private static final ResponseEntity<List<MalAnime>> ANIME_LIST_FALLBACK = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());

	private static final String ERROR_MESSAGE_PREFIX = "Sorry, ";

	private static final String ERROR_MESSAGE_SUFFIX = " Please, try again later.";

	private final HttpRequestServiceI httpRequestService;

	private final MalAnimeFilterI malAnimeFilter;

	private final MalAnimeFormatterI malAnimeFormatter;

	private final AppProps appProps;

	private String animeListUrl;

	@PostConstruct
	public void init() {
		this.animeListUrl = this.appProps.getMal().getUrl() + "/animelist/{username}/load.json?offset=0&status={statusCode}";
	}

	@Override
	public Mono<List<MalAnime>> getAnimeList(String username) {
		return httpRequestService.performHttpRequest(buildAnimeListRequest(username))
				.doOnNext(x -> validateResponse(x.getStatusCode(), username))
				.mapNotNull(ResponseEntity::getBody)
				.map(x -> x.stream().limit(appProps.getMal().getLimit()).filter(malAnimeFilter::filter).map(malAnimeFormatter::format).toList())
				.doOnNext(x -> validateAnimeList(x, username))
				.doOnSubscribe(x -> log.info("Getting anime for [{}]", username))
				.doOnSuccess(x -> log.info("Got [{}] anime for [{}]", x.size(), username));
	}

	private HttpRequestServiceDto<ResponseEntity<List<MalAnime>>> buildAnimeListRequest(String username) {
		return HttpRequestServiceDto.<ResponseEntity<List<MalAnime>>>builder()
				.url(animeListUrl)
				.urlVariables(List.of(username, WatchingStatus.WATCHING.getCode()))
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

	private void validateResponse(HttpStatusCode responseStatus, String username) {
		if (HttpStatus.BAD_REQUEST.equals(responseStatus)) {
			throw new MalException(username + "'s anime list is private or does not exist.");
		} else if (HttpStatus.FORBIDDEN.equals(responseStatus) || HttpStatus.TOO_MANY_REQUESTS.equals(responseStatus)
							 || HttpStatus.METHOD_NOT_ALLOWED.equals(responseStatus)) {
			throw new MalException(ERROR_MESSAGE_PREFIX + username + ", but MAL has restricted our access to it." + ERROR_MESSAGE_SUFFIX);
		} else if (HttpStatus.SERVICE_UNAVAILABLE.equals(responseStatus)) {
			throw new MalException(ERROR_MESSAGE_PREFIX + username + ", but MAL is being unavailable now." + ERROR_MESSAGE_SUFFIX);
		} else {
			if (!HttpStatus.OK.equals(responseStatus)) {
				throw new UnexpectedCallingException();
			}
		}
	}

	private void validateAnimeList(List<MalAnime> anime, String username) {
		if (CollectionUtils.isEmpty(anime)) {
			throw new MalException(username + "'s anime list is empty or does not contain actual watching anime.");
		}
	}
}
