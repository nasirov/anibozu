package nasirov.yv.ac;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nasirov.yv.ac.data.front.InputDto;
import nasirov.yv.ac.data.properties.CachesNames;
import nasirov.yv.ac.data.properties.FandubSupportProps;
import nasirov.yv.ac.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.ac.service.ResultProcessingServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractTest {

	public static final String MAL_USERNAME = "foobarbaz";

	@SpyBean
	protected HttpRequestServiceI httpRequestService;

	@SpyBean
	protected ResultProcessingServiceI resultProcessingService;

	@SpyBean
	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected StarterCommonProperties starterCommonProperties;

	@Autowired
	protected HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Autowired
	protected CachesNames cachesNames;

	@Autowired
	protected FandubSupportProps fandubSupportProps;

	protected WebTestClient webTestClient;

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
				.configureClient()
				.responseTimeout(Duration.ofDays(1))
				.build();
	}

	@AfterEach
	void tearDown() {
		clearCaches();
	}

	protected void mockExternalMalServiceResponse(MalServiceResponseDto malServiceResponseDto) {
		doReturn(Mono.just(malServiceResponseDto)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(starterCommonProperties.getExternalServices().getMalServiceUrl() + "titles?username=" + MAL_USERNAME
								+ "&status=" + MalTitleWatchingStatus.WATCHING.name())));
	}

	protected MalServiceResponseDto buildMalServiceResponseDto(List<MalTitle> malTitles, String errorMessage) {
		return MalServiceResponseDto.builder().username(MAL_USERNAME).malTitles(malTitles).errorMessage(errorMessage).build();
	}

	protected InputDto buildInputDto() {
		return InputDto.builder().username(MAL_USERNAME).build();
	}

	protected String buildCacheKeyForUser() {
		return MAL_USERNAME;
	}

	protected Set<FandubSource> getEnabledFandubSources() {
		return fandubSupportProps.getEnabled();
	}

	private void clearCaches() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).filter(Objects::nonNull).forEach(Cache::clear);
	}
}
