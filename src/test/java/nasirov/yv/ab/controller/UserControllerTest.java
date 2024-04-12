package nasirov.yv.ab.controller;

import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import nasirov.yv.ab.AbstractTest;
import nasirov.yv.ab.dto.fe.Anime;
import nasirov.yv.ab.dto.fe.AnimeListResponse;
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
class UserControllerTest extends AbstractTest {

	private static final String MAL_USERNAME = "foobarbaz";

	private static final String ANIME_LIST_URL = "/animelist/" + MAL_USERNAME + "/load.json?offset=0&status=" + WatchingStatus.WATCHING.getCode();

	private static final String MAL_RESTRICTED_ACCESS_ERROR_MESSAGE =
			"Sorry, " + MAL_USERNAME + ", but MAL has restricted our access to it. Please, try again later.";

	private static final String GENERIC_ERROR_MESSAGE = "Sorry, something went wrong. Please, try again later.";

	private static final String EMPTY_ANIME_LIST_ERROR_MESSAGE = "Not found actual watching anime! Please, try again later.";

	private static final String USERNAME_VALIDATION_MESSAGE =
			"Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, " + "underscores and dashes only)";

	@Test
	void shouldReturnResultCacheIsEmpty() {
		//given
		mockCompiledAnimeResourcesService();
		stubAnimeListOk();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result);
		checkGithubCacheIsFilled();
	}

	@Test
	void shouldReturnResultGithubCacheIsFilled() {
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
		doThrow(new RuntimeException("MalService cause")).when(malService).getAnimeList(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
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
		testWithRestrictedMalAccess(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldReturnMalRestrictedAccessError429() {
		testWithRestrictedMalAccess(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void shouldReturnMalRestrictedAccessError405() {
		testWithRestrictedMalAccess(HttpStatus.METHOD_NOT_ALLOWED);
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
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorEmptyAnimeList() {
		//given
		stubHttpRequest(ANIME_LIST_URL, "mal/watching_anime_empty.json", HttpStatus.OK);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, EMPTY_ANIME_LIST_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorFandubDataServiceException() {
		//given
		stubAnimeListOk();
		doThrow(new RuntimeException("FandubDataService cause")).when(fandubDataService).getFandubData();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturn400Error() {
		//given
		String[] invalidUsernameArray = {"moreThan16Charssss", "space between ", "@#!sd", "1"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray).map(this::call).toList();
		//then
		result.forEach(x -> checkResponse(x, HttpStatus.BAD_REQUEST, USERNAME_VALIDATION_MESSAGE));
	}

	@Test
	void shouldReturn500Error() {
		//given
		doThrow(new RuntimeException("UserService cause")).when(userService).getAnimeList(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
	}

	private void stubAnimeListOk() {
		stubHttpRequest(ANIME_LIST_URL, "mal/watching_anime_offset_0.json", HttpStatus.OK);
	}

	private void stubAnimeListError(HttpStatus httpStatus) {
		stubHttpRequest(ANIME_LIST_URL, "mal/not_expected_response.json", httpStatus);
	}

	private void testWithRestrictedMalAccess(HttpStatus httpStatus) {
		//given
		stubAnimeListError(httpStatus);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, MAL_RESTRICTED_ACCESS_ERROR_MESSAGE);
	}

	private ResponseSpec call(String username) {
		RequestHeadersSpec<?> spec = webTestClient.get().uri(x -> x.pathSegment("user").pathSegment(username).pathSegment("anime-list").build());
		return spec.exchange();
	}

	private void checkResponse(ResponseSpec result) {
		result.expectStatus().isEqualTo(HttpStatus.OK).expectBody(new ParameterizedTypeReference<AnimeListResponse>() {})
				.value(new CustomTypeSafeMatcher<>("unordered fields should be equal") {
					@Override
					protected boolean matchesSafely(AnimeListResponse actual) {
						List<Anime> expectedAnimeList = getExpectedAnimeList();
						List<Anime> actualAnimeList = actual.getAnimeList();
						Map<String, Anime> animeMappedByName = actualAnimeList.stream().collect(Collectors.toMap(Anime::getName, Function.identity()));
						return StringUtils.isBlank(actual.getErrorMessage()) && Objects.equals(expectedAnimeList.size(), actualAnimeList.size())
									 && expectedAnimeList.stream().allMatch(x -> {
							Anime actualAnime = animeMappedByName.get(x.getName());
							return Objects.nonNull(actualAnime) && Objects.equals(x.getNextEpisode(), actualAnime.getNextEpisode()) && Objects.equals(
									x.getPosterUrl(), actualAnime.getPosterUrl()) && Objects.equals(x.getMalUrl(), actualAnime.getMalUrl())
										 && x.getFandubInfoList().size() == actualAnime.getFandubInfoList().size() && actualAnime.getFandubInfoList()
												 .containsAll(x.getFandubInfoList());
						});
					}
				});
	}

	private void checkResponse(ResponseSpec result, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
		result.expectStatus()
				.isEqualTo(expectedHttpStatus)
				.expectBody(new ParameterizedTypeReference<AnimeListResponse>() {})
				.isEqualTo(new AnimeListResponse(expectedErrorMessage));
	}

	private List<Anime> getExpectedAnimeList() {
		return unmarshal("result", "expected_anime_list.json", new TypeReference<List<Anime>>() {}).stream()
				.peek(x -> x.setMalUrl(appProps.getMalProps().getUrl() + x.getMalUrl()))
				.collect(Collectors.toList());
	}
}