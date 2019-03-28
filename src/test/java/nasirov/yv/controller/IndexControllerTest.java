package nasirov.yv.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;
import nasirov.yv.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created by nasirov.yv
 */
@ContextConfiguration(classes = {IndexController.class, IndexFilter.class, ResourceUrlAdvice.class})
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
			mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name(INDEX))
					.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
		}
	}
}