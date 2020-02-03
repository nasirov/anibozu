package nasirov.yv.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;
import static nasirov.yv.data.constants.FunDubSource.NINEANIME;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_AVAILABLE_EPISODE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_DATA_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterables;
import feign.template.UriUtils;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.UserMALTitleInfo;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Sets;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class NineAnimeServiceTest extends AbstractTest {

	@Test
	public void dubAvailableNewEpisodeAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL + REGULAR_TITLE_DUB_AVAILABLE_EPISODE_ID, "1");
	}

	@Test
	public void dubAvailableNewEpisodeAvailableAndMixedWithWords() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndMixedWithWords.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL + REGULAR_TITLE_DUB_AVAILABLE_EPISODE_ID, "1");
	}

	@Test
	public void dubAvailableNewEpisodeAvailableAndStubConstant() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndStubConstant.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL + REGULAR_TITLE_DUB_AVAILABLE_EPISODE_ID, "1");
	}

	@Test
	public void dubAvailableNewEpisodeNotAvailableAndUnknownStubConstant() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailableAndUnknownStubConstant.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, "1");
	}

	@Test
	public void dubAvailableNewEpisodeNotAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubAvailable.json",
				"nine_anime/episodes_search/newEpisodeNotAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, "1");
	}

	@Test
	public void dubNotAvailableNewEpisodeAvailable() {
		mockNineAnime("nine_anime/title_search/regularTitleDubNotAvailable.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL + REGULAR_TITLE_DUB_AVAILABLE_EPISODE_ID, "1");
	}

	@Test
	public void titleNotFound() {
		mockNineAnime("nine_anime/title_search/regularTitleNotFound.json",
				"nine_anime/episodes_search/newEpisodeAvailable.json",
				REGULAR_TITLE_DUB_NINE_ANIME_DATA_ID);
		Set<Anime> matchedAnime = nineAnimeService.getMatchedAnime(Sets.newLinkedHashSet(buildWatchingTitle()));
		checkMatchedSize(matchedAnime);
		Anime anime = Iterables.get(matchedAnime, 0);
		checkMatchedAnime(anime, NOT_FOUND_ON_FUNDUB_SITE_URL, null);
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

	private UserMALTitleInfo buildWatchingTitle() {
		return new UserMALTitleInfo(1, 0, buildTitleNameWithSemiColin(), MY_ANIME_LIST_STATIC_CONTENT_URL + REGULAR_TITLE_POSTER_URL, "animeUrl");
	}

	private void checkMatchedAnime(Anime anime, String s, String expectedEpisode) {
		assertEquals(NINEANIME, anime.getFunDubSource());
		assertEquals(buildTitleNameWithSemiColin(), anime.getTitleName());
		assertEquals(s, anime.getLink());
		assertEquals(expectedEpisode, anime.getEpisode());
	}

	private void checkMatchedSize(Set<Anime> matchedAnime) {
		assertEquals(1, matchedAnime.size());
	}
}