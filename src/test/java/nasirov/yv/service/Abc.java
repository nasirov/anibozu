//package nasirov.yv.service;
//
//import com.sun.research.ws.wadl.HTTPMethods;
//import nasirov.yv.configuration.AppConfiguration;
//import nasirov.yv.http.HttpCaller;
//import nasirov.yv.parameter.RequestParametersBuilder;
//import nasirov.yv.parser.AnimediaHTMLParser;
//import nasirov.yv.parser.AnimediaTitlesSearchParser;
//import nasirov.yv.parser.WrappedObjectMapper;
//import nasirov.yv.response.HttpResponse;
//import nasirov.yv.serialization.AnimediaTitleSearchInfo;
//import nasirov.yv.util.RoutinesIO;
//import nasirov.yv.util.URLBuilder;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
//import org.springframework.cache.CacheManager;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.context.TestExecutionListeners;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Set;
//
//import static nasirov.yv.enums.Constants.ONLINE_ANIMEDIA_TV;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertNotNull;
//
///**
// * Created by nasirov.yv
// */
//@SpringBootTest(classes = {AnimediaService.class, AnimediaTitlesSearchParser.class, AnimediaHTMLParser.class, WrappedObjectMapper.class, CacheManager.class, AppConfiguration.class})
//@TestExecutionListeners(MockitoTestExecutionListener.class)
//@TestPropertySource(locations = "classpath:system.properties")
//public class Abc extends AbstractTestNGSpringContextTests{
//
//	@Value("${cache.animediaSearchList.name}")
//	private String animediaSearchListCacheName;
//
//	@Value("${cache.currentlyUpdatedTitles.name}")
//	private String currentlyUpdatedTitlesCacheName;
//
//	@Value("classpath:pageWithCurrentlyAddedEpisodes.txt")
//	private Resource pageWithCurrentlyAddedEpisodes;
//
//	@Value("classpath:animediaSearchListFull.json")
//	private Resource animediaSearchList;
//
//	@MockBean
//	private HttpCaller httpCaller;
//
//	@MockBean
//	@Qualifier("animediaRequestParametersBuilder")
//	private RequestParametersBuilder requestParametersBuilder;
//
//	@Autowired
//	private AnimediaTitlesSearchParser animediaTitlesSearchParser;
//
//	@Autowired
//	private AnimediaHTMLParser animediaHTMLParser;
//
//	@MockBean
//	private URLBuilder urlBuilder;
//
//	@Autowired
//	private CacheManager cacheManager;
//
//	@Autowired
//	private WrappedObjectMapper wrappedObjectMapper;
//
//	@MockBean
//	private RoutinesIO routinesIOMock;
//
//	private RoutinesIO routinesIO;
//
//	@Autowired
//	private AnimediaService animediaService;
//
//	@BeforeTest
//	public void setUp() {
//		MockitoAnnotations.initMocks(this);
//		routinesIO = new RoutinesIO(new WrappedObjectMapper());
//	}
//
//	@Test
//	public void test1() throws IOException {
////		doReturn(new HttpResponse(routinesIO.readFromFile(animediaSearchList.getFile()), HttpStatus.OK.value())).when(httpCaller).call(any(String.class), any(HTTPMethods.class), any(Map.class));
////		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
////		assertNotNull(animediaSearchList);
////		assertEquals(animediaSearchList.size(), 780);
////		long count = animediaSearchList.stream().filter(set -> set.getUrl().startsWith(ONLINE_ANIMEDIA_TV.getDescription())).count();
////		assertEquals(count, 0);
//	}
//
//
//}
