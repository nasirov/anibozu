package nasirov.yv.ac.service.impl;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import nasirov.yv.ac.properties.AppProps;
import nasirov.yv.ac.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.ac.service.MalAccessRestorerI;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import nasirov.yv.starter.reactive.services.exception.RetryableResponseStatusCodeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class HttpRequestServiceDtoBuilder implements HttpRequestServiceDtoBuilderI {

	private final AppProps appProps;

	private final MalAccessRestorerI malAccessRestorer;

	@Override
	public HttpRequestServiceDto<ResponseEntity<String>> buildUserProfileDto(String username) {
		return HttpRequestServiceDto.<ResponseEntity<String>>builder()
				.url(appProps.getMalProps().getUrl() + "/profile/" + username)
				.responseHandler(clientResponse -> buildResponseHandler(clientResponse,
						x -> x.toEntity(new ParameterizedTypeReference<String>() {})))
				.fallback(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StringUtils.EMPTY))
				.build();
	}

	@Override
	public HttpRequestServiceDto<ResponseEntity<List<MalTitle>>> buildPartOfTitlesDto(Integer currentOffset, String username,
			MalTitleWatchingStatus status) {
		return HttpRequestServiceDto.<ResponseEntity<List<MalTitle>>>builder()
				.url(appProps.getMalProps().getUrl() + "/animelist/" + username + "/load.json?offset=" + currentOffset + "&status="
						+ status.getCode())
				.responseHandler(clientResponse -> buildResponseHandler(clientResponse, this::buildClientResponseFunction))
				.fallback(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of()))
				.build();
	}

	private Mono<ResponseEntity<List<MalTitle>>> buildClientResponseFunction(ClientResponse clientResponse) {
		HttpStatus statusCode = clientResponse.statusCode();
		return clientResponse.toEntityList(MalTitle.class).onErrorReturn(ResponseEntity.status(statusCode).body(List.of()));
	}

	private <T> Mono<T> buildResponseHandler(ClientResponse clientResponse,
			Function<ClientResponse, Mono<T>> clientResponseFunction) {
		Mono<T> result;
		int rawStatusCode = clientResponse.rawStatusCode();
		if (HttpStatus.FORBIDDEN.value() == rawStatusCode) {
			result = malAccessRestorer.restoreMalAccess().flatMap(malAccessRestored -> {
				if (malAccessRestored) {
					return clientResponseFunction.apply(clientResponse);
				} else {
					return Mono.error(new RetryableResponseStatusCodeException(
							"Received a response with retryable status code [" + rawStatusCode + "]."));
				}
			});
		} else {
			result = clientResponseFunction.apply(clientResponse);
		}
		return result;
	}
}
