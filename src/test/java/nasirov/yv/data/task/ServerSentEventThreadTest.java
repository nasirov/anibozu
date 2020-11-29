package nasirov.yv.data.task;

import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.impl.common.AnimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerSentEventThreadTest {

	@Mock
	private AnimeService animeService;

	@Mock
	private MalServiceI malService;

	@Mock
	private SseEmitter sseEmitter;

	@Mock
	private UserInputDto userInputDto;

	@InjectMocks
	private ServerSentEventThread serverSentEventThread;

	@Test
	public void shouldCompleteOk() {
		//given
		mockServicesOk();
		//when
		serverSentEventThread.run();
		//then
		verifySse(1, 1, 1, 1, 1, 0);
	}

	@Test
	public void shouldCompleteWithError() {
		//given
		mockServicesException();
		//when
		serverSentEventThread.run();
		//then
		verifySse(1, 0, 0, 0, 0, 1);
	}

	private void mockServicesOk() {
		Set<FanDubSource> fanDubSources = Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME);
		mockUserInputDto(fanDubSources);
		mockMalService();
		Anime available = buildAnime(buildFanDubUrls(EventType.AVAILABLE));
		Anime notAvailable = buildAnime(buildFanDubUrls(EventType.NOT_AVAILABLE));
		Anime notFound = buildAnime(buildFanDubUrls(EventType.NOT_FOUND));
		doReturn(available, notAvailable, notFound).when(animeService)
				.buildAnime(eq(fanDubSources), any(MalTitle.class));
	}

	private void mockServicesException() {
		Set<FanDubSource> fanDubSources = Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME);
		mockUserInputDto(fanDubSources);
		mockMalService();
		Anime available = buildAnime(buildFanDubUrls(EventType.AVAILABLE));
		doReturn(available).doThrow(new RuntimeException("Exception message"))
				.when(animeService)
				.buildAnime(eq(fanDubSources), any(MalTitle.class));
	}

	private void mockUserInputDto(Set<FanDubSource> fanDubSources) {
		doReturn(TEST_ACC_FOR_DEV).when(userInputDto)
				.getUsername();
		doReturn(fanDubSources).when(userInputDto)
				.getFanDubSources();
	}

	private void mockMalService() {
		doReturn(MalUserInfo.builder()
				.username(TEST_ACC_FOR_DEV)
				.malTitles(Lists.newArrayList(new MalTitle(), new MalTitle(), new MalTitle()))
				.build()).when(malService)
				.getMalUserInfo(TEST_ACC_FOR_DEV);
	}

	@SneakyThrows
	private void verifySse(int available, int notAvailable, int notFound, int done, int complete, int completeWithError) {
		verify(sseEmitter, times(available)).send(argThat(x -> wasHandled(buildSseEvent(0,
				buildSseDto(buildAnime(buildFanDubUrls(EventType.AVAILABLE)), EventType.AVAILABLE)), x)));
		verify(sseEmitter, times(notAvailable)).send(argThat(x -> wasHandled(buildSseEvent(1,
				buildSseDto(buildAnime(buildFanDubUrls(EventType.NOT_AVAILABLE)), EventType.NOT_AVAILABLE)), x)));
		verify(sseEmitter, times(notFound)).send(argThat(x -> wasHandled(buildSseEvent(2,
				buildSseDto(buildAnime(buildFanDubUrls(EventType.NOT_FOUND)), EventType.NOT_FOUND)), x)));
		verify(sseEmitter, times(done)).send(argThat(x -> wasHandled(buildSseEvent(-1, buildSseDto(null, EventType.DONE)), x)));
		verify(sseEmitter, times(complete)).complete();
		verify(sseEmitter, times(completeWithError)).completeWithError(any(Throwable.class));
	}

	private boolean wasHandled(SseEventBuilder expectedBuilder, SseEventBuilder actualBuilder) {
		DataWithMediaType expected = Iterables.get(expectedBuilder.build(), 1);
		DataWithMediaType actual = Iterables.get(actualBuilder.build(), 1);
		return expected.getData()
				.equals(actual.getData());
	}

	private Map<FanDubSource, String> buildFanDubUrls(EventType eventType) {
		LinkedHashMap<FanDubSource, String> result = new LinkedHashMap<>();
		switch (eventType) {
			case AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ORIGINAL_NAME + "/1/1");
				result.put(FanDubSource.NINEANIME, BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
				break;
			case NOT_AVAILABLE:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
				break;
			case NOT_FOUND:
				result.put(FanDubSource.ANIMEDIA, BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL);
				result.put(FanDubSource.NINEANIME, BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL);
				break;
		}
		return result;
	}

	private Anime buildAnime(Map<FanDubSource, String> fanDubUrls) {
		return Anime.builder()
				.animeName(REGULAR_TITLE_ORIGINAL_NAME)
				.episode("1")
				.posterUrlOnMal(MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL)
				.animeUrlOnMal(MY_ANIME_LIST_URL + REGULAR_TITLE_MAL_ANIME_URL)
				.fanDubUrls(fanDubUrls)
				.build();
	}

	private SseEventBuilder buildSseEvent(int eventId, SseDto sseDto) {
		return SseEmitter.event()
				.id(String.valueOf(eventId))
				.data(sseDto);
	}

	private SseDto buildSseDto(Anime anime, EventType eventType) {
		return SseDto.builder()
				.eventType(eventType)
				.anime(anime)
				.build();
	}
}