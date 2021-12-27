package nasirov.yv;

import java.util.Objects;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ServerSentEventServiceI;
import nasirov.yv.service.impl.common.CacheCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Nasirov Yuriy
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractTest {

	@MockBean
	protected HttpRequestServiceI httpRequestService;

	@MockBean
	protected MalServiceI malService;

	@SpyBean
	protected AnimeServiceI animeService;

	@SpyBean
	protected ServerSentEventServiceI serverSentEventService;

	@SpyBean
	protected CacheCleanerService cacheCleanerService;

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected ExternalServicesProps externalServicesProps;

	@Autowired
	protected FanDubProps fanDubProps;

	protected WebTestClient webTestClient;

	@BeforeEach
	public void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
				.build();
	}

	@AfterEach
	public void tearDown() {
		clearCaches();
	}

	private void clearCaches() {
		cacheManager.getCacheNames()
				.stream()
				.map(cacheManager::getCache)
				.filter(Objects::nonNull)
				.forEach(Cache::clear);
	}
}
