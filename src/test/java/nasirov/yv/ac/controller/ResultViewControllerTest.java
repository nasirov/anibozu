package nasirov.yv.ac.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nasirov.yv.ac.AbstractTest;
import nasirov.yv.ac.dto.fe.InputDto;
import nasirov.yv.ac.dto.fe.TitleDto;
import nasirov.yv.ac.service.impl.ResultProcessingService;
import nasirov.yv.ac.utils.IOUtils;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class ResultViewControllerTest extends AbstractTest {

	private static final String MAL_USERNAME = "foobarbaz";

	private static final String USER_PROFILE_URL = "/profile/" + MAL_USERNAME;

	@Test
	void shouldReturnResultView() {
		//given
		fillGithubCache();
		stubMalHttpRequests(5);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		assertEquals("Result for " + MAL_USERNAME, getTitleText(document));
		assertEquals(MAL_USERNAME, getInputValue(document, "username"));
		assertEquals(getExpectedFandubList(), getInputValue(document, "fandubList"));
		assertEquals(getExpectedTitleDtos(), getActualTitleDtos(document));
		assertEquals(MAL_USERNAME + "'s currently watching titles", getStatusText(document));
	}

	@Test
	void shouldReturnResultViewCommonTitlesCacheFails() {
		//given
		mockGitHubResourcesService();
		stubMalHttpRequests(5);
		String cacheKey = getGithubCacheKey();
		Cache spiedCache = getSpiedGithubCache();
		doThrow(new RuntimeException("fail on get")).when(spiedCache).get(cacheKey, List.class);
		doThrow(new RuntimeException("fail on put")).when(spiedCache).put(eq(cacheKey), any(List.class));
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		assertEquals("Result for " + MAL_USERNAME, getTitleText(document));
		assertEquals(MAL_USERNAME, getInputValue(document, "username"));
		assertEquals(getExpectedFandubList(), getInputValue(document, "fandubList"));
		assertEquals(getExpectedTitleDtos(), getActualTitleDtos(document));
		assertEquals(MAL_USERNAME + "'s currently watching titles", getStatusText(document));
		verify(gitHubResourcesService, never()).getResourcesParts();
	}

	@Test
	void shouldReturnErrorViewMalUserAccountNotFoundException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.NOT_FOUND);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "MAL account " + MAL_USERNAME + " is not found.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionMalAccessRestored() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(true);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", but MAL rejected our requests with status 403.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionMalAccessNotRestored() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(false);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionMalAccessRestorerException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.FORBIDDEN);
		mockMalAccessRestorerException();
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewUnexpectedCallingException() {
		//given
		stubMalUserProfileHttpRequest(HttpStatus.GATEWAY_TIMEOUT);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewWatchingTitlesNotFoundException() {
		//given
		stubMalUserProfileHttpRequest(0);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Not found watching titles for " + MAL_USERNAME + " !";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalUserAnimeListAccessException() {
		//given
		stubMalHttpRequests(HttpStatus.BAD_REQUEST);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = MAL_USERNAME + "'s anime list has private access!";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewUnexpectedCallingExceptionOnAnimeList() {
		//given
		stubMalHttpRequests(HttpStatus.INTERNAL_SERVER_ERROR);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionOnAnimeListMalAccessRestored() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(true);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", but MAL rejected our requests with status 403.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionOnAnimeListMalAccessNotRestored() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		stubMalAccessRestorerHttpRequest(false);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalForbiddenExceptionOnAnimeListMalAccessRestorerException() {
		//given
		stubMalHttpRequests(HttpStatus.FORBIDDEN);
		mockMalAccessRestorerException();
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = "Sorry, " + MAL_USERNAME + ", unexpected error has occurred.";
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewEmptyAnimeList() {
		//given
		stubMalUserProfileHttpRequest(1);
		stubMalAnimeListHttpRequest(0, "mal/watching-titles-empty.json");
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = ResultProcessingService.GENERIC_ERROR_MESSAGE;
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewMalServiceException() {
		//given
		doThrow(new RuntimeException("MalService cause")).when(malService).getMalUserInfo(MAL_USERNAME);
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = ResultProcessingService.GENERIC_ERROR_MESSAGE;
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturnErrorViewCommonTitlesServiceException() {
		//given
		stubMalHttpRequests(1);
		doThrow(new RuntimeException("CommonTitlesService cause")).when(commonTitlesService)
				.getCommonTitles(anySet(), anyList());
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of());
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK);
		Document document = getDocument(result);
		String expectedErrorMessage = ResultProcessingService.GENERIC_ERROR_MESSAGE;
		assertEquals(expectedErrorMessage, getTitleText(document));
		assertEquals(expectedErrorMessage, getHeaderText(document));
	}

	@Test
	void shouldReturn4xxErrorView() {
		//given
		String[] invalidUsernameArray = {null, "", "moreThan16Charssss", "space between ", "@#!sd"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray)
				.map(x -> call(x, Map.of(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)))
				.toList();
		//then
		result.forEach(x -> x.expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:__files/view/test-4xx-error-view.html")));
	}

	@Test
	void shouldReturn404ErrorView() {
		//given
		//when
		ResponseSpec result = webTestClient.get()
				.uri("/unknown")
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
				.exchange();
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.NOT_FOUND)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:__files/view/test-404-error-view.html"));
	}

	@Test
	void shouldReturn500ErrorView() {
		//given
		doThrow(new RuntimeException("ResultProcessingService cause")).when(resultProcessingService)
				.getResult(any(InputDto.class));
		//when
		ResponseSpec result = call(MAL_USERNAME, Map.of(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE));
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:__files/view/test-5xx-error-view.html"));
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

	private ResponseSpec call(String username, Map<String, String> headers) {
		RequestHeadersSpec<?> spec = webTestClient.get().uri(x -> x.path("/result").queryParam("username", username).build());
		headers.forEach(spec::header);
		return spec.exchange();
	}

	private Document getDocument(ResponseSpec result) {
		String body = result.expectBody(String.class).returnResult().getResponseBody();
		assertNotNull(body);
		return Jsoup.parse(body);
	}

	private String getExpectedFandubList() {
		return getEnabledFandubSources().stream().map(FandubSource::name).collect(Collectors.joining(","));
	}

	private List<TitleDto> getExpectedTitleDtos() {
		return IOUtils.unmarshalToListFromFile("classpath:__files/view/expected-titles.json", TitleDto.class)
				.stream()
				.peek(x -> x.setAnimeUrlOnMal(appProps.getMalProps().getUrl() + x.getAnimeUrlOnMal()))
				.collect(Collectors.toList());
	}

	private List<TitleDto> getActualTitleDtos(Document document) {
		return IOUtils.unmarshalToListFromString(getInputValue(document, "titles"), TitleDto.class)
				.stream()
				.sorted(Comparator.comparing(TitleDto::getNameOnMal))
				.collect(Collectors.toList());
	}

	private String getStatusText(Document document) {
		return document.selectFirst("h1[id=\"status\"]").ownText();
	}

	private String getTitleText(Document document) {
		return document.selectFirst("title").ownText();
	}

	private String getHeaderText(Document document) {
		return document.selectFirst("h2").ownText();
	}

	private String getInputValue(Document document, String inputId) {
		return document.selectFirst("input[id=\"" + inputId + "\"][type=\"hidden\"]").attr("value");
	}

	private void mockMalAccessRestorerException() {
		doThrow(new RuntimeException("MalAccessRestorer cause")).when(malAccessRestorer).restoreMalAccess();
	}
}