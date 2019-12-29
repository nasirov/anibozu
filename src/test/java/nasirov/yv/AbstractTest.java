package nasirov.yv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static nasirov.yv.utils.TestConstants.TEXT_JAVASCRIPT_CHARSET_UTF_8;
import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import java.util.stream.Stream;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.RepositoryCheckerServiceI;
import nasirov.yv.service.ResourcesCheckerServiceI;
import nasirov.yv.service.SeasonsAndEpisodesServiceI;
import nasirov.yv.util.RoutinesIO;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

	@SpyBean
	protected AnimediaServiceI animediaService;

	@SpyBean
	protected MALServiceI malService;

	@SpyBean
	protected SeasonsAndEpisodesServiceI seasonsAndEpisodesService;

	@SpyBean
	protected ReferencesServiceI referencesService;

	@Autowired
	protected ResourcesNames resourcesNames;

	@Autowired
	protected RoutinesIO routinesIO;

	@Autowired
	protected WrappedObjectMapperI wrappedObjectMapper;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected WireMockServer wireMockServer;

	@Autowired
	protected ResourcesCheckerServiceI resourcesCheckerService;

	@Autowired
	protected NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@Autowired
	protected RepositoryCheckerServiceI repositoryCheckerService;

	@Before
	public void setUp() {
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

	protected void stubAnimeMainPageAndDataLists(String animeId, String bodyFilePathForMainPage, Map<String, String> dataListAndBodyFilePath) {
		createStubWithBodyFile("/api/mobile-anime/" + animeId, TEXT_JAVASCRIPT_CHARSET_UTF_8, bodyFilePathForMainPage);
		Stream.of(dataListAndBodyFilePath)
				.flatMap(x -> x.entrySet()
						.stream())
				.forEach(x -> createStubWithBodyFile("/api/mobile-anime/" + animeId + "/" + x.getKey(), TEXT_JAVASCRIPT_CHARSET_UTF_8, x.getValue()));
	}

	private void clearResources() {
		clearAllStub();
		notFoundAnimeOnAnimediaRepository.deleteAll();
	}

	private void clearAllStub() {
		wireMockServer.resetAll();
	}
}
