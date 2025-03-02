package nasirov.yv.anibozu.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import nasirov.yv.anibozu.AbstractTest;
import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.user.AnimeList;
import nasirov.yv.anibozu.dto.user.AnimeList.Anime;
import nasirov.yv.starter_common.dto.mal.WatchingStatus;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

class UserApiTest extends AbstractTest {

	private static final String MAL_USERNAME = "foobarbaz";

	private static final String ANIME_LIST_URL = "/animelist/" + MAL_USERNAME + "/load.json?offset=0&status=" + WatchingStatus.WATCHING.getCode();

	@Test
	void shouldReturnAnimeList() {
		//given
		createAnimeData();
		stubAnimeListOk();
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkResponse(result, "expected_anime_list.json");
		deleteAnimeData();
	}

	@Test
	void shouldReturnAnimeListAnimeDataIsEmpty() {
		//given
		stubAnimeListOk();
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkResponse(result, "expected_anime_list_empty_anime_data.json");
	}

	@Test
	void shouldReturnErrorMalServiceException() {
		//given
		doThrow(new RuntimeException("MalService cause")).when(malService).getAnimeList(MAL_USERNAME);
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldReturnErrorAnimeListPrivateOrDoesNotExist() {
		//given
		stubAnimeListError(HttpStatus.BAD_REQUEST);
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkErrorResponse(result, HttpStatus.UNPROCESSABLE_ENTITY, MAL_USERNAME + "'s anime list is private or does not exist.");
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
		ResponseSpec result = getAnimeList();
		//then
		checkErrorResponse(result, HttpStatus.UNPROCESSABLE_ENTITY,
				"Sorry, " + MAL_USERNAME + ", but MAL is being unavailable now. Please try again later.");
	}

	@Test
	void shouldReturnErrorCannotGetAnimeList() {
		//given
		stubAnimeListError(HttpStatus.INTERNAL_SERVER_ERROR);
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkFailedAnimeListRequestErrorResponse(result);
	}

	@Test
	void shouldReturnErrorCannotGetAnimeListReadTimeout() {
		//given
		stubHttpRequest(ANIME_LIST_URL, "mal/watching_anime_offset_0.json", HttpStatus.OK, Duration.ofSeconds(4));
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkFailedAnimeListRequestErrorResponse(result);
	}

	@Test
	void shouldReturnErrorEmptyAnimeList() {
		//given
		stubHttpRequest(ANIME_LIST_URL, "mal/watching_anime_empty.json", HttpStatus.OK);
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkErrorResponse(result, HttpStatus.UNPROCESSABLE_ENTITY, MAL_USERNAME + "'s anime list is empty or does not contain actual watching anime.");
	}

	@Test
	void shouldReturnErrorAnimeDataServiceException() {
		//given
		stubAnimeListOk();
		doThrow(new RuntimeException("AnimeDataService cause")).when(animeDataService).getAnimeData(any(AnimeDataId.class));
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldReturnError400() {
		//given
		String[] invalidUsernameArray = {"moreThan16Charssss", "space between ", "@#!sd", "1"};
		//when
		List<ResponseSpec> result = Arrays.stream(invalidUsernameArray).map(this::getAnimeList).toList();
		//then
		result.forEach(this::checkValidationErrorResponse);
	}

	@Test
	void shouldReturnError500() {
		//given
		doThrow(new RuntimeException("UserService cause")).when(userService).getAnimeList(MAL_USERNAME);
		//when
		ResponseSpec result = getAnimeList();
		//then
		checkGenericErrorResponse(result);
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
		ResponseSpec result = getAnimeList();
		//then
		checkErrorResponse(result, HttpStatus.UNPROCESSABLE_ENTITY,
				"Sorry, " + MAL_USERNAME + ", but MAL has restricted our access to it. Please try again later.");
	}

	private ResponseSpec getAnimeList() {
		return getAnimeList(MAL_USERNAME);
	}

	private ResponseSpec getAnimeList(String username) {
		return webTestClient.get().uri("/api/v1/user/{username}/anime-list", username).exchange();
	}

	private void checkResponse(ResponseSpec result, String expectedAnimeListFile) {
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(new ParameterizedTypeReference<AnimeList>() {})
				.value(new CustomTypeSafeMatcher<>("unordered fields should be equal") {
					@Override
					protected boolean matchesSafely(AnimeList result) {
						List<Anime> expectedAnimeList = getExpectedAnimeList(expectedAnimeListFile).animeList();
						List<Anime> actualAnimeList = result.animeList();
						Map<String, Anime> animeMappedByName = actualAnimeList.stream().collect(Collectors.toMap(Anime::name, Function.identity()));
						return Objects.equals(expectedAnimeList.size(), actualAnimeList.size()) && expectedAnimeList.stream().allMatch(x -> {
							Anime actualAnime = animeMappedByName.get(x.name());
							return Objects.nonNull(actualAnime) && x.nextEpisode() == actualAnime.nextEpisode() && x.maxEpisodes() == actualAnime.maxEpisodes()
										 && Objects.equals(x.posterUrl(), actualAnime.posterUrl()) && Objects.equals(x.malUrl(), actualAnime.malUrl())
										 && x.episodes().size() == actualAnime.episodes().size() && actualAnime.episodes().containsAll(x.episodes());
						});
					}
				});
	}

	private AnimeList getExpectedAnimeList(String file) {
		return new AnimeList(unmarshal("result", file, new TypeReference<AnimeList>() {}).animeList()
				.stream()
				.map(x -> new Anime(x.name(), x.nextEpisode(), x.maxEpisodes(), x.posterUrl(), appProps.getMal().getUrl() + x.malUrl(), x.episodes(),
						x.airing()))
				.toList());
	}

	private void checkFailedAnimeListRequestErrorResponse(ResponseSpec result) {
		checkErrorResponse(result, HttpStatus.UNPROCESSABLE_ENTITY, "Sorry, cannot get " + MAL_USERNAME + "'s anime list. Please try again later.");
	}
}