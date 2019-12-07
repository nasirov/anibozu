package nasirov.yv.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;
import nasirov.yv.AbstractTest;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Created by nasirov.yv
 */


public class IndexControllerTest extends AbstractTest {

	private static final String INDEX = "index";

	@Test
	public void index() throws Exception {
		List<String> mapping = Arrays.asList("/", "/index");
		for (String url : mapping) {
			mockMvc.perform(get(url))
					.andExpect(status().isOk())
					.andExpect(view().name(INDEX))
					.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
		}
	}
}