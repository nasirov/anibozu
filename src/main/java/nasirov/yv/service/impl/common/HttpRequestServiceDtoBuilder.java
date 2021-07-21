package nasirov.yv.service.impl.common;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikPlayer;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.selenium_service.SeleniumServiceRequestDto;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class HttpRequestServiceDtoBuilder implements HttpRequestServiceDtoBuilderI {

	private static final Set<Integer> RETRYABLE_STATUS_CODES = Sets.newHashSet(500, 502, 503, 504, 520, 524);

	private final FanDubProps fanDubProps;

	private final ExternalServicesProps externalServicesProps;

	@Override
	public HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status) {
		return buildDto(externalServicesProps.getMalServiceUrl() + "titles?username=" + username + "&status=" + status.name(),
				Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getMalServiceBasicAuth()),
				Collections.emptySet(),
				y -> y.bodyToMono(MalServiceResponseDto.class),
				MalServiceResponseDto.builder()
						.username(username)
						.malTitles(Collections.emptyList())
						.errorMessage(StringUtils.EMPTY)
						.build());
	}

	@Override
	public HttpRequestServiceDto<List<CommonTitle>> fandubTitlesService(FanDubSource fanDubSource, int malId, int malEpisodeId) {
		return buildDto(
				externalServicesProps.getFandubTitlesServiceUrl() + "titles?fanDubSource=" + fanDubSource.name() + "&malId=" + malId + "&malEpisodeId="
						+ malEpisodeId,
				Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getFandubTitlesServiceBasicAuth()),
				Collections.emptySet(),
				x -> x.bodyToMono(new ParameterizedTypeReference<List<CommonTitle>>() {
				}),
				Collections.emptyList());
	}

	@Override
	public HttpRequestServiceDto<String> seleniumService(SeleniumServiceRequestDto seleniumServiceRequestDto) {
		return buildDto(externalServicesProps.getSeleniumServiceUrl() + "content?url=" + seleniumServiceRequestDto.getUrl() + "&timeoutInSec="
						+ seleniumServiceRequestDto.getTimeoutInSec() + "&cssSelector=" + seleniumServiceRequestDto.getCssSelector(),
				Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getSeleniumServiceBasicAuth()),
				Collections.emptySet(),
				x -> x.bodyToMono(String.class),
				StringUtils.EMPTY);
	}

	@Override
	public HttpRequestServiceDto<String> anidub(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.ANIDUB);
	}

	@Override
	public HttpRequestServiceDto<String> anilibria(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.ANILIBRIA);
	}

	@Override
	public HttpRequestServiceDto<List<AnimediaEpisode>> animedia(CommonTitle commonTitle) {
		return buildDto(fanDubProps.getUrls()
						.get(FanDubSource.ANIMEDIA) + "embeds/playlist-j.txt/" + commonTitle.getId() + "/" + commonTitle.getDataList(),
				Collections.emptyMap(),
				RETRYABLE_STATUS_CODES,
				x -> x.bodyToMono(new ParameterizedTypeReference<List<AnimediaEpisode>>() {
				}),
				Collections.emptyList());
	}

	@Override
	public HttpRequestServiceDto<AnimepikTitleEpisodes> animepik(CommonTitle commonTitle) {
		return buildDto(fanDubProps.getUrls()
						.get(FanDubSource.ANIMEPIK) + "api/v1/" + commonTitle.getUrl(),
				Collections.emptyMap(),
				RETRYABLE_STATUS_CODES,
				x -> x.bodyToMono(AnimepikTitleEpisodes.class),
				AnimepikTitleEpisodes.builder()
						.animepikPlayer(AnimepikPlayer.builder()
								.episodes(Collections.emptyList())
								.build())
						.build());
	}

	@Override
	public HttpRequestServiceDto<String> jisedai(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.JISEDAI);
	}

	@Override
	public HttpRequestServiceDto<String> jutsu(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.JUTSU);
	}

	@Override
	public HttpRequestServiceDto<String> nineAnime(CommonTitle commonTitle) {
		return buildDto(fanDubProps.getUrls()
						.get(FanDubSource.NINEANIME) + "ajax/anime/servers?id=" + commonTitle.getDataId(),
				Collections.emptyMap(),
				RETRYABLE_STATUS_CODES,
				x -> x.bodyToMono(String.class),
				StringUtils.EMPTY);
	}

	@Override
	public HttpRequestServiceDto<String> shizaProject(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.SHIZAPROJECT);
	}

	@Override
	public HttpRequestServiceDto<String> sovetRomantica(CommonTitle commonTitle) {
		return buildDtowithStringResponse(commonTitle, FanDubSource.SOVETROMANTICA);
	}

	@Override
	public HttpRequestServiceDto<String> sovetRomantica(CommonTitle commonTitle, String cookie) {
		HttpRequestServiceDto<String> result = buildDtowithStringResponse(commonTitle, FanDubSource.SOVETROMANTICA);
		if (Objects.nonNull(cookie)) {
			result.setHeaders(Collections.singletonMap(HttpHeaders.COOKIE, cookie));
		}
		return result;
	}

	@Override
	public HttpRequestServiceDto<String> sovetRomanticaDdosGuard() {
		return buildDto(fanDubProps.getSovetRomanticaDdosGuardUrl() + "check.js",
				Collections.singletonMap(HttpHeaders.REFERER,
						fanDubProps.getUrls()
								.get(FanDubSource.SOVETROMANTICA)),
				Collections.emptySet(),
				x -> x.bodyToMono(String.class),
				StringUtils.EMPTY);
	}

	private <T> HttpRequestServiceDto<T> buildDto(String url, Map<String, String> headers, Set<Integer> retryableStatusCodes,
			Function<ClientResponse, Mono<T>> clientResponseFunction, T fallback) {
		return new HttpRequestServiceDto<>(url, HttpMethod.GET, headers, null, retryableStatusCodes, clientResponseFunction, fallback);
	}

	private HttpRequestServiceDto<String> buildDtowithStringResponse(CommonTitle commonTitle, FanDubSource fanDubSource) {
		return buildDto(fanDubProps.getUrls()
						.get(fanDubSource) + commonTitle.getUrl(),
				Collections.emptyMap(),
				RETRYABLE_STATUS_CODES,
				x -> x.bodyToMono(String.class),
				StringUtils.EMPTY);
	}
}
