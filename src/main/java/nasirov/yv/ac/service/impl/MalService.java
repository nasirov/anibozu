package nasirov.yv.ac.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ac.dto.mal.MalUserInfo;
import nasirov.yv.ac.exception.MalForbiddenException;
import nasirov.yv.ac.exception.MalUnavailableException;
import nasirov.yv.ac.exception.MalUserAccountNotFoundException;
import nasirov.yv.ac.exception.MalUserAnimeListAccessException;
import nasirov.yv.ac.exception.UnexpectedCallingException;
import nasirov.yv.ac.exception.WatchingTitlesNotFoundException;
import nasirov.yv.ac.properties.AppProps;
import nasirov.yv.ac.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.ac.service.MalServiceI;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Flux;
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

	private final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	private final AppProps appProps;

	@Override
	public Mono<MalUserInfo> getMalUserInfo(String username) {
		MalTitleWatchingStatus status = MalTitleWatchingStatus.WATCHING;
		return Mono.just(username)
				.flatMap(this::getUserProfile)
				.map(x -> getAmountOfTitles(status, x))
				.flatMap(x -> buildResult(username, status, x))
				.onErrorReturn(MalUserAccountNotFoundException.class::isInstance,
						buildErrorResponse("MAL account " + username + " is not found."))
				.onErrorReturn(MalForbiddenException.class::isInstance,
						buildErrorResponse(ERROR_MESSAGE_PREFIX + username + ", but MAL rejected our requests with status 403."))
				.onErrorReturn(MalUnavailableException.class::isInstance,
						buildErrorResponse(ERROR_MESSAGE_PREFIX + username + ", but MAL is unavailable now."))
				.onErrorReturn(UnexpectedCallingException.class::isInstance,
						buildErrorResponse(ERROR_MESSAGE_PREFIX + username + ", unexpected error has occurred."))
				.onErrorReturn(WatchingTitlesNotFoundException.class::isInstance,
						buildErrorResponse("Not found watching titles for " + username + " !"))
				.onErrorReturn(MalUserAnimeListAccessException.class::isInstance,
						buildErrorResponse(username + "'s anime list has private access!"))
				.doOnSubscribe(x -> log.debug("Trying to build MalUserInfo for [{}]", username))
				.doOnSuccess(x -> log.debug("Built MalUserInfo for [{}].", username));
	}

	private Mono<String> getUserProfile(String username) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.buildUserProfileDto(username))
				.doOnNext(x -> validateMalResponse(x.getStatusCode()))
				.mapNotNull(ResponseEntity::getBody);
	}

	private void validateMalResponse(HttpStatus responseStatus) {
		switch (responseStatus) {
			case NOT_FOUND -> throw new MalUserAccountNotFoundException();
			case BAD_REQUEST -> throw new MalUserAnimeListAccessException();
			case FORBIDDEN -> throw new MalForbiddenException();
			case SERVICE_UNAVAILABLE -> throw new MalUnavailableException();
			default -> {
				if (responseStatus != HttpStatus.OK) {
					throw new UnexpectedCallingException();
				}
			}
		}
	}

	private Integer getAmountOfTitles(MalTitleWatchingStatus status, String profile) {
		return Optional.ofNullable(Jsoup.parse(profile).selectFirst("li:has(a.anime." + status.getDescription() + ") >span"))
				.map(x -> x.ownText().replace(",", StringUtils.EMPTY))
				.filter(StringUtils::isNotBlank)
				.map(Integer::valueOf)
				.filter(x -> x > 0)
				.orElseThrow(WatchingTitlesNotFoundException::new);
	}

	private Mono<MalUserInfo> buildResult(String username, MalTitleWatchingStatus status, Integer amountOfTitles) {
		log.info("[{}] has [{}] {} titles.", username, amountOfTitles, status);
		return Flux.fromIterable(generateOffsets(amountOfTitles))
				.flatMap(x -> getPartOfTitles(x, username, status))
				.collectList()
				.map(x -> new MalUserInfo(mergeLists(x), StringUtils.EMPTY));
	}

	private List<Integer> generateOffsets(int amountOfTitles) {
		int offsetStep = appProps.getMalProps().getOffsetStep();
		return IntStream.iterate(0, x -> x < amountOfTitles, x -> x + offsetStep).boxed().toList();
	}

	private Mono<List<MalTitle>> getPartOfTitles(Integer currentOffset, String username, MalTitleWatchingStatus status) {
		return httpRequestService.performHttpRequest(
						httpRequestServiceDtoBuilder.buildPartOfTitlesDto(currentOffset, username, status))
				.doOnNext(x -> validateMalResponse(x.getStatusCode()))
				.mapNotNull(ResponseEntity::getBody);
	}

	private List<MalTitle> mergeLists(List<List<MalTitle>> malTitleLists) {
		return malTitleLists.stream()
				.flatMap(List::stream)
				.filter(this::isWatchingNotCompleted)
				.map(this::formatMalTitle)
				.toList();
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

	private MalUserInfo buildErrorResponse(String errorMessage) {
		return MalUserInfo.builder().malTitles(List.of()).errorMessage(errorMessage).build();
	}
}
