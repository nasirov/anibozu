package nasirov.yv.ab;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Objects;
import java.util.Set;
import nasirov.yv.ab.dto.internal.FandubData;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CacheServiceI;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.ab.service.MalAnimeServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CompiledAnimeResource;
import nasirov.yv.starter.common.service.CompiledAnimeResourcesServiceI;
import nasirov.yv.starter.common.service.WrappedObjectMapperI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractTest {

	@SpyBean
	protected CompiledAnimeResourcesServiceI<Mono<CompiledAnimeResource>> compiledAnimeResourcesService;

	@SpyBean
	protected ProcessServiceI processService;

	@SpyBean
	protected CacheManager cacheManager;

	@SpyBean
	protected FandubDataServiceI fandubDataService;

	@SpyBean
	protected MalAnimeServiceI malAnimeService;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected AppProps appProps;

	@Autowired
	protected CacheServiceI cacheService;

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
		clearCaches();
		clearAllStub();
	}

	protected void stubHttpRequest(String url, String bodyFilePath, HttpStatus httpStatus) {
		wireMockServer.addStubMapping(
				WireMock.get(url).willReturn(WireMock.aResponse().withBodyFile(bodyFilePath).withStatus(httpStatus.value())).build());
	}

	protected Set<FandubSource> getEnabledFandubSources() {
		return appProps.getEnabledFandubSources();
	}

	protected void mockCompiledAnimeResourcesService() {
		getEnabledFandubSources().forEach(x -> doReturn(Mono.just(
				unmarshal("github", x.name() + "_" + CompiledAnimeResourcesServiceI.FILE_NAME, new TypeReference<CompiledAnimeResource>() {}))).when(
				compiledAnimeResourcesService).getCompiledAnimeResource(x));
	}

	protected void fillGithubCache() {
		mockCompiledAnimeResourcesService();
		cacheService.fillGithubCache().block();
		checkGithubCacheIsFilled();
	}

	protected void checkGithubCacheIsFilled() {
		Cache githubCache = getGithubCache();
		assertNotNull(githubCache);
		FandubData cached = githubCache.get(getGithubCacheKey(), FandubData.class);
		assertNotNull(cached);
	}

	protected Cache getGithubCache() {
		return cacheManager.getCache(appProps.getCacheProps().getGithubCacheName());
	}

	protected String getGithubCacheKey() {
		return appProps.getCacheProps().getGithubCacheKey();
	}

	protected <T> T unmarshal(String directory, String file, TypeReference<T> type) {
		return wrappedObjectMapper.unmarshalFromFile("classpath:__files/" + directory + "/" + file, type);
	}

	private void clearCaches() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).filter(Objects::nonNull).forEach(Cache::clear);
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
