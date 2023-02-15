package nasirov.yv.ab.service.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.exception.MalException;
import nasirov.yv.ab.exception.UnexpectedCallingException;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MalService implements MalServiceI {

	private static final Pattern POSTER_URL_RESOLUTION_PATTERN = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");

	private static final Pattern S_VARIABLE_PATTERN = Pattern.compile("(\\?s=.+)");

	private static final String ERROR_MESSAGE_PREFIX = "Sorry, ";

	private final HttpRequestServiceI httpRequestService;

	private final AppProps appProps;

	@Override
	public Mono<List<MalTitle>> getMalTitles(String username) {
		return httpRequestService.performHttpRequest(buildWatchingTitlesRequest(username))
				.doOnNext(x -> validateMalResponse(x.getStatusCode(), username))
				.mapNotNull(ResponseEntity::getBody)
				.map(x -> filterTitles(x, username))
				.doOnSubscribe(x -> log.info("Getting titles for {}", username))
				.doOnSuccess(x -> log.info("Got {} titles for {}", x.size(), username));
	}

	private void validateMalResponse(HttpStatus responseStatus, String username) {
		switch (responseStatus) {
			case BAD_REQUEST ->
					throw new MalException(username + "'s anime list is private or does not exist.", HttpStatus.BAD_REQUEST);
			case FORBIDDEN -> throw new MalException(
					ERROR_MESSAGE_PREFIX + username + ", but MAL has restricted our access to it. Please, try again later.",
					HttpStatus.FORBIDDEN);
			case SERVICE_UNAVAILABLE -> throw new MalException(
					ERROR_MESSAGE_PREFIX + username + ", but MAL is being unavailable now. Please, try again later.",
					HttpStatus.SERVICE_UNAVAILABLE);
			default -> {
				if (responseStatus != HttpStatus.OK) {
					throw new UnexpectedCallingException();
				}
			}
		}
	}

	private HttpRequestServiceDto<ResponseEntity<List<MalTitle>>> buildWatchingTitlesRequest(String username) {
		return HttpRequestServiceDto.<ResponseEntity<List<MalTitle>>>builder()
				.url(appProps.getMalProps().getUrl() + "/animelist/" + username + "/load.json?offset=0&status="
						+ MalTitleWatchingStatus.WATCHING.getCode())
				.clientResponseFunction(x -> {
					Mono<ResponseEntity<List<MalTitle>>> result;
					HttpStatus responseHttpStatus = x.statusCode();
					if (responseHttpStatus == HttpStatus.OK) {
						result = x.toEntity(new ParameterizedTypeReference<>() {});
					} else {
						result = Mono.just(ResponseEntity.status(responseHttpStatus).body(List.of()));
					}
					return result;
				})
				.fallback(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of()))
				.build();
	}

	private List<MalTitle> filterTitles(List<MalTitle> malTitles, String username) {
		List<MalTitle> result = malTitles.stream().filter(this::isWatchingNotCompleted).map(this::formatMalTitle).toList();
		if (CollectionUtils.isEmpty(result)) {
			throw new MalException("Not found actual watching titles for " + username, HttpStatus.NOT_FOUND);
		}
		return result;
	}

	private boolean isWatchingNotCompleted(MalTitle malTitle) {
		Integer animeNumEpisodes = malTitle.getAnimeNumEpisodes();
		return animeNumEpisodes == 0 || malTitle.getNumWatchedEpisodes() < animeNumEpisodes;
	}

	private MalTitle formatMalTitle(MalTitle malTitle) {
		String changedPosterUrl = StringUtils.EMPTY;
		Matcher matcher = POSTER_URL_RESOLUTION_PATTERN.matcher(malTitle.getPosterUrl());
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll(StringUtils.EMPTY);
		}
		matcher = S_VARIABLE_PATTERN.matcher(changedPosterUrl);
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll(StringUtils.EMPTY);
		}
		if (StringUtils.isNotBlank(changedPosterUrl)) {
			malTitle.setPosterUrl(changedPosterUrl);
		}
		malTitle.setAnimeUrl(appProps.getMalProps().getUrl() + malTitle.getAnimeUrl());
		malTitle.setName(HtmlUtils.htmlUnescape(malTitle.getName()));
		return malTitle;
	}
}
