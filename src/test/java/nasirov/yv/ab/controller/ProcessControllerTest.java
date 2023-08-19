package nasirov.yv.ab.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import nasirov.yv.ab.AbstractTest;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.fe.Title;
import nasirov.yv.ab.utils.IOUtils;
import nasirov.yv.starter.common.dto.mal.WatchingStatus;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class ProcessControllerTest extends AbstractTest {

	private static final String MAL_USERNAME = "foobarbaz";

	private static final String ANIME_LIST_URL = "/animelist/" + MAL_USERNAME + "/load.json?offset=0&status=" + WatchingStatus.WATCHING.getCode();

	private static final String MAL_RESTRICTED_ACCESS =
			"Sorry, " + MAL_USERNAME + ", but MAL has restricted our access to it. Please, try again later.";

	@Test
	void shouldReturnProcessResultCacheIsEmpty() {
		//given
		mockGitHubResourcesService();
		stubAnimeListOk();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result);
		checkGithubCacheIsFilled();
	}

	@Test
	void shouldReturnProcessResultGithubCacheIsFilled() {
		//given
		fillGithubCache();
		stubAnimeListOk();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result);
	}

	@Test
	void shouldReturnErrorMalServiceException() {
		//given
		doThrow(new RuntimeException("MalService cause")).when(malService).getMalTitles(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorAnimeListPrivateOrDoesNotExist() {
		//given
		stubAnimeListError(HttpStatus.BAD_REQUEST);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, MAL_USERNAME + "'s anime list is private or does not exist.");
	}

	@Test
	void shouldReturnMalRestrictedAccessError403() {
		testWithMalAccessRestorer(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldReturnMalRestrictedAccessError429() {
		testWithMalAccessRestorer(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void shouldReturnErrorMalUnavailable() {
		//given
		stubAnimeListError(HttpStatus.SERVICE_UNAVAILABLE);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", but MAL is being unavailable now. Please, try again later.");
	}

	@Test
	void shouldReturnErrorUnexpectedCallingException() {
		//given
		stubAnimeListError(HttpStatus.INTERNAL_SERVER_ERROR);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorEmptyAnimeList() {
		//given
		stubHttpRequest(ANIME_LIST_URL, "mal/watching-titles-empty.json", HttpStatus.OK);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Not found actual watching titles for " + MAL_USERNAME);
	}

	@Test
	void shouldReturnErrorCommonTitlesServiceException() {
		//given
		stubAnimeListOk();
		doThrow(new RuntimeException("CommonTitlesService cause")).when(commonTitlesService).getCommonEpisodesMappedByKey();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturn400Error() {
		//given
		String[] invalidUsernameArray = {"moreThan16Charssss", "space between ", "@#!sd", "1"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray).map(this::call).toList();
		//then
		result.forEach(x -> checkResponse(x, HttpStatus.BAD_REQUEST, ProcessController.USERNAME_VALIDATION_MESSAGE));
	}

	@Test
	void shouldReturn500Error() {
		//given
		doThrow(new RuntimeException("ProcessService cause")).when(processService).process(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	private void stubAnimeListOk() {
		stubHttpRequest(ANIME_LIST_URL, "mal/watching-titles-offset-0.json", HttpStatus.OK);
	}

	private void stubAnimeListError(HttpStatus httpStatus) {
		stubHttpRequest(ANIME_LIST_URL, "mal/not-expected-response.json", httpStatus);
	}

	private void testWithMalAccessRestorer(HttpStatus httpStatus) {
		//given
		stubAnimeListError(httpStatus);
		doNothing().when(malAccessRestorerAsync).restoreMalAccessAsync();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, MAL_RESTRICTED_ACCESS);
		verify(malAccessRestorerAsync, times(1)).restoreMalAccessAsync();
	}

	private ResponseSpec call(String username) {
		RequestHeadersSpec<?> spec = webTestClient.get().uri(x -> x.pathSegment("process").pathSegment(username).build());
		return spec.exchange();
	}

	private void checkResponse(ResponseSpec result) {
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(new ParameterizedTypeReference<ProcessResult>() {})
				.value(new CustomTypeSafeMatcher<>("unordered fields should be equal") {
					@Override
					protected boolean matchesSafely(ProcessResult actual) {
						List<Title> expectedTitles = getExpectedTitles();
						List<Title> actualTitles = actual.getTitles();
						Map<String, Title> nameToTitle = actualTitles.stream().collect(Collectors.toMap(Title::getName, Function.identity()));
						return StringUtils.isBlank(actual.getErrorMessage()) && Objects.equals(expectedTitles.size(), actualTitles.size())
								&& expectedTitles.stream().allMatch(x -> {
							Title actualTitle = nameToTitle.get(x.getName());
							return Objects.nonNull(actualTitle) && Objects.equals(x.getNextEpisodeNumber(), actualTitle.getNextEpisodeNumber()) && Objects.equals(
									x.getPosterUrl(), actualTitle.getPosterUrl()) && Objects.equals(x.getMalUrl(), actualTitle.getMalUrl())
									&& x.getFandubInfoList().size() == actualTitle.getFandubInfoList().size() && actualTitle.getFandubInfoList()
									.containsAll(x.getFandubInfoList());
						});
					}
				});
	}

	private void checkResponse(ResponseSpec result, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
		result.expectStatus()
				.isEqualTo(expectedHttpStatus)
				.expectBody(new ParameterizedTypeReference<ProcessResult>() {})
				.isEqualTo(new ProcessResult(expectedErrorMessage));
	}

	private List<Title> getExpectedTitles() {
		return IOUtils.unmarshalToListFromFile("classpath:__files/result/expected-titles.json", Title.class)
				.stream()
				.peek(x -> x.setMalUrl(appProps.getMalProps().getUrl() + x.getMalUrl()))
				.collect(Collectors.toList());
	}
}