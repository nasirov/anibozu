package nasirov.yv.ab.controller;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import nasirov.yv.ab.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class CacheControllerTest extends AbstractTest {

	@Test
	void shouldRefreshEmptyGithubCache() {
		//given
		mockGitHubResourcesService();
		Cache spiedCache = spyGithubCachePut();
		assertNull(spiedCache.get(getGithubCacheKey(), List.class));
		//when
		ResponseSpec result = call();
		waitForCachePut();
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK.value()).expectBody(String.class).isEqualTo(CacheController.INFO_MESSAGE);
		checkGithubCacheIsFilled();
	}

	@Test
	void shouldRefreshFilledGithubCache() {
		//given
		fillGithubCache();
		//when
		ResponseSpec result = call();
		waitForCachePut();
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK.value()).expectBody(String.class).isEqualTo(CacheController.INFO_MESSAGE);
		checkGithubCacheIsFilled();
	}

	private ResponseSpec call() {
		return webTestClient.post().uri("/refresh/cache/github").exchange();
	}
}