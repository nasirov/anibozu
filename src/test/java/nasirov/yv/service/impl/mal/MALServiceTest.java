package nasirov.yv.service.impl.mal;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.exception.mal.MalForbiddenException;
import nasirov.yv.exception.mal.MalUserAccountNotFoundException;
import nasirov.yv.exception.mal.MalUserAnimeListAccessException;
import nasirov.yv.exception.mal.UnexpectedCallingException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

/**
 * Created by nasirov.yv
 */
public class MALServiceTest extends AbstractTest {

	@Test
	@SneakyThrows
	public void shouldReturnWatchingTitles() {
		//given
		stubGetUserProfile(ResponseEntity.ok(IOUtils.readFromFile("classpath:__files/mal/testAccForDevProfile.html")));
		stubGetUserAnimeList(ResponseEntity.status(OK)
				.body(IOUtils.unmarshal(IOUtils.readFromFile("classpath:__files/mal/testAccForDevFirstJson300.json"), MalTitle.class, ArrayList.class)));
		List<MalTitle> expectedTitles = buildExpectedTitles();
		//when
		List<MalTitle> watchingTitles = malService.getWatchingTitles(TEST_ACC_FOR_DEV);
		//then
		assertNotNull(watchingTitles);
		assertEquals(TEST_ACC_WATCHING_TITLES, watchingTitles.size());
		watchingTitles.forEach(x -> assertTrue(expectedTitles.contains(x)));
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	@SneakyThrows
	public void shouldFailOnWatchingTitlesNotFound() {
		//given
		stubGetUserProfile(ResponseEntity.status(OK)
				.body("Watching</a><span class=\"changed-class\">123</span>"));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = WatchingTitlesNotFoundException.class)
	@SneakyThrows
	public void shouldFailOnWatchingTitlesEqualsZero() {
		//given
		stubGetUserProfile(ResponseEntity.status(OK)
				.body(IOUtils.readFromFile("classpath:__files/mal/testAccForDevProfileWatchingTitles0.html")));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalUserAccountNotFoundException.class)
	@SneakyThrows
	public void shouldFailOnUserAccountNotFound() {
		//given
		stubGetUserProfile(ResponseEntity.status(NOT_FOUND)
				.body(""));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalForbiddenException.class)
	@SneakyThrows
	public void shouldFailOnMALForbiddenException() {
		//given
		stubGetUserProfile(ResponseEntity.status(FORBIDDEN)
				.body(""));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = UnexpectedCallingException.class)
	@SneakyThrows
	public void shouldFailOnUnexpectedCallingException() {
		//given
		stubGetUserProfile(ResponseEntity.status(GATEWAY_TIMEOUT)
				.body(""));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalUserAnimeListAccessException.class)
	@SneakyThrows
	public void shouldFailOnMalUserAnimeListAccessException() {
		//given
		stubGetUserProfile(ResponseEntity.ok(IOUtils.readFromFile("classpath:__files/mal/testAccForDevProfile.html")));
		stubGetUserAnimeList(ResponseEntity.status(BAD_REQUEST)
				.body("{\"errors\":[{\"message\":\"invalid request\"}]}"));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = UnexpectedCallingException.class)
	@SneakyThrows
	public void shouldFailOnUnexpectedCallingExceptionOnAnimeList() {
		//given
		stubGetUserProfile(ResponseEntity.ok(IOUtils.readFromFile("classpath:__files/mal/testAccForDevProfile.html")));
		stubGetUserAnimeList(ResponseEntity.status(INTERNAL_SERVER_ERROR)
				.body("{\"errors\":[{\"message\":\"invalid request\"}]}"));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	private void stubGetUserProfile(ResponseEntity<String> response) {
		doReturn(response).when(malFeignClient)
				.getUserProfile(TEST_ACC_FOR_DEV);
	}

	private void stubGetUserAnimeList(ResponseEntity<Object> response) {
		doReturn(response).when(malFeignClient)
				.getUserAnimeList(TEST_ACC_FOR_DEV, 0, 1);
	}

	private ArrayList<MalTitle> buildExpectedTitles() {
		return Lists.newArrayList(MalTitle.builder()
						.id(1)
						.name("foo")
						.animeUrl("https://myanimelist.net/anime/1/Foo")
						.numWatchedEpisodes(42)
						.posterUrl("https://cdn.myanimelist.net/images/anime/2/79900.jpg")
						.build(),
				MalTitle.builder()
						.id(2)
						.name("bar")
						.animeUrl("https://myanimelist.net/anime/2/Bar")
						.numWatchedEpisodes(42)
						.posterUrl("https://cdn.myanimelist.net/images/anime/4/23083.jpg")
						.build());
	}
}