package nasirov.yv.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub_titles_service.FandubTitlesServiceRequestDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.util.MalUtils;
import nasirov.yv.utils.MalTitleTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author Nasirov Yuriy
 */
class HttpRequestServiceDtoBuilderTest extends AbstractTest {

	@Test
	void shouldBuildHttpRequestServiceDtoForMalService() {
		//given
		String url = externalServicesProps.getMalServiceUrl() + "titles?username=" + MAL_USERNAME + "&status=WATCHING";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION,
				externalServicesProps.getMalServiceBasicAuth());
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		MalServiceResponseDto fallback = MalServiceResponseDto.builder()
				.username(MAL_USERNAME)
				.malTitles(Collections.emptyList())
				.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
				.build();
		//when
		HttpRequestServiceDto<MalServiceResponseDto> result = httpRequestServiceDtoBuilder.malService(MAL_USERNAME,
				MalTitleWatchingStatus.WATCHING);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback, null);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForFandubTitlesService() {
		//given
		String url = externalServicesProps.getFandubTitlesServiceUrl() + "titles";
		HttpMethod method = HttpMethod.POST;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION,
				externalServicesProps.getFandubTitlesServiceBasicAuth());
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		FanDubSource fanDubSource = FanDubSource.ANIMEDIA;
		Set<FanDubSource> fanDubSources = Set.of(fanDubSource);
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		Map<Integer, Map<FanDubSource, List<CommonTitle>>> fallback = Map.of(regularTitle.getId(),
				Map.of(fanDubSource, Collections.emptyList()));
		FandubTitlesServiceRequestDto requestBody = FandubTitlesServiceRequestDto.builder()
				.fanDubSources(fanDubSources)
				.malIdToEpisode(Map.of(regularTitle.getId(), MalUtils.getNextEpisodeForWatch(regularTitle)))
				.build();
		//when
		HttpRequestServiceDto<Map<Integer, Map<FanDubSource, List<CommonTitle>>>> result =
				httpRequestServiceDtoBuilder.fandubTitlesService(
				fanDubSources, List.of(regularTitle));
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback, requestBody);
	}

	private <T> void checkResult(HttpRequestServiceDto<T> result, String url, HttpMethod method, Map<String, String> headers,
			Set<Integer> retryableStatusCodes, T fallback, Object requestBody) {
		assertEquals(url, result.getUrl());
		assertEquals(method, result.getMethod());
		assertEquals(headers, result.getHeaders());
		assertEquals(retryableStatusCodes, result.getRetryableStatusCodes());
		assertEquals(requestBody, result.getRequestBody());
		assertNotNull(result.getClientResponseFunction());
		assertEquals(fallback, result.getFallback());
		assertNotNull(result.getClientResponseFunction().apply(ClientResponse.create(HttpStatus.OK).build()));
	}
}