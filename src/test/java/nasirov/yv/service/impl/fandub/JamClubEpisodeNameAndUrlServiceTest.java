package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.utils.CommonTitleTestBuilder.JAM_CLUB_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveJamClubServiceI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@ExtendWith(MockitoExtension.class)
public class JamClubEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest<String> {

	private static final String RUNTIME_EPISODE_NAME = "2 серия";

	@Mock
	private ReactiveJamClubServiceI reactiveJamClubService;

	@InjectMocks
	private JamClubEpisodeNameAndUrlService jamClubEpisodeNameAndUrlService;

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisode() {
		super.shouldReturnNameAndUrlForAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(getRegularCommonTitles(), REGULAR_TITLE_MAL_ID, 2);
		mockReactiveJamClubService();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 1);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		checkNameAndUrlForAvailableEpisodeBuiltInRuntime(episodeNameAndUrl);
	}

	@Test
	@Override
	public void shouldReturnNotFoundOnFandubSiteNameAndUrl() {
		super.shouldReturnNotFoundOnFandubSiteNameAndUrl();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForNotAvailableEpisode() {
		super.shouldReturnNameAndUrlForNotAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime() {
		//given
		mockCommonProps();
		mockFandubUrlsMap();
		mockFandubTitleService(getRegularAndConcretizedCommonTitles(), REGULAR_TITLE_MAL_ID, 3);
		mockReactiveJamClubService();
		MalTitle malTitle = buildWatchingTitle(REGULAR_TITLE_MAL_ID, 2);
		//when
		Pair<String, String> episodeNameAndUrl = getEpisodeNameAndUrlService().getEpisodeNameAndUrl(malTitle)
				.block();
		//then
		assertEquals(NOT_AVAILABLE_EPISODE_NAME_AND_URL, episodeNameAndUrl);
	}

	@Override
	protected String getRuntimeExpectedResponse() {
		return "foobar";
	}

	@Override
	protected String getFandubUrl() {
		return JAM_CLUB_URL;
	}

	@Override
	protected void mockGetRuntimeResponse(String runtimeExpectedResponse, CommonTitle commonTitle) {
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return jamClubEpisodeNameAndUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JAMCLUB;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(JAM_CLUB_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JAM_CLUB_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JAM_CLUB_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JAM_CLUB_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JAM_CLUB_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JAM_CLUB_URL), episodeNameAndUrl);
	}

	@Override
	protected void mockParser(String runtimeExpectedResponse) {
	}

	private void mockReactiveJamClubService() {
		doReturn(Mono.just(getFandubEpisodes())).when(reactiveJamClubService)
				.getTitleEpisodes(regularCommonTitle.getUrl(), regularCommonTitle.getId());
	}
}