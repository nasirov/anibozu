package nasirov.yv;

import nasirov.yv.data.properties.AnimediaProps;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.fandub.service.spring.boot.starter.service.WrappedObjectMapperI;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.parser.AnilibriaParserI;
import nasirov.yv.parser.AnimepikParserI;
import nasirov.yv.parser.JisedaiParserI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.AnimediaTitlesUpdateServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ResourcesCheckerServiceI;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import nasirov.yv.service.impl.fandub.animedia.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
public abstract class AbstractTest {

	@MockBean
	protected AnimediaServiceI animediaService;

	@SpyBean
	protected WrappedObjectMapperI wrappedObjectMapper;

	@SpyBean
	protected MalServiceI malService;

	@SpyBean
	protected GitHubResourcesServiceI githubResourcesService;

	@SpyBean
	protected SseEmitterExecutorServiceI sseEmitterExecutorService;

	@Autowired
	protected AnimediaEpisodeUrlService animediaEpisodeUrlService;

	@Autowired
	protected NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService;

	@Autowired
	protected ResourcesNames resourcesNames;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ResourcesCheckerServiceI resourcesCheckerService;

	@Autowired
	protected GitHubAuthProps gitHubAuthProps;

	@Autowired
	protected GitHubResourceProps gitHubResourceProps;

	@Autowired
	protected AnimediaProps animediaProps;

	@Autowired
	protected AnimediaTitlesUpdateServiceI animediaTitlesUpdateService;

	@Autowired
	protected AnidubParserI anidubParser;

	@Autowired
	protected JisedaiParserI jisedaiParser;

	@Autowired
	protected AnimepikParserI animepikParser;

	@Autowired
	protected AnilibriaParserI anilibriaParser;

	@Autowired
	protected UrlsNames urlsNames;

	@Before
	public void setUp() {
	}
}
