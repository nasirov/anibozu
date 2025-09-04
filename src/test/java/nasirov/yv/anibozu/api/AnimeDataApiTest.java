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
import nasirov.yv.anibozu.model.AnimeEpisodesData;
import nasirov.yv.anibozu.properties.AppProps.Security.Admin;
import nasirov.yv.starter_common.constant.AnimeSite;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.anibozu.EpisodesData;
import nasirov.yv.starter_common.dto.anibozu.EpisodesData.EpisodeData;
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
		EpisodesData episodes = buildEpisodesData();
		//when
		ResponseSpec result = save(episodes);
		//then
		checkResponse(result, HttpStatus.OK);
	}

	@Test
	void shouldNotSaveAnimeData500() {
		//given
		EpisodesData episodes = buildEpisodesData();
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doReturn(Mono.just(false)).when(opsForValue).set(new AnimeDataKey(MAL_ID, EPISODE_ID), new AnimeDataValue(buildAnimeEpisodeData()));
		//when
		ResponseSpec result = save(episodes);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void shouldNotSaveAnimeDataException500() {
		//given
		EpisodesData episodes = buildEpisodesData();
		ReactiveValueOperations<AnimeDataKey, AnimeDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue)
				.set(new AnimeDataKey(MAL_ID, EPISODE_ID), new AnimeDataValue(buildAnimeEpisodeData()));
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
		EpisodesData episodes = buildEpisodesData();
		//when
		ResponseSpec result = saveNoAuth(episodes);
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldGetAnimeData200() {
		//given
		EpisodesData episodes = buildEpisodesData();
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
		EpisodesData episodes = buildEpisodesData();
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

	private ResponseSpec save(EpisodesData episodes) {
		return requestAnimeData(HttpMethod.PUT, true).bodyValue(new AnimeData(episodes)).exchange();
	}

	private ResponseSpec saveNoAuth(EpisodesData episodes) {
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

	private EpisodesData buildEpisodesData() {
		return EpisodesData.builder()
				.dub(1)
				.sub(2)
				.list(List.of(EpisodeData.builder()
						.site(AnimeSite.KODIK.name())
						.siteName(AnimeSite.KODIK.getCanonicalName())
						.type("dub_sub")
						.source("Foobar")
						.link("https://foo.bar/42")
						.name("1 foo")
						.build()))
				.build();
	}

	private AnimeEpisodesData buildAnimeEpisodeData() {
		EpisodesData episodesData = buildEpisodesData();
		return AnimeEpisodesData.builder()
				.dub(episodesData.dub())
				.sub(episodesData.sub())
				.list(episodesData.list()
						.stream()
						.map(x -> AnimeEpisodeData.builder()
								.site(x.site())
								.siteName(x.siteName())
								.type(x.type())
								.source(x.source())
								.link(x.link())
								.name(x.name())
								.build())
						.toList())
				.build();
	}

	private void checkResponse(ResponseSpec result, HttpStatus expectedStatus) {
		result.expectStatus().isEqualTo(expectedStatus.value()).expectBody().isEmpty();
	}
}