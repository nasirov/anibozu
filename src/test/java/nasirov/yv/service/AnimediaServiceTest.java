package nasirov.yv.service;

import com.sun.research.ws.wadl.HTTPMethods;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.AnimediaTitlesSearchParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

import static nasirov.yv.enums.Constants.ONLINE_ANIMEDIA_TV;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.*;

/**
 * Created by Хикка on 29.01.2019.
 */
@SpringBootTest
public class AnimediaServiceTest extends AbstractTestNGSpringContextTests {
	
	@Mock
	private HttpCaller httpCaller;
	
	@Mock
	private RequestParametersBuilder requestParametersBuilder;
	
	
	private AnimediaTitlesSearchParser animediaTitlesSearchParser;
	
	@Mock
	private AnimediaHTMLParser animediaHTMLParser;
	
	@Mock
	private URLBuilder urlBuilder;
	
//	@Mock
//	private RoutinesIO mockRoutinesIO;
	
	@Mock
	private CacheManager cacheManager;
	
	
	private WrappedObjectMapper wrappedObjectMapper;
	
	
	private RoutinesIO routinesIO;
	
	private AnimediaService animediaService;
	
	@BeforeTest
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		wrappedObjectMapper = new WrappedObjectMapper();
		animediaTitlesSearchParser=new AnimediaTitlesSearchParser(wrappedObjectMapper);
		routinesIO = new RoutinesIO(wrappedObjectMapper);
		animediaService = new AnimediaService(httpCaller, requestParametersBuilder, animediaTitlesSearchParser, animediaHTMLParser, urlBuilder, routinesIO, cacheManager);
	}
	
	@Test
	public void testGetAnimediaSearchList() throws Exception {
		doReturn(new HttpResponse(routinesIO.readFromResource("animediaSearchList.json"), HttpStatus.OK.value())).when(httpCaller).call(any(String.class), any(HTTPMethods.class), any(Map.class));
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchList();
		assertEquals(true,animediaSearchList.size() == 780);
		long count = animediaSearchList.stream().filter(set -> set.getUrl().startsWith(ONLINE_ANIMEDIA_TV.getDescription())).count();
		assertEquals(0, count);
	}
	
	@Test
	public void testGetCurrentlyUpdatedTitles() throws Exception {
	}
	
	@Test
	public void testGetSortedForSeasonAnime() throws Exception {
	}
	
	@Test
	public void testGetAnime() throws Exception {
	}
	
	@Test
	public void testCheckAnime() throws Exception {
	}
	
	@Test
	public void testCheckCurrentlyUpdatedTitles() throws Exception {
	}
}