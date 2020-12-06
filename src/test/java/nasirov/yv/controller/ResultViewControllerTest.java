package nasirov.yv.controller;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public class ResultViewControllerTest extends AbstractTest {

	private static final String RESULT_VIEW_PATH = "/result";

	private static final String[] VALID_FANDUBS = {"ANIMEDIA", "NINEANIME"};

	@Test
	public void shouldReturnResultView() {
		//given
		mockMalService(buildMalServiceResponseDto(Lists.newArrayList(new MalTitle()), ""));
		//when
		ResponseSpec result = call(TEST_ACC_FOR_DEV, Collections.emptyMap(), VALID_FANDUBS);
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-result-view.html"));
	}

	@Test
	public void shouldReturn400ForInvalidUsernames() {
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
	public void shouldReturn400ForInvalidFandubSources() {
		//given
		String[] invalidFandubSourceArray = {"animedia", "nineanime", ""};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidFandubSourceArray)
				.map(x -> call(TEST_ACC_FOR_DEV, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE), x))
				.collect(Collectors.toList());
		//then
		result.forEach(x -> x.expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-4xx-error-view.html")));
	}

	@Test
	public void shouldReturnErrorViewWithErrorMessage() {
		//given
		String errorMsg = "Foo Bar";
		mockMalService(buildMalServiceResponseDto(Collections.emptyList(), errorMsg));
		//when
		ResponseSpec result = call(TEST_ACC_FOR_DEV, Collections.emptyMap(), VALID_FANDUBS);
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-error-view.html"));
	}

	@Test
	public void shouldReturn404ErrorView() {
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
	public void shouldReturn500ErrorView() {
		//given
		mockMalServiceException();
		//when
		ResponseSpec result = call(TEST_ACC_FOR_DEV, Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE), VALID_FANDUBS);
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:view/test-5xx-error-view.html"));
	}

	private void mockMalService(MalServiceResponseDto malServiceResponseDto) {
		doReturn(Mono.just(malServiceResponseDto)).when(malService)
				.getUserWatchingTitles(new UserInputDto(TEST_ACC_FOR_DEV, Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME)));
	}

	private void mockMalServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(malService)
				.getUserWatchingTitles(new UserInputDto(TEST_ACC_FOR_DEV, Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME)));
	}

	private MalServiceResponseDto buildMalServiceResponseDto(List<MalTitle> malTitles, String errorMessage) {
		return MalServiceResponseDto.builder()
				.username(TEST_ACC_FOR_DEV)
				.malTitles(malTitles)
				.errorMessage(errorMessage)
				.build();
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
		return webTestClient.get()
				.uri("/unknown")
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
				.exchange();
	}
}