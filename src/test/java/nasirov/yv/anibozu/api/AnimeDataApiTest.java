package nasirov.yv.anibozu.api;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import nasirov.yv.anibozu.AbstractTest;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import nasirov.yv.anibozu.model.AnimeEpisodeData;
import nasirov.yv.anibozu.properties.AppProps.Security.Admin;
import nasirov.yv.starter_common.constant.AnimeSite;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.anibozu.EpisodeData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

class AnimeDataApiTest extends AbstractTest {

	private static final int MAL_ID = 42;

	private static final int EPISODE_ID = 1;

	@Test
	void shouldSaveAnimeData200() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		//when
		ResponseSpec result = save(episodes);
		//then
		checkResponse(result, HttpStatus.OK);
	}

	@Test
	void shouldNotSaveAnimeData500() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doReturn(Mono.just(false)).when(opsForValue).set(new AnimeDataKey(MAL_ID, EPISODE_ID), new AnimeDataValue(buildAnimeEpisodeDataList()));
		//when
		ResponseSpec result = save(episodes);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void shouldNotSaveAnimeDataException500() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue)
				.set(new AnimeDataKey(MAL_ID, EPISODE_ID), new AnimeDataValue(buildAnimeEpisodeDataList()));
		//when
		ResponseSpec result = save(episodes);
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotSaveAnimeDataBodyIsInvalid400() {
		//given
		//when
		ResponseSpec result = save(null);
		//then
		checkValidationErrorResponse(result);
	}

	@Test
	void shouldNotSaveAnimeData401() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		//when
		ResponseSpec result = saveNoAuth(episodes);
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldGetAnimeData200() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		ResponseSpec createResult = save(episodes);
		checkResponse(createResult, HttpStatus.OK);
		//when
		ResponseSpec result = get();
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK.value())
				.expectBody(new ParameterizedTypeReference<AnimeData>() {})
				.isEqualTo(new AnimeData(episodes));
		delete();
	}

	@Test
	void shouldNotGetAnimeData404() {
		//given
		//when
		ResponseSpec result = get();
		//then
		checkResponse(result, HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotGetAnimeDataException500() {
		//given
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue).get(new AnimeDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = get();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotGetAnimeData401() {
		//given
		//when
		ResponseSpec result = getNoAuth();
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldDeleteAnimeData200() {
		//given
		List<EpisodeData> episodes = buildEpisodeDataList();
		ResponseSpec createResult = save(episodes);
		checkResponse(createResult, HttpStatus.OK);
		//when
		ResponseSpec result = delete();
		//then
		checkResponse(result, HttpStatus.OK);
	}

	@Test
	void shouldNotDeleteAnimeData500() {
		//given
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doReturn(Mono.just(false)).when(opsForValue).delete(new AnimeDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = delete();
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void shouldNotDeleteAnimeDataException500() {
		//given
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue).delete(new AnimeDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = delete();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotDeleteAnimeData401() {
		//given
		//when
		ResponseSpec result = deleteNoAuth();
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	private ResponseSpec save(List<EpisodeData> episodes) {
		return requestAnimeData(HttpMethod.PUT, true).bodyValue(new AnimeData(episodes)).exchange();
	}

	private ResponseSpec saveNoAuth(List<EpisodeData> episodes) {
		return requestAnimeData(HttpMethod.PUT, false).bodyValue(new AnimeData(episodes)).exchange();
	}

	private ResponseSpec get() {
		return requestAnimeData(HttpMethod.GET, true).exchange();
	}

	private ResponseSpec getNoAuth() {
		return requestAnimeData(HttpMethod.GET, false).exchange();
	}

	private ResponseSpec delete() {
		return requestAnimeData(HttpMethod.DELETE, true).exchange();
	}

	private ResponseSpec deleteNoAuth() {
		return requestAnimeData(HttpMethod.DELETE, false).exchange();
	}

	private RequestBodySpec requestAnimeData(HttpMethod method, boolean auth) {
		RequestBodySpec result = webTestClient.method(method).uri("/api/v1/anime/{mal-id}/episode/{episode-id}", MAL_ID, EPISODE_ID);
		if (auth) {
			result.header(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader());
		}
		return result;
	}

	private String buildAuthorizationHeader() {
		Admin admin = appProps.getSecurity().getAdmin();
		return "Basic " + Base64.getEncoder().encodeToString((admin.getUsername() + ":" + admin.getPassword()).getBytes(StandardCharsets.UTF_8));
	}

	private List<EpisodeData> buildEpisodeDataList() {
		return List.of(
				EpisodeData.builder().animeSite(AnimeSite.KODIK.name()).link("https://foo.bar/42").name("1 foo").extra(List.of("dub", "sub")).build());
	}

	private List<AnimeEpisodeData> buildAnimeEpisodeDataList() {
		return buildEpisodeDataList().stream().map(x -> new AnimeEpisodeData(x.animeSite(), x.link(), x.name(), x.extra())).toList();
	}

	private void checkResponse(ResponseSpec result, HttpStatus expectedStatus) {
		result.expectStatus().isEqualTo(expectedStatus.value()).expectBody().isEmpty();
	}
}