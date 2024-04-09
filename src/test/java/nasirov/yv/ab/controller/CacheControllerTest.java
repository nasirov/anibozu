package nasirov.yv.ab.controller;

import static org.junit.jupiter.api.Assertions.assertNull;

import nasirov.yv.ab.AbstractTest;
import nasirov.yv.ab.dto.internal.FandubData;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/**
 * @author Nasirov Yuriy
 */
class CacheControllerTest extends AbstractTest {

	private static final String CACHE_REFRESHED_MESSAGE = "github cache has been refreshed.";

	@Test
	void shouldRefreshEmptyGithubCache() {
		//given
		mockCompiledAnimeResourcesService();
		Cache githubCache = getGithubCache();
		assertNull(githubCache.get(getGithubCacheKey(), FandubData.class));
		//when
		ResponseSpec result = call();
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK.value()).expectBody(String.class).isEqualTo(CACHE_REFRESHED_MESSAGE);
		checkGithubCacheIsFilled();
	}

	@Test
	void shouldRefreshFilledGithubCache() {
		//given
		fillGithubCache();
		//when
		ResponseSpec result = call();
		//then
		result.expectStatus().isEqualTo(HttpStatus.OK.value()).expectBody(String.class).isEqualTo(CACHE_REFRESHED_MESSAGE);
		checkGithubCacheIsFilled();
	}

	private ResponseSpec call() {
		return webTestClient.post().uri("/refresh/cache/github").exchange();
	}
}