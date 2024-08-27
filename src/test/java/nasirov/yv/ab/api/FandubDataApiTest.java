package nasirov.yv.ab.api;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import nasirov.yv.ab.AbstractTest;
import nasirov.yv.ab.model.FandubDataKey;
import nasirov.yv.ab.model.FandubDataValue;
import nasirov.yv.ab.model.FandubEpisodeData;
import nasirov.yv.ab.properties.AppProps.Security.Admin;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisodeDataDto;
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

/**
 * @author Nasirov Yuriy
 */
class FandubDataApiTest extends AbstractTest {

	private static final int MAL_ID = 42;

	private static final int EPISODE_ID = 1;

	@Test
	void shouldCreateOrUpdateFandubData200() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		//when
		ResponseSpec result = createOrUpdate(episodes);
		//then
		checkResponse(result, HttpStatus.OK);
	}

	@Test
	void shouldNotCreateOrUpdateFandubData500() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		ReactiveValueOperations<FandubDataKey, FandubDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doReturn(Mono.just(false)).when(opsForValue).set(new FandubDataKey(MAL_ID, EPISODE_ID), new FandubDataValue(buildFandubEpisodeDataList()));
		//when
		ResponseSpec result = createOrUpdate(episodes);
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void shouldNotCreateOrUpdateFandubDataException500() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		ReactiveValueOperations<FandubDataKey, FandubDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue)
				.set(new FandubDataKey(MAL_ID, EPISODE_ID), new FandubDataValue(buildFandubEpisodeDataList()));
		//when
		ResponseSpec result = createOrUpdate(episodes);
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotCreateOrUpdateFandubDataBodyIsInvalid400() {
		//given
		//when
		ResponseSpec result = createOrUpdate(null);
		//then
		checkValidationErrorResponse(result);
	}

	@Test
	void shouldNotCreateOrUpdateFandubData401() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		//when
		ResponseSpec result = createOrUpdateNoAuth(episodes);
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldGetFandubData200() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		ResponseSpec createResult = createOrUpdate(episodes);
		checkResponse(createResult, HttpStatus.OK);
		//when
		ResponseSpec result = get();
		//then
		result.expectStatus()
				.isEqualTo(HttpStatus.OK.value())
				.expectBody(new ParameterizedTypeReference<FandubDataDto>() {})
				.isEqualTo(new FandubDataDto(episodes));
		delete();
	}

	@Test
	void shouldNotGetFandubData404() {
		//given
		//when
		ResponseSpec result = get();
		//then
		checkResponse(result, HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotGetFandubDataException500() {
		//given
		ReactiveValueOperations<FandubDataKey, FandubDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue).get(new FandubDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = get();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotGetFandubData401() {
		//given
		//when
		ResponseSpec result = getNoAuth();
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldDeleteFandubData200() {
		//given
		List<FandubEpisodeDataDto> episodes = buildFandubEpisodeDataDtoList();
		ResponseSpec createResult = createOrUpdate(episodes);
		checkResponse(createResult, HttpStatus.OK);
		//when
		ResponseSpec result = delete();
		//then
		checkResponse(result, HttpStatus.OK);
	}

	@Test
	void shouldNotDeleteFandubData500() {
		//given
		ReactiveValueOperations<FandubDataKey, FandubDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doReturn(Mono.just(false)).when(opsForValue).delete(new FandubDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = delete();
		//then
		checkResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void shouldNotDeleteFandubDataException500() {
		//given
		ReactiveValueOperations<FandubDataKey, FandubDataValue> opsForValue = Mockito.spy(redisTemplate.opsForValue());
		doReturn(opsForValue).when(redisTemplate).opsForValue();
		doThrow(new RuntimeException("ReactiveValueOperations cause")).when(opsForValue).delete(new FandubDataKey(MAL_ID, EPISODE_ID));
		//when
		ResponseSpec result = delete();
		//then
		checkGenericErrorResponse(result);
	}

	@Test
	void shouldNotDeleteFandubData401() {
		//given
		//when
		ResponseSpec result = deleteNoAuth();
		//then
		checkResponse(result, HttpStatus.UNAUTHORIZED);
	}

	private ResponseSpec createOrUpdate(List<FandubEpisodeDataDto> episodes) {
		return requestFandubData(HttpMethod.PUT, true).bodyValue(new FandubDataDto(episodes)).exchange();
	}

	private ResponseSpec createOrUpdateNoAuth(List<FandubEpisodeDataDto> episodes) {
		return requestFandubData(HttpMethod.PUT, false).bodyValue(new FandubDataDto(episodes)).exchange();
	}

	private ResponseSpec get() {
		return requestFandubData(HttpMethod.GET, true).exchange();
	}

	private ResponseSpec getNoAuth() {
		return requestFandubData(HttpMethod.GET, false).exchange();
	}

	private ResponseSpec delete() {
		return requestFandubData(HttpMethod.DELETE, true).exchange();
	}

	private ResponseSpec deleteNoAuth() {
		return requestFandubData(HttpMethod.DELETE, false).exchange();
	}

	private RequestBodySpec requestFandubData(HttpMethod method, boolean auth) {
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

	private List<FandubEpisodeDataDto> buildFandubEpisodeDataDtoList() {
		return List.of(FandubEpisodeDataDto.builder()
				.fandubSource(FandubSource.KODIK.name())
				.link("https://foo.bar/42")
				.name("1 foo")
				.extra(List.of("dub", "sub"))
				.build());
	}

	private List<FandubEpisodeData> buildFandubEpisodeDataList() {
		return buildFandubEpisodeDataDtoList().stream().map(x -> new FandubEpisodeData(x.fandubSource(), x.link(), x.name(), x.extra())).toList();
	}

	private void checkResponse(ResponseSpec result, HttpStatus expectedStatus) {
		result.expectStatus().isEqualTo(expectedStatus.value()).expectBody().isEmpty();
	}
}