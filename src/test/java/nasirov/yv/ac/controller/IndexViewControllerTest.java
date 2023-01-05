package nasirov.yv.ac.controller;

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
		result.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectHeader()
				.cacheControl(CacheControl.noStore().mustRevalidate())
				.expectBody(String.class)
				.isEqualTo(IOUtils.readFromFile("classpath:__files/view/test-index-view.html"));
	}
}