package nasirov.yv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.REQUEST_ACCEPT_ENCODING;
import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import java.util.stream.Stream;
import nasirov.yv.data.properties.AnimediaProps;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnidubApiFeignClient;
import nasirov.yv.http.feign.AnidubSiteFeignClient;
import nasirov.yv.http.feign.AnimediaApiFeignClient;
import nasirov.yv.http.feign.AnimediaSiteFeignClient;
import nasirov.yv.http.feign.JisedaiSiteFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.parser.JisedaiParserI;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.AnimediaTitlesUpdateServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ResourcesCheckerServiceI;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import nasirov.yv.service.impl.fandub.anidub.AnidubApiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.anidub.AnidubApiGitHubResourcesService;
import nasirov.yv.service.impl.fandub.anidub.AnidubSiteEpisodeUrlService;
import nasirov.yv.service.impl.fandub.anidub.AnidubSiteGitHubResourcesService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaApiService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaSiteService;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiGitHubResourcesService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created by nasirov.yv
 */
@SpringBootTest
@AutoConfigureCache
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureWireMock(port = 0)
public abstract class AbstractTest {

	@MockBean
	protected AnimediaServiceI animediaService;

	@SpyBean
	protected WrappedObjectMapperI wrappedObjectMapper;

	@SpyBean
	protected MALServiceI malService;

	@SpyBean
	protected GitHubResourcesServiceI githubResourcesService;

	@SpyBean
	protected AnimeServiceI animeService;

	@SpyBean
	protected AnimediaEpisodeUrlService animediaEpisodeUrlService;

	@SpyBean
	protected NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService;

	@SpyBean
	protected SseEmitterExecutorServiceI sseEmitterExecutorService;

	@Autowired
	protected ResourcesNames resourcesNames;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected WireMockServer wireMockServer;

	@Autowired
	protected ResourcesCheckerServiceI resourcesCheckerService;

	@Autowired
	protected GitHubAuthProps gitHubAuthProps;

	@Autowired
	protected GitHubResourceProps gitHubResourceProps;

	@Autowired
	protected AnimediaApiFeignClient animediaApiFeignClient;

	@Autowired
	protected AnimediaSiteFeignClient animediaSiteFeignClient;

	@Autowired
	protected AnimediaProps animediaProps;

	@Autowired
	protected AnimediaTitlesUpdateServiceI animediaTitlesUpdateService;

	@Autowired
	protected AnidubApiFeignClient anidubApiFeignClient;

	@Autowired
	protected AnidubSiteFeignClient anidubSiteFeignClient;

	@Autowired
	protected JisedaiSiteFeignClient jisedaiSiteFeignClient;

	@Autowired
	protected AnidubParserI anidubParser;

	@Autowired
	protected JisedaiParserI jisedaiParser;

	@Autowired
	protected UrlsNames urlsNames;

	protected AnimediaApiService animediaApiService;

	protected AnimediaSiteService animediaSiteService;

	protected AnidubApiGitHubResourcesService anidubApiGitHubResourcesService;

	protected AnidubSiteGitHubResourcesService anidubSiteGitHubResourcesService;

	protected AnidubApiEpisodeUrlService anidubApiEpisodeUrlService;

	protected AnidubSiteEpisodeUrlService anidubSiteEpisodeUrlService;

	protected JisedaiGitHubResourcesService jisedaiGitHubResourcesService;

	protected JisedaiEpisodeUrlService jisedaiSiteEpisodeUrlService;

	@Before
	public void setUp() {
		animediaApiService = new AnimediaApiService(animediaApiFeignClient);
		animediaSiteService = new AnimediaSiteService(animediaSiteFeignClient, animediaProps);
		animediaSiteService.init();
		anidubApiGitHubResourcesService = new AnidubApiGitHubResourcesService(githubResourcesService, gitHubResourceProps);
		anidubSiteGitHubResourcesService = new AnidubSiteGitHubResourcesService(githubResourcesService, gitHubResourceProps);
		anidubApiEpisodeUrlService = new AnidubApiEpisodeUrlService(anidubApiFeignClient, anidubApiGitHubResourcesService, anidubParser);
		anidubSiteEpisodeUrlService = new AnidubSiteEpisodeUrlService(anidubSiteFeignClient, anidubSiteGitHubResourcesService, anidubParser, urlsNames);
		jisedaiGitHubResourcesService = new JisedaiGitHubResourcesService(githubResourcesService, gitHubResourceProps);
		jisedaiSiteEpisodeUrlService = new JisedaiEpisodeUrlService(jisedaiSiteFeignClient, jisedaiGitHubResourcesService, jisedaiParser, urlsNames);
	}

	@After
	public void tearDown() {
		clearResources();
	}

	protected void createStubWithBodyFile(String url, String contentType, String bodyFilePath) {
		wireMockServer.addStubMapping(get(url).withHeader(ACCEPT_ENCODING, equalTo(REQUEST_ACCEPT_ENCODING))
				.willReturn(aResponse().withHeader(CONTENT_TYPE, contentType)
						.withBodyFile(bodyFilePath))
				.build());
	}

	protected void createStubWithBodyFile(String url, String contentType, String bodyFilePath, String token) {
		wireMockServer.addStubMapping(get(url).withHeader(ACCEPT_ENCODING, equalTo(REQUEST_ACCEPT_ENCODING))
				.withHeader(AUTHORIZATION, equalTo("token " + token))
				.willReturn(aResponse().withHeader(CONTENT_TYPE, contentType)
						.withBodyFile(bodyFilePath))
				.build());
	}

	protected void createStubWithContent(String url, String contentType, String content, int status) {
		wireMockServer.addStubMapping(get(url).withHeader(ACCEPT_ENCODING, equalTo(REQUEST_ACCEPT_ENCODING))
				.willReturn(aResponse().withHeader(CONTENT_TYPE, contentType)
						.withBody(content)
						.withStatus(status))
				.build());
	}

	protected void stubAnimeMainPageAndDataLists(String animeId, String bodyFilePathForMainPage, Map<String, String> dataListAndBodyFilePath) {
		createStubWithBodyFile("/api/mobile-anime/" + animeId, APPLICATION_JSON_CHARSET_UTF_8, bodyFilePathForMainPage);
		Stream.of(dataListAndBodyFilePath)
				.flatMap(x -> x.entrySet()
						.stream())
				.forEach(x -> createStubWithBodyFile("/api/mobile-anime/" + animeId + "/" + x.getKey(), APPLICATION_JSON_CHARSET_UTF_8, x.getValue()));
	}

	private void clearResources() {
		clearAllStub();
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
