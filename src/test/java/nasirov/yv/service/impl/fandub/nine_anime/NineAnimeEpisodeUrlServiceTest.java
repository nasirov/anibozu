package nasirov.yv.service.impl.fandub.nine_anime;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_DATA_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static org.junit.Assert.assertEquals;

import feign.template.UriUtils;
import java.nio.charset.StandardCharsets;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.mal.MalTitle;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class NineAnimeEpisodeUrlServiceTest extends AbstractTest {

	@Test
	public void dubAvailableNewEpisodeAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL);
	}

	@Test
	public void dubAvailableNewEpisodeAvailableAndMixedWithWords() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndMixedWithWords.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL);
	}

	@Test
	public void dubAvailableNewEpisodeAvailableAndStubConstant() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndStubConstant.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL);
	}

	@Test
	public void dubAvailableNewEpisodeNotAvailableAndUnknownStubConstant() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndUnknownStubConstant.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Test
	public void dubAvailableNewEpisodeNotAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeNotAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Test
	public void dubNotAvailableNewEpisodeAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubNotAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_NINE_ANIME_DATA_ID);
		performAndCheck(NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL);
	}

	@Test
	public void titleNotFound() {
		mockNineAnime("nine_anime/title_search/regularTitleNotFound.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		performAndCheck(NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	private void performAndCheck(String expectedUrl) {
		String actualUrl = nineAnimeEpisodeUrlService.getEpisodeUrl(buildWatchingTitle());
		assertEquals(expectedUrl, actualUrl);
	}

	private void mockNineAnime(String titleSearchBodyFile, String episodesSearchBodyFile, String dataId) {
		createStubWithBodyFile("/ajax/film/search?keyword=" + UriUtils.encode(buildTitleNameWithSemiColin(), StandardCharsets.UTF_8),
				APPLICATION_JSON,
				titleSearchBodyFile);
		createStubWithBodyFile("/ajax/film/servers/" + dataId, APPLICATION_JSON, episodesSearchBodyFile);
	}

	private String buildTitleNameWithSemiColin() {
		return StringUtils.join(REGULAR_TITLE_NAME.split(" "), ";");
	}

	private MalTitle buildWatchingTitle() {
		return MalTitle.builder()
				.id(1)
				.numWatchedEpisodes(0)
				.name(buildTitleNameWithSemiColin())
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL)
				.animeUrl(MY_ANIME_LIST_URL + REGULAR_TITLE_MAL_ANIME_URL)
				.build();
	}
}