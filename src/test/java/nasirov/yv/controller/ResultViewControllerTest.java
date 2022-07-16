package nasirov.yv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.InputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.utils.CommonTitleTestFactory;
import nasirov.yv.utils.IOUtils;
import nasirov.yv.utils.MalTitleTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	@Test
	void shouldReturnResultViewWithAllKindsOfTitles() {
		//given
		Set<FandubSource> fandubSources = getEnabledFandubSources();
		MalTitle regularTitle = MalTitleTestFactory.buildRegularMalTitle();
		MalTitle concretizedTitle = MalTitleTestFactory.buildConcretizedMalTitle();
		MalTitle notFoundOnFandubTitle = MalTitleTestFactory.buildNotFoundOnFandubMalTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(
				Lists.newArrayList(regularTitle, concretizedTitle, notFoundOnFandubTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		Map<Integer, Map<FandubSource, List<CommonTitle>>> orderedMalTitles = new LinkedHashMap<>();
		orderedMalTitles.put(regularTitle.getId(), CommonTitleTestFactory.buildRegularCommonTitles(fandubSources));
		orderedMalTitles.put(concretizedTitle.getId(), CommonTitleTestFactory.buildConcretizedCommonTitles(fandubSources));
		orderedMalTitles.put(notFoundOnFandubTitle.getId(),
				CommonTitleTestFactory.buildNotFoundOnFandubCommonTitles(fandubSources));
		mockExternalFandubTitlesServiceResponse(orderedMalTitles);
		//when
		ResponseSpec result = call(MAL_USERNAME, Collections.emptyMap());
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-result-view.html"));
	}

	@Test
	void shouldReturn400ForInvalidUsernames() {
		//given
		String[] invalidUsernameArray = {"", "moreThan16Charssss", "space between ", "@#!sd"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray)
				.map(x -> call(x, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)))
				.collect(Collectors.toList());
		//then
		result.forEach(x -> x.expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-4xx-error-view.html")));
	}

	@Test
	void shouldReturnErrorViewWithErrorMessage() {
		//given
		String errorMsg = "Foo Bar";
		mockExternalMalServiceResponse(buildMalServiceResponseDto(Collections.emptyList(), errorMsg));
		//when
		ResponseSpec result = call(MAL_USERNAME, Collections.emptyMap());
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-error-view.html"));
	}

	@Test
	void shouldReturn404ErrorView() {
		//given
		//when
		ResponseSpec result = callNotFoundResource();
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.NOT_FOUND)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-404-error-view.html"));
	}

	@Test
	void shouldReturn500ErrorView() {
		//given
		mockTitlesServiceException();
		//when
		ResponseSpec result = call(MAL_USERNAME, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE));
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-5xx-error-view.html"));
	}

	private ResponseSpec call(String username, Map<String, String> headers) {
		RequestHeadersSpec<?> spec = webTestClient.get()
				.uri(x -> x.path(RESULT_VIEW_PATH).queryParam("username", username).build());
		headers.forEach(spec::header);
		return spec.exchange();
	}

	private ResponseSpec callNotFoundResource() {
		return webTestClient.get().uri("/unknown").header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE).exchange();
	}

	private void mockExternalFandubTitlesServiceResponse(
			Map<Integer, Map<FandubSource, List<CommonTitle>>> commonTitlesForMalTitles) {
		doReturn(Mono.just(commonTitlesForMalTitles)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl().equals(externalServicesProps.getFandubTitlesServiceUrl() + "titles")));
	}

	private void mockTitlesServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(resultProcessingService).getResult(any(InputDto.class));
	}
}