package nasirov.yv;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.service.ServerSentEventServiceI;
import nasirov.yv.service.TitleServiceI;
import nasirov.yv.service.impl.common.CacheCleanerService;
import org.assertj.core.util.Sets;
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
	protected TitleServiceI titleService;

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

	@Autowired
	protected HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Autowired
	protected CacheProps cacheProps;

	protected WebTestClient webTestClient;

	protected Set<FanDubSource> inputFanDubSources = Sets.newLinkedHashSet(FanDubSource.ANIDUB, FanDubSource.ANILIBRIA);

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
	}

	@AfterEach
	void tearDown() {
		clearCaches();
	}

	protected void mockExternalMalServiceResponse(MalServiceResponseDto malServiceResponseDto) {
		doReturn(Mono.just(malServiceResponseDto)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + MAL_USERNAME + "&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}

	protected MalServiceResponseDto buildMalServiceResponseDto(List<MalTitle> malTitles, String errorMessage) {
		return MalServiceResponseDto.builder().username(MAL_USERNAME).malTitles(malTitles).errorMessage(errorMessage).build();
	}

	protected UserInputDto buildUserInputDto() {
		return UserInputDto.builder().username(MAL_USERNAME).fanDubSources(inputFanDubSources).build();
	}

	protected String buildCacheKeyForUser() {
		StringJoiner stringJoiner = new StringJoiner(",", ":", "");
		inputFanDubSources.stream().map(FanDubSource::name).forEach(stringJoiner::add);
		return stringJoiner.toString();
	}

	private void clearCaches() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).filter(Objects::nonNull).forEach(Cache::clear);
	}
}
