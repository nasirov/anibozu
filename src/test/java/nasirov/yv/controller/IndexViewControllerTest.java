package nasirov.yv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nasirov.yv.AbstractTest;
import nasirov.yv.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class IndexViewControllerTest extends AbstractTest {

	@Test
	void shouldReturnIndex() {
		//given
		List<String> mapping = Arrays.asList("/", "/index");
		//when
		List<ResponseSpec> result = mapping.stream().map(this::call).collect(Collectors.toList());
		//then
		result.forEach(this::checkResponses);
	}

	private ResponseSpec call(String url) {
		return webTestClient.get().uri(url).exchange();
	}

	private void checkResponses(ResponseSpec responseSpec) {
		String responseBody = responseSpec.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectHeader()
				.cacheControl(CacheControl.noStore().mustRevalidate())
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();
		assertEquals(IOUtils.readFromFile("classpath:view/test-index-view.html"), responseBody);
	}
}