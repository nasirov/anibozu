package nasirov.yv.anibozu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import nasirov.yv.anibozu.configuration.TestRedisConfiguration;
import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.api.ApiErrorResponse;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import nasirov.yv.anibozu.properties.AppProps;
import nasirov.yv.anibozu.service.AnimeDataServiceI;
import nasirov.yv.anibozu.service.MalServiceI;
import nasirov.yv.anibozu.service.UserServiceI;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.anibozu.EpisodeData;
import nasirov.yv.starter_common.service.WrappedObjectMapperI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {Application.class, TestRedisConfiguration.class})
public abstract class AbstractTest {

	@SpyBean
	protected ReactiveRedisTemplate<AnimeDataKey, AnimeDataValue> redisTemplate;

	@SpyBean
	protected UserServiceI userService;

	@SpyBean
	protected AnimeDataServiceI animeDataService;

	@SpyBean
	protected MalServiceI malService;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected AppProps appProps;

	@Autowired
	protected WireMockServer wireMockServer;

	@Autowired
	protected WrappedObjectMapperI wrappedObjectMapper;

	protected WebTestClient webTestClient;

	@BeforeEach
	protected void setUp() {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
	}

	@AfterEach
	protected void tearDown() {
		clearAllStub();
	}

	protected void stubHttpRequest(String url, String bodyFilePath, HttpStatus httpStatus) {
		stubHttpRequest(url, bodyFilePath, httpStatus, Duration.ZERO);
	}

	protected void stubHttpRequest(String url, String bodyFilePath, HttpStatus httpStatus, Duration delay) {
		wireMockServer.addStubMapping(WireMock.get(url)
				.willReturn(WireMock.aResponse().withBodyFile(bodyFilePath).withStatus(httpStatus.value()).withFixedDelay((int) delay.toMillis()))
				.build());
	}

	protected Map<Integer, Map<Integer, List<EpisodeData>>> getTestAnimeData() {
		return unmarshal("anime_data", "anime_data.json", new TypeReference<>() {});
	}

	protected void createAnimeData() {
		getTestAnimeData().forEach((malId, map) -> map.forEach(
				(episodeId, episodes) -> animeDataService.saveAnimeData(buildAnimeDataId(malId, episodeId), new AnimeData(episodes)).subscribe()));
	}

	protected void deleteAnimeData() {
		getTestAnimeData().forEach(
				(malId, map) -> map.forEach((episodeId, episodes) -> animeDataService.deleteAnimeData(buildAnimeDataId(malId, episodeId)).subscribe()));
	}

	protected <T> T unmarshal(String directory, String file, TypeReference<T> type) {
		return wrappedObjectMapper.unmarshalFromFile("classpath:__files/" + directory + "/" + file, type);
	}

	protected void checkErrorResponse(ResponseSpec result, HttpStatus expectedStatus, String expectedErrorMessage) {
		result.expectStatus()
				.isEqualTo(expectedStatus)
				.expectBody(new ParameterizedTypeReference<ApiErrorResponse>() {})
				.isEqualTo(new ApiErrorResponse(expectedErrorMessage));
	}

	protected void checkGenericErrorResponse(ResponseSpec result) {
		checkErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, "Sorry, something went wrong. Please, try again later.");
	}

	protected void checkValidationErrorResponse(ResponseSpec result) {
		checkErrorResponse(result, HttpStatus.BAD_REQUEST, "Invalid request.");
	}

	private AnimeDataId buildAnimeDataId(Integer malId, Integer episodeId) {
		return AnimeDataId.builder().malId(malId).episodeId(episodeId).build();
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
