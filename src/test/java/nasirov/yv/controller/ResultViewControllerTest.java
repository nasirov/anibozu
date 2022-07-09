package nasirov.yv.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nasirov.yv.AbstractTest;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String[] VALID_FANDUBS = {"ANIDUB", "ANILIBRIA"};

	@Test
	void shouldReturnResultView() {
		//given
		mockExternalMalServiceResponse(buildMalServiceResponseDto(Lists.newArrayList(new MalTitle()), ""));
		//when
		ResponseSpec result = call(MAL_USERNAME, Collections.emptyMap(), VALID_FANDUBS);
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
				.map(x -> call(x, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE), VALID_FANDUBS))
				.collect(Collectors.toList());
		//then
		result.forEach(x -> x.expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-4xx-error-view.html")));
	}

	@Test
	void shouldReturn400ForInvalidFandubSources() {
		//given
		String[] invalidFandubSourceArray = {"animedia", "nineanime", ""};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidFandubSourceArray)
				.map(x -> call(MAL_USERNAME, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE), x))
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
		ResponseSpec result = call(MAL_USERNAME, Collections.emptyMap(), VALID_FANDUBS);
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
		mockHttpRequestServiceException();
		//when
		ResponseSpec result = call(MAL_USERNAME, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE),
				VALID_FANDUBS);
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-5xx-error-view.html"));
	}

	private ResponseSpec call(String username, Map<String, String> headers, String... fanDubSources) {
		RequestHeadersSpec<?> spec = webTestClient.get()
				.uri(x -> x.path(RESULT_VIEW_PATH)
						.queryParam("username", username)
						.queryParam("fanDubSources", fanDubSources)
						.build());
		headers.forEach(spec::header);
		return spec.exchange();
	}

	private ResponseSpec callNotFoundResource() {
		return webTestClient.get().uri("/unknown").header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE).exchange();
	}

	private void mockHttpRequestServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + MAL_USERNAME + "&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}
}