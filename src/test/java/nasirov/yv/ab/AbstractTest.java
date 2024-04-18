package nasirov.yv.ab;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import java.util.Map;
import nasirov.yv.ab.configuration.TestRedisConfiguration;
import nasirov.yv.ab.dto.api.ApiErrorResponse;
import nasirov.yv.ab.dto.fandub_data.FandubDataId;
import nasirov.yv.ab.model.FandubDataKey;
import nasirov.yv.ab.model.FandubDataValue;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.UserServiceI;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisodeDataDto;
import nasirov.yv.starter.common.service.WrappedObjectMapperI;
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
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {AnibozuApplication.class, TestRedisConfiguration.class})
public abstract class AbstractTest {

	@SpyBean
	protected ReactiveRedisTemplate<FandubDataKey, FandubDataValue> redisTemplate;

	@SpyBean
	protected UserServiceI userService;

	@SpyBean
	protected FandubDataServiceI fandubDataService;

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
		wireMockServer.addStubMapping(
				WireMock.get(url).willReturn(WireMock.aResponse().withBodyFile(bodyFilePath).withStatus(httpStatus.value())).build());
	}

	protected Map<Integer, Map<Integer, List<FandubEpisodeDataDto>>> getTestFandubData() {
		return unmarshal("fandub_data", "fandub_data.json", new TypeReference<>() {});
	}

	protected void createFandubData() {
		getTestFandubData().forEach((malId, map) -> map.forEach(
				(episodeId, episodes) -> fandubDataService.createOrUpdateFandubData(buildFandubDataId(malId, episodeId), new FandubDataDto(episodes))
						.subscribe()));
	}

	protected void deleteFandubData() {
		getTestFandubData().forEach(
				(malId, map) -> map.forEach((episodeId, episodes) -> fandubDataService.deleteFandubData(buildFandubDataId(malId, episodeId)).subscribe()));
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

	private FandubDataId buildFandubDataId(Integer malId, Integer episodeId) {
		return FandubDataId.builder().malId(malId).episodeId(episodeId).build();
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
