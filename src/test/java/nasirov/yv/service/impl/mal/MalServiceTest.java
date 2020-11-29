package nasirov.yv.service.impl.mal;

import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static nasirov.yv.utils.TestConstants.TEST_ACC_WATCHING_TITLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.data.properties.MalProps;
import nasirov.yv.exception.mal.MalForbiddenException;
import nasirov.yv.exception.mal.MalUserAccountNotFoundException;
import nasirov.yv.exception.mal.MalUserAnimeListAccessException;
import nasirov.yv.exception.mal.UnexpectedCallingException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.mal.MalFeignClient;
import nasirov.yv.parser.MalParserI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class MalServiceTest {

	@Mock
	private MalFeignClient malFeignClient;

	@Mock
	private MalParserI malParser;

	@Mock
	private MalProps malProps;

	@Spy
	@InjectMocks
	private MalService malService;

	@Before
	public void setUp() {
		mockMalProps();
		malService.init();
	}

	@Test
	@SneakyThrows
	public void shouldReturnWatchingTitles() {
		//given
		String userProfileHtml = "foobar";
		mockGetUserProfile(ResponseEntity.ok(userProfileHtml));
		mockParser(TEST_ACC_WATCHING_TITLES, userProfileHtml);
		mockGetUserAnimeList(ResponseEntity.ok(buildOriginalTitles()));
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
		String userProfileHtml = "foobar";
		mockGetUserProfile(ResponseEntity.ok(userProfileHtml));
		mockParser(0, userProfileHtml);
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalUserAccountNotFoundException.class)
	@SneakyThrows
	public void shouldFailOnUserAccountNotFound() {
		//given
		String userProfileHtml = "";
		mockGetUserProfile(ResponseEntity.status(NOT_FOUND)
				.body(userProfileHtml));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalForbiddenException.class)
	@SneakyThrows
	public void shouldFailOnMalForbiddenException() {
		//given
		String userProfileHtml = "";
		mockGetUserProfile(ResponseEntity.status(FORBIDDEN)
				.body(userProfileHtml));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = UnexpectedCallingException.class)
	@SneakyThrows
	public void shouldFailOnUnexpectedCallingException() {
		//given
		String userProfileHtml = "";
		mockGetUserProfile(ResponseEntity.status(GATEWAY_TIMEOUT)
				.body(userProfileHtml));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = MalUserAnimeListAccessException.class)
	@SneakyThrows
	public void shouldFailOnMalUserAnimeListAccessException() {
		//given
		String userProfileHtml = "foobar";
		mockGetUserProfile(ResponseEntity.ok(userProfileHtml));
		mockParser(TEST_ACC_WATCHING_TITLES, userProfileHtml);
		mockGetUserAnimeList(ResponseEntity.status(BAD_REQUEST)
				.body(Collections.emptyList()));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test(expected = UnexpectedCallingException.class)
	@SneakyThrows
	public void shouldFailOnUnexpectedCallingExceptionOnAnimeList() {
		//given
		String userProfileHtml = "foobar";
		mockGetUserProfile(ResponseEntity.ok(userProfileHtml));
		mockParser(TEST_ACC_WATCHING_TITLES, userProfileHtml);
		mockGetUserAnimeList(ResponseEntity.status(INTERNAL_SERVER_ERROR)
				.body(Collections.emptyList()));
		//when
		malService.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@Test
	public void shouldReturnMalUserInfo() {
		//given
		List<MalTitle> malTitles = Lists.newArrayList(new MalTitle());
		mockGetWatchingTitles(malTitles);
		//when
		MalUserInfo result = malService.getMalUserInfo(TEST_ACC_FOR_DEV);
		//then
		assertEquals(TEST_ACC_FOR_DEV, result.getUsername());
		assertEquals(malTitles, result.getMalTitles());
		assertNull(result.getErrorMessage());
	}

	@Test
	public void shouldReturnMalUserInfoWithMalErrorMessage() {
		//given
		List<MalTitle> malTitles = Collections.emptyList();
		String errorMessage = "Error message because of invalid mal responses";
		mockGetWatchingTitles(new MalUserAccountNotFoundException(errorMessage));
		//when
		MalUserInfo result = malService.getMalUserInfo(TEST_ACC_FOR_DEV);
		//then
		assertEquals(TEST_ACC_FOR_DEV, result.getUsername());
		assertEquals(malTitles, result.getMalTitles());
		assertEquals(errorMessage, result.getErrorMessage());
	}

	@Test
	public void shouldReturnMalUserInfoWithCommonErrorMessage() {
		//given
		List<MalTitle> malTitles = Collections.emptyList();
		String errorMessage = "Sorry, testaccfordev, unexpected error has occurred.";
		mockGetWatchingTitles(new RuntimeException("Some unexpected error"));
		//when
		MalUserInfo result = malService.getMalUserInfo(TEST_ACC_FOR_DEV);
		//then
		assertEquals(TEST_ACC_FOR_DEV, result.getUsername());
		assertEquals(malTitles, result.getMalTitles());
		assertEquals(errorMessage, result.getErrorMessage());
	}

	private void mockMalProps() {
		doReturn(2).when(malProps)
				.getOffsetStep();
		doReturn(MY_ANIME_LIST_URL).when(malProps)
				.getUrl();
	}

	private void mockGetUserProfile(ResponseEntity<String> response) {
		doReturn(response).when(malFeignClient)
				.getUserProfile(TEST_ACC_FOR_DEV);
	}

	private void mockParser(int watchingTitles, String userProfileHtml) {
		doReturn(watchingTitles).when(malParser)
				.getNumWatchingTitles(userProfileHtml);
	}

	private void mockGetUserAnimeList(ResponseEntity<List<MalTitle>> response) {
		doReturn(response).when(malFeignClient)
				.getUserAnimeList(TEST_ACC_FOR_DEV, 0, 1);
	}

	private List<MalTitle> buildOriginalTitles() {
		return Lists.newArrayList(MalTitle.builder()
						.id(1)
						.name("Foo")
						.animeUrl("/anime/1/Foo")
						.numWatchedEpisodes(42)
						.posterUrl("https://cdn.myanimelist.net/r/96x136/images/anime/2/79900.jpg?s=0c645b8c2a73c6f3efdf3840d97cac41")
						.build(),
				MalTitle.builder()
						.id(2)
						.name("Bar")
						.animeUrl("/anime/2/Bar")
						.numWatchedEpisodes(42)
						.posterUrl("https://cdn.myanimelist.net/r/96x136/images/anime/4/23083.jpg?s=6642056737a33b71f58c44927aeb4a7a")
						.build());
	}

	private List<MalTitle> buildExpectedTitles() {
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

	@SneakyThrows
	private void mockGetWatchingTitles(List<MalTitle> malTitles) {
		doReturn(malTitles).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV);
	}

	@SneakyThrows
	private void mockGetWatchingTitles(Exception exception) {
		doThrow(exception).when(malService)
				.getWatchingTitles(TEST_ACC_FOR_DEV);
	}
}