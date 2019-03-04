package nasirov.yv.controller;

import nasirov.yv.AbstractTest;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by nasirov.yv
 */
@ContextConfiguration(classes = {
		IndexController.class
})
@WebMvcTest(IndexController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IndexControllerTest extends AbstractTest {
	private static final String INDEX = "index.html";
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void index() throws Exception {
		List<String> mapping = Arrays.asList("/", "/index");
		for (String url : mapping) {
			MockHttpServletResponse response = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse();
			assertEquals(INDEX, response.getForwardedUrl());
		}
	}
}