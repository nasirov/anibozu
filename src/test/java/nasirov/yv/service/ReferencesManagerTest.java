package nasirov.yv.service;

import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by nasirov.yv
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		WrappedObjectMapper.class,
		RoutinesIO.class,
		AnimediaRequestParametersBuilder.class,
		AnimediaHTMLParser.class})
@TestPropertySource(locations = "classpath:system.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReferencesManagerTest {
	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;
	
	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;
	
	@Value("classpath:${resources.rawReference.name}")
	private Resource rawReferencesResource;
	
	@MockBean
	private HttpCaller httpCaller;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	@Test
	public void getMultiSeasonsReferences() throws Exception {
	
	}
	
	@Test
	public void updateReferences() throws Exception {
	}
	
	@Test
	public void getMatchedReferences() throws Exception {
	}
	
	@Test
	public void checkReferences() throws Exception {
	}
	
	@Test
	public void updateReferences1() throws Exception {
	}
}