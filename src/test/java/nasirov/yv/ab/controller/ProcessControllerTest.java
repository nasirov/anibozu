package nasirov.yv.ab.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

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
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class ProcessControllerTest extends AbstractTest {

	private static final String MAL_USERNAME = "foobarbaz";

	private static final String USER_PROFILE_URL = "/profile/" + MAL_USERNAME;

	@Test
	void shouldReturnProcessResult() {
		//given
		fillGithubCache();
		stubMalHttpRequests(5);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result);
	}

	@Test
	void shouldReturnProcessResultCommonTitlesCacheFails() {
		//given
		mockGitHubResourcesService();
		stubMalHttpRequests(5);
		String cacheKey = getGithubCacheKey();
		Cache spiedCache = getSpiedGithubCache();
		doThrow(new RuntimeException("fail on get")).when(spiedCache).get(cacheKey, List.class);
		doThrow(new RuntimeException("fail on put")).when(spiedCache).put(eq(cacheKey), any(List.class));
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result);
	}

	@Test
	void shouldReturnErrorMalUserAccountNotFoundException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.NOT_FOUND);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "MAL account " + MAL_USERNAME + " is not found.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionMalAccessRestored() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(true);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", but MAL rejected our requests with status 403.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionMalAccessNotRestored() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(false);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionMalAccessRestorerException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		mockMalAccessRestorerException();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorMalUnavailableException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.SERVICE_UNAVAILABLE);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", but MAL is unavailable now.");
	}

	@Test
	void shouldReturnErrorUnexpectedCallingException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.GATEWAY_TIMEOUT);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorWatchingTitlesNotFoundException() {
		//given
		stubMalUserProfileHttpRequest(0);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Not found watching titles for " + MAL_USERNAME + " !");
	}

	@Test
	void shouldReturnErrorMalUserAnimeListAccessException() {
		//given
		stubMalHttpRequests(HttpStatus.BAD_REQUEST);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, MAL_USERNAME + "'s anime list has private access!");
	}

	@Test
	void shouldReturnErrorUnexpectedCallingExceptionOnAnimeList() {
		//given
		stubMalHttpRequests(HttpStatus.INTERNAL_SERVER_ERROR);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorMalUnavailableExceptionOnAnimeList() {
		//given
		stubMalHttpRequests(HttpStatus.SERVICE_UNAVAILABLE);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", but MAL is unavailable now.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionOnAnimeListMalAccessRestored() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(true);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", but MAL rejected our requests with status 403.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionOnAnimeListMalAccessNotRestored() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(false);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorMalForbiddenExceptionOnAnimeListMalAccessRestorerException() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		mockMalAccessRestorerException();
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.");
	}

	@Test
	void shouldReturnErrorEmptyAnimeList() {
		//given
		stubMalUserProfileHttpRequest(1);
		stubMalAnimeListHttpRequest(0, "mal/watching-titles-empty.json");
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorMalServiceException() {
		//given
		doThrow(new RuntimeException("MalService cause")).when(malService).getMalUserInfo(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturnErrorCommonTitlesServiceException() {
		//given
		stubMalHttpRequests(1);
		doThrow(new RuntimeException("CommonTitlesService cause")).when(commonTitlesService)
				.getCommonTitles(anySet(), anyList());
		//when
		ResponseSpec result = call(MAL_USERNAME);
		//then
		checkResponse(result, HttpStatus.OK, ExceptionHandlers.GENERIC_ERROR_MESSAGE);
	}

	@Test
	void shouldReturn400Error() {
		//given
		String[] invalidUsernameArray = {"moreThan16Charssss", "space between ", "@#!sd", "1"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray).map(x -> call(x)).toList();
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

	private void stubMalHttpRequests(int amountOfTitles) {
		stubMalUserProfileHttpRequest(amountOfTitles);
		int offsetStep = appProps.getMalProps().getOffsetStep();
		for (int i = 0, offset = 0; i <= amountOfTitles / offsetStep; i++, offset += offsetStep) {
			stubMalAnimeListHttpRequest(offset, "mal/watching-titles-offset-" + offset + ".json");
		}
	}

	private void stubMalHttpRequests(HttpStatus animeListStatus) {
		stubMalUserProfileHttpRequest(1);
		for (int i = 0, offset = 0; i < 1; i++, offset += appProps.getMalProps().getOffsetStep()) {
			stubHttpRequest(buildWatchingTitlesAnimeListUrl(offset), animeListStatus);
		}
	}

	private void stubMalAnimeListHttpRequest(int offset, String bodyFilePath) {
		stubHttpRequest(buildWatchingTitlesAnimeListUrl(offset), bodyFilePath, HttpStatus.OK);
	}

	private String buildWatchingTitlesAnimeListUrl(int offset) {
		return "/animelist/" + MAL_USERNAME + "/load.json?offset=" + offset + "&status="
				+ MalTitleWatchingStatus.WATCHING.getCode();
	}

	private void stubMalUserProfileHttpRequest(int amountOfTitles) {
		stubHttpRequest(USER_PROFILE_URL, "mal/user-profile-" + amountOfTitles + ".html", HttpStatus.OK);
	}

	private void stubMalUserProfileHttpRequest(HttpStatus status) {
		stubHttpRequest(USER_PROFILE_URL, status);
	}

	private void stubMalAccessRestorerHttpRequest(boolean restored) {
		stubHttpRequest("/access/restore", "mal-access-restorer/" + (restored ? "" : "not-") + "restored.txt", HttpStatus.OK);
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
						Map<FandubSource, String> expectedFandubMap = getExpectedFandubMap();
						List<Title> actualTitles = actual.getTitles();
						Map<String, Title> nameToTitle = actualTitles.stream()
								.collect(Collectors.toMap(Title::getNameOnMal, Function.identity()));
						Map<FandubSource, String> actualFandubMap = actual.getFandubMap();
						return StringUtils.isBlank(actual.getErrorMessage()) && Objects.equals(expectedTitles.size(),
								(actualTitles.size())) && expectedTitles.stream().allMatch(x -> {
							Title actualTitle = nameToTitle.get(x.getNameOnMal());
							return Objects.nonNull(actualTitle) && x.getType() == actualTitle.getType() && Objects.equals(
									x.getEpisodeNumberOnMal(), actualTitle.getEpisodeNumberOnMal()) && Objects.equals(x.getPosterUrlOnMal(),
									actualTitle.getPosterUrlOnMal()) && Objects.equals(x.getAnimeUrlOnMal(), actualTitle.getAnimeUrlOnMal())
									&& x.getFandubToUrl().size() == actualTitle.getFandubToUrl().size() && x.getFandubToUrl()
									.entrySet()
									.stream()
									.allMatch(e -> Objects.equals(e.getValue(), actualTitle.getFandubToUrl().get(e.getKey())))
									&& x.getFandubToEpisodeName().size() == actualTitle.getFandubToEpisodeName().size()
									&& x.getFandubToEpisodeName()
									.entrySet()
									.stream()
									.allMatch(e -> Objects.equals(e.getValue(), actualTitle.getFandubToEpisodeName().get(e.getKey())));
						}) && Objects.equals(expectedFandubMap.size(), (actualFandubMap.size())) && expectedFandubMap.entrySet()
								.stream()
								.allMatch(e -> Objects.equals(e.getValue(), actualFandubMap.get(e.getKey())));
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
				.peek(x -> x.setAnimeUrlOnMal(appProps.getMalProps().getUrl() + x.getAnimeUrlOnMal()))
				.collect(Collectors.toList());
	}

	private Map<FandubSource, String> getExpectedFandubMap() {
		return getEnabledFandubSources().stream().collect(Collectors.toMap(Function.identity(),
				FandubSource::getCanonicalName));
	}

	private void mockMalAccessRestorerException() {
		doThrow(new RuntimeException("MalAccessRestorer cause")).when(malAccessRestorer).restoreMalAccess();
	}
}