package nasirov.yv;

import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_ID;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnidubParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnilibriaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimediaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnythingGroupParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JamClubParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JutsuParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.NineAnimeParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.ShizaProjectParserI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveAnythingGroupServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveJamClubServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveNineAnimeServiceI;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ServerSentEventServiceI;
import nasirov.yv.service.impl.common.CacheCleanerService;
import nasirov.yv.util.MalUtils;
import nasirov.yv.utils.CommonTitleTestBuilder;
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

	@SpyBean
	protected AnidubParserI anidubParser;

	@SpyBean
	protected AnilibriaParserI anilibriaParser;

	@SpyBean
	protected AnimediaParserI animediaParser;

	@SpyBean
	protected AnimepikParserI animepikParser;

	@SpyBean
	protected AnythingGroupParserI anythingGroupParser;

	@SpyBean
	protected JamClubParserI jamClubParser;

	@SpyBean
	protected JisedaiParserI jisedaiParser;

	@SpyBean
	protected JutsuParserI jutsuParser;

	@SpyBean
	protected ShizaProjectParserI shizaProjectParser;

	@SpyBean
	protected NineAnimeParserI nineAnimeParser;

	@SpyBean
	protected SovetRomanticaParserI sovetRomanticaParser;

	@SpyBean
	protected HttpRequestServiceI httpRequestService;

	@SpyBean
	protected ReactiveJamClubServiceI reactiveJamClubService;

	@SpyBean
	protected ReactiveAnythingGroupServiceI reactiveAnythingGroupService;

	@SpyBean
	protected ReactiveNineAnimeServiceI reactiveNineAnimeService;

	@SpyBean
	protected AnimeServiceI animeService;

	@SpyBean
	protected ServerSentEventServiceI serverSentEventService;

	@SpyBean
	protected CacheCleanerService cacheCleanerService;

	@SpyBean
	protected CommonProps commonProps;

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected ExternalServicesProps externalServicesProps;

	@Autowired
	protected FanDubProps fanDubProps;

	@Autowired
	protected Map<FanDubSource, EpisodeNameAndUrlServiceI> episodeNameAndUrlServiceStrategy;

	@Autowired
	protected MalServiceI malService;

	@Autowired
	protected HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	protected WebTestClient webTestClient;

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
				.build();
	}

	@AfterEach
	void tearDown() {
		clearCaches();
	}

	protected void mockExternalFandubTitlesServiceResponse(MalTitle malTitle, Map<FanDubSource, List<CommonTitle>> commonTitles,
			Set<FanDubSource> fanDubSources) {
		doReturn(Mono.just(commonTitles)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getFandubTitlesServiceUrl() + "titles?fanDubSources=" + fanDubSources.stream()
								.map(FanDubSource::name)
								.collect(Collectors.joining(",")) + "&malId=" + malTitle.getId() + "&malEpisodeId=" + MalUtils.getNextEpisodeForWatch(malTitle))));
	}

	protected void mockExternalMalServiceResponse(MalServiceResponseDto malServiceResponseDto) {
		doReturn(Mono.just(malServiceResponseDto)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + TEST_ACC_FOR_DEV + "&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}

	protected void mockHttpRequestServiceException() {
		doThrow(new RuntimeException("foo bar cause")).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getMalServiceUrl() + "titles?username=" + TEST_ACC_FOR_DEV + "&status="
								+ MalTitleWatchingStatus.WATCHING.name())));
	}

	protected Map<FanDubSource, List<CommonTitle>> buildRegularCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Lists.newArrayList(CommonTitleTestBuilder.buildRegularTitle(x))));
	}

	protected Map<FanDubSource, List<CommonTitle>> buildConcretizedCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Lists.newArrayList(CommonTitleTestBuilder.buildConcretizedTitle(x))));
	}

	protected Map<FanDubSource, List<CommonTitle>> buildNotFoundOnFandubCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Collections.emptyList()));
	}

	protected MalTitle buildRegularTitle() {
		return buildWatchingTitle(REGULAR_TITLE_ORIGINAL_NAME, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL, REGULAR_TITLE_MAL_ID, 0);
	}

	protected MalTitle buildRegularTitle(int numWatchedEpisodes) {
		return buildWatchingTitle(REGULAR_TITLE_ORIGINAL_NAME,
				REGULAR_TITLE_POSTER_URL,
				REGULAR_TITLE_MAL_ANIME_URL,
				REGULAR_TITLE_MAL_ID,
				numWatchedEpisodes);
	}

	protected MalTitle buildConcretizedTitle() {
		return buildWatchingTitle(CONCRETIZED_TITLE_ORIGINAL_NAME,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL,
				CONCRETIZED_TITLE_MAL_ID,
				10);
	}

	protected MalTitle buildNotFoundOnFandubTitle() {
		return buildWatchingTitle(NOT_FOUND_ON_FANDUB_TITLE_ORIGINAL_NAME,
				NOT_FOUND_ON_FANDUB_TITLE_POSTER_URL,
				NOT_FOUND_ON_FANDUB_TITLE_MAL_ANIME_URL,
				NOT_FOUND_ON_FANDUB_TITLE_ID,
				0);
	}

	protected MalTitle buildWatchingTitle(String titleName, String posterUrl, String animeUrl, int id, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(id)
				.numWatchedEpisodes(numWatchedEpisodes)
				.name(titleName)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl)
				.animeUrl(MY_ANIME_LIST_URL + animeUrl)
				.build();
	}

	protected MalServiceResponseDto buildMalServiceResponseDto(List<MalTitle> malTitles, String errorMessage) {
		return MalServiceResponseDto.builder()
				.username(TEST_ACC_FOR_DEV)
				.malTitles(malTitles)
				.errorMessage(errorMessage)
				.build();
	}

	protected UserInputDto buildUserInputDto() {
		return UserInputDto.builder()
				.username(TEST_ACC_FOR_DEV)
				.fanDubSources(Sets.newLinkedHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME))
				.build();
	}

	private void clearCaches() {
		cacheManager.getCacheNames()
				.stream()
				.map(cacheManager::getCache)
				.filter(Objects::nonNull)
				.forEach(Cache::clear);
	}
}
