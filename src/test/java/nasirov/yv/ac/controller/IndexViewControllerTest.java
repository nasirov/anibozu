package nasirov.yv.ac.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nasirov.yv.ac.AbstractTest;
import nasirov.yv.ac.utils.IOUtils;
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
		//when
		ResponseSpec result = webTestClient.get().uri("/").exchange();
		//then
		String responseBody = result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectHeader()
				.cacheControl(CacheControl.noStore().mustRevalidate())
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();
		assertEquals(IOUtils.readFromFile("classpath:view/test-index-view.html"), responseBody);
	}
}