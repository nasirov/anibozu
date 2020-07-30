package nasirov.yv.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by nasirov.yv
 */
public class IndexControllerTest extends AbstractTest {

	private static final String INDEX = "index";

	@Test
	public void shouldReturnIndex() {
		//given
		List<String> mapping = Arrays.asList("/", "/index");
		//when
		List<MvcResult> result = mapping.stream()
				.map(this::getMvcResult)
				.collect(Collectors.toList());
		//then
		result.forEach(x -> {
			assertEquals(HttpStatus.OK.value(),
					x.getResponse()
							.getStatus());
			ModelAndView modelAndView = x.getModelAndView();
			assertNotNull(modelAndView);
			assertEquals(INDEX, modelAndView.getViewName());
			assertEquals("no-cache, no-store, must-revalidate",
					x.getResponse()
							.getHeader(HttpHeaders.CACHE_CONTROL));
		});
	}

	@SneakyThrows
	private MvcResult getMvcResult(String url) {
		return mockMvc.perform(get(url))
				.andReturn();
	}
}