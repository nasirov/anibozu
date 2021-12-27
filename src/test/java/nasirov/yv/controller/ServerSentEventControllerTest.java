package nasirov.yv.controller;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_URL;
import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.front.EventType;
import nasirov.yv.data.front.SseDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Flux;

/**
 * @author Nasirov Yuriy
 */
public class ServerSentEventControllerTest extends AbstractTest {

	private static final String SSE_PATH = "/sse";

	@Test
	public void shouldReturn200() {
		//given
		mockServerSentEventServiceOk();
		List<ServerSentEvent<SseDto>> serverSentEvents = buildServerSentEvents();
		//when
		ResponseSpec responseSpec = call();
		//then
		List<ServerSentEvent<SseDto>> resultServerSentEvents = responseSpec.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectHeader()
				.contentType("text/event-stream;charset=UTF-8")
				.expectBodyList(new ParameterizedTypeReference<ServerSentEvent<SseDto>>() {
				})
				.returnResult()
				.getResponseBody();
		assertNotNull(resultServerSentEvents);
		checkServerSentEvent(serverSentEvents.get(0), resultServerSentEvents.get(0));
		checkServerSentEvent(serverSentEvents.get(1), resultServerSentEvents.get(1));
		checkServerSentEvent(serverSentEvents.get(2), resultServerSentEvents.get(2));
		checkServerSentEvent(serverSentEvents.get(3), resultServerSentEvents.get(3));
	}

	private void checkServerSentEvent(ServerSentEvent<SseDto> expected, ServerSentEvent<SseDto> actual) {
		assertEquals(expected.id(), actual.id());
		assertEquals(expected.data(), actual.data());
		assertEquals(expected.event(), actual.event());
	}

	private void mockServerSentEventServiceOk() {
		List<ServerSentEvent<SseDto>> serverSentEvents = buildServerSentEvents();
		doReturn(Flux.fromIterable(serverSentEvents)).when(serverSentEventService)
				.getServerSentEvents(argThat(x -> x.getUsername()
						.equals("testaccfordev") && x.getFanDubSources()
						.size() == 2 && x.getFanDubSources()
						.containsAll(Sets.newHashSet(FanDubSource.ANIMEDIA, FanDubSource.NINEANIME))));
	}

	private List<ServerSentEvent<SseDto>> buildServerSentEvents() {
		return Lists.newArrayList(buildServerSentEvent(EventType.AVAILABLE, "urlOnAnimedia", "", "0"),
				buildServerSentEvent(EventType.NOT_AVAILABLE, NOT_AVAILABLE_EPISODE_URL, NOT_AVAILABLE_EPISODE_URL, "1"),
				buildServerSentEvent(EventType.NOT_FOUND, TITLE_NOT_FOUND_EPISODE_URL, TITLE_NOT_FOUND_EPISODE_URL, "2"),
				buildFinalServerSentEvent());
	}

	private ServerSentEvent<SseDto> buildServerSentEvent(EventType eventType, String urlOnAnimedia, String urlOnNineAnime, String id) {
		return ServerSentEvent.builder(SseDto.builder()
						.eventType(eventType)
						.anime(Anime.builder()
								.animeName("name")
								.animeUrlOnMal("animeUrlOnMal")
								.malEpisodeNumber("1")
								.posterUrlOnMal("posterUrlOnMal")
								.fanDubUrl(FanDubSource.ANIMEDIA, urlOnAnimedia)
								.fanDubUrl(FanDubSource.NINEANIME, urlOnNineAnime)
								.build())
						.build())
				.id(id)
				.build();
	}

	private ServerSentEvent<SseDto> buildFinalServerSentEvent() {
		return ServerSentEvent.builder(SseDto.builder()
						.eventType(EventType.DONE)
						.build())
				.id("-1")
				.build();
	}


	private ResponseSpec call() {
		return webTestClient.get()
				.uri(SSE_PATH + "?username=" + TEST_ACC_FOR_DEV + "&fanDubSources=ANIMEDIA,NINEANIME")
				.exchange();
	}
}