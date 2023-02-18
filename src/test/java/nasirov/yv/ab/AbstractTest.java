package nasirov.yv.ab;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.ab.dto.cache.CacheEntity;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CacheServiceI;
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.ab.service.MalAccessRestorerI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.ab.utils.AwareHelper;
import nasirov.yv.ab.utils.IOUtils;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.service.GitHubResourcesServiceI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.invocation.InvocationOnMock;
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
	protected GitHubResourcesServiceI<Mono<List<CommonTitle>>> gitHubResourcesService;

	@SpyBean
	protected ProcessServiceI processService;

	@SpyBean
	protected CacheManager cacheManager;

	@SpyBean
	protected CommonTitlesServiceI commonTitlesService;

	@SpyBean
	protected MalServiceI malService;

	@SpyBean
	protected MalAccessRestorerI malAccessRestorer;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected AppProps appProps;

	@Autowired
	protected CacheServiceI cacheService;

	@Autowired
	protected WireMockServer wireMockServer;

	protected WebTestClient webTestClient;

	protected AwareHelper awareHelper;

	@BeforeEach
	protected void setUp() {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
		this.awareHelper = new AwareHelper();
	}

	@AfterEach
	protected void tearDown() {
		clearCaches();
		clearAllStub();
	}

	protected void stubHttpRequest(String url, String bodyFilePath, HttpStatus httpStatus) {
		wireMockServer.addStubMapping(WireMock.get(url)
				.willReturn(WireMock.aResponse().withBodyFile(bodyFilePath).withStatus(httpStatus.value()))
				.build());
	}

	protected void stubHttpRequest(String url, String bodyFilePath, HttpStatus httpStatus, Duration delay) {
		wireMockServer.addStubMapping(WireMock.get(url)
				.willReturn(WireMock.aResponse()
						.withBodyFile(bodyFilePath)
						.withStatus(httpStatus.value())
						.withFixedDelay((int) delay.toMillis()))
				.build());
	}

	protected Set<FandubSource> getEnabledFandubSources() {
		return appProps.getEnabledFandubSources();
	}

	protected void mockGitHubResourcesService() {
		getEnabledFandubSources().forEach(x -> doReturn(Mono.just(
				IOUtils.unmarshalToListFromFile("classpath:__files/github/" + x.name() + "-titles.json",
						CommonTitle.class))).when(gitHubResourcesService).getResource(x));
	}

	protected void fillGithubCache() {
		mockGitHubResourcesService();
		spyGithubCachePut();
		cacheService.fillGithubCache().block();
		waitForCachePut();
		checkGithubCacheIsFilled();
	}

	protected Cache spyGithubCachePut() {
		Cache spiedCache = getSpiedGithubCache();
		doAnswer(this::doAnswerAsRealMethodAndSignal).when(spiedCache).put(eq(getGithubCacheKey()), any(List.class));
		return spiedCache;
	}

	@SneakyThrows
	protected Class<Void> doAnswerAsRealMethodAndSignal(InvocationOnMock invocation) {
		invocation.callRealMethod();
		awareHelper.signal();
		return Void.TYPE;
	}

	protected void waitForCachePut() {
		awareHelper.await();
	}

	protected void checkGithubCacheIsFilled() {
		Cache githubCache = getGithubCache();
		assertNotNull(githubCache);
		List<CacheEntity> cached = githubCache.get(getGithubCacheKey(), List.class);
		assertNotNull(cached);
	}

	protected Cache getGithubCache() {
		return cacheManager.getCache(getGithubCacheName());
	}

	protected String getGithubCacheName() {
		return appProps.getCacheProps().getGithubCacheName();
	}

	protected Cache getSpiedGithubCache() {
		Cache githubCache = getGithubCache();
		assertNotNull(githubCache);
		Cache spiedCache = spy(githubCache);
		doReturn(spiedCache).when(cacheManager).getCache(getGithubCacheName());
		return spiedCache;
	}

	protected String getGithubCacheKey() {
		return appProps.getCacheProps().getGithubCacheKey();
	}

	private void clearCaches() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).filter(Objects::nonNull).forEach(Cache::clear);
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
