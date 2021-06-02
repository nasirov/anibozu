package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.CommonTitleTestBuilder.JISEDAI_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
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
public class JisedaiEpisodeNameAndUrlServiceTest extends AbstractEpisodeNameAndUrlsServiceTest {

	private static final String RUNTIME_EPISODE_NAME = "2 эпизод";

	@Mock
	private JisedaiParserI jisedaiParser;

	@InjectMocks
	private JisedaiEpisodeNameAndUrlService jisedaiEpisodeUrlService;

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisode() {
		super.shouldReturnNameAndUrlForAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime() {
		super.shouldReturnNameAndUrlForAvailableEpisodeBuiltInRuntime();
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
		super.shouldReturnNameAndUrlForNotAvailableEpisodeBuiltInRuntime();
	}

	@Override
	protected String getFandubUrl() {
		return JISEDAI_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return jisedaiParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent, CommonTitle commonTitle) {
		HttpRequestServiceDto<String> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.jisedai(commonTitle);
		doReturn(Mono.just(titlePageContent)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	@Override
	protected EpisodeNameAndUrlServiceI getEpisodeNameAndUrlService() {
		return jisedaiEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JISEDAI;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name(JISEDAI_EPISODE_NAME)
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build(),
				FandubEpisode.builder()
						.name(RUNTIME_EPISODE_NAME)
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build());
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisode(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(JISEDAI_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JISEDAI_URL), episodeNameAndUrl);
	}

	@Override
	protected void checkNameAndUrlForAvailableEpisodeBuiltInRuntime(Pair<String, String> episodeNameAndUrl) {
		assertEquals(Pair.of(RUNTIME_EPISODE_NAME, getFandubUrl() + REGULAR_TITLE_JISEDAI_URL), episodeNameAndUrl);
	}
}