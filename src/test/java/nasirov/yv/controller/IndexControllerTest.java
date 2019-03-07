package nasirov.yv.controller;

import nasirov.yv.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by nasirov.yv
 */
@ContextConfiguration(classes = {
		IndexController.class
})
@WebMvcTest(IndexController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IndexControllerTest extends AbstractTest {
	private static final String INDEX = "index";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void index() throws Exception {
		List<String> mapping = Arrays.asList("/", "/index");
		for (String url : mapping) {
			mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name(INDEX));
		}
	}
}