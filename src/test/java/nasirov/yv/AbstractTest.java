package nasirov.yv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static nasirov.yv.data.constants.CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import java.util.stream.Stream;
import nasirov.yv.data.constants.CacheNamesConstants;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.ResourcesServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import nasirov.yv.service.scheduler.CacheCleanerService;
import nasirov.yv.service.scheduler.ResourcesCheckerService;
import nasirov.yv.util.RoutinesIO;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created by nasirov.yv
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureWireMock(port = 0)
public abstract class AbstractTest {

	@Value("classpath:__files/animedia/search/singleSeasonsAnimeUrls.json")
	protected Resource singleSeasonsAnimeUrls;

	@Value("classpath:__files/animedia/search/announcements.json")
	protected Resource announcementsUrls;

	@Value("classpath:__files/animedia/search/multiSeasonsAnimeUrls.json")
	protected Resource multiSeasonsAnimeUrls;

	@Value("classpath:__files/animedia/sao/saoHtml.txt")
	protected Resource saoHtml;

	@Value("classpath:__files/animedia/sao/sao1.txt")
	protected Resource saoDataList1;

	@Value("classpath:__files/animedia/sao/sao7.txt")
	protected Resource saoDataList7;

	@Value("classpath:__files/animedia/search/animediaMainPage.txt")
	protected Resource pageWithCurrentlyAddedEpisodes;

	@Value("classpath:__files/animedia/announcements/htmlWithAnnouncement.txt")
	protected Resource htmlWithAnnouncement;

	@Value("classpath:__files/animedia/search/animediaSearchListForCheck.json")
	protected Resource animediaSearchListForCheck;

	@Value("classpath:__files/animedia/onePiece2.txt")
	protected Resource onePieceDataList2;

	@Value("classpath:__files/animedia/announcements/dataListWithTrailer.txt")
	protected Resource dataListWithTrailer;

	@Autowired
	protected ResourcesNames resourcesNames;

	@Autowired
	protected RoutinesIO routinesIO;

	@Autowired
	protected WrappedObjectMapperI wrappedObjectMapperI;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected CacheCleanerService cacheCleanerService;

	@Autowired
	protected WireMockServer wireMockServer;

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected ResourcesCheckerService resourcesCheckerService;

	@Autowired
	protected NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@SpyBean
	protected ResourcesServiceI resourcesService;

	@SpyBean
	protected AnimediaServiceI animediaService;

	@SpyBean
	protected MALServiceI malService;

	@SpyBean
	protected SeasonsAndEpisodesServiceI seasonAndEpisodeChecker;

	@SpyBean
	protected ReferencesServiceI referencesManager;

	protected Cache userMALCache;

	protected Cache userMatchedAnimeCache;

	protected Cache currentlyUpdatedTitlesCache;

	protected Cache sortedAnimediaSearchListCache;

	@Before
	public void setUp() {
		userMALCache = cacheManager.getCache(CacheNamesConstants.USER_MAL_CACHE);
		userMatchedAnimeCache = cacheManager.getCache(CacheNamesConstants.USER_MATCHED_ANIME_CACHE);
		currentlyUpdatedTitlesCache = cacheManager.getCache(CURRENTLY_UPDATED_TITLES_CACHE);
		sortedAnimediaSearchListCache = cacheManager.getCache(CacheNamesConstants.SORTED_ANIMEDIA_SEARCH_LIST_CACHE);
		clearResources();
	}

	@After
	public void tearDown() {
		clearResources();
	}

	protected void createStubWithBodyFile(String url, String contentType, String bodyFilePath) {
		wireMockServer.addStubMapping(get(url).withHeader(ACCEPT_ENCODING, equalTo("gzip, deflate, br"))
				.willReturn(aResponse().withHeader(CONTENT_TYPE, contentType)
						.withBodyFile(bodyFilePath))
				.build());
	}

	protected void createStubWithContent(String url, String contentType, String content, int status) {
		wireMockServer.addStubMapping(get(url).withHeader(ACCEPT_ENCODING, equalTo("gzip, deflate, br"))
				.willReturn(aResponse().withHeader(CONTENT_TYPE, contentType)
						.withBody(content)
						.withStatus(status))
				.build());
	}

	protected void stubAnimeMainPageAndDataLists(String url, String bodyFilePathForMainPage, String animeId,
			Map<String, String> dataListAndBodyFilePath) {
		createStubWithBodyFile("/" + url, TEXT_PLAIN_CHARSET_UTF_8, bodyFilePathForMainPage);
		Stream.of(dataListAndBodyFilePath)
				.flatMap(x -> x.entrySet()
						.stream())
				.forEach(x -> createStubWithBodyFile("/ajax/episodes/" + animeId + "/" + x.getKey() + "/undefined", TEXT_PLAIN_CHARSET_UTF_8, x.getValue()));
	}

	private void clearResources() {
		clearAllStub();
		notFoundAnimeOnAnimediaRepository.deleteAll();
		clearCache(userMALCache);
		clearCache(userMatchedAnimeCache);
		clearCache(currentlyUpdatedTitlesCache);
		clearCache(sortedAnimediaSearchListCache);
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}

	private void clearCache(Cache cache) {
		if (cache != null) {
			cache.clear();
		}
	}
}
