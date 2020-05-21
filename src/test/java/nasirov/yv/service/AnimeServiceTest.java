package nasirov.yv.service;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.data.constants.FanDubSource.ANIMEDIA;
import static nasirov.yv.data.constants.FanDubSource.NINEANIME;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.MalTitle;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimeServiceTest extends AbstractTest {

	private static final String EPISODE_URL_ON_ANIMEDIA = ANIMEDIA_ONLINE_TV + REGULAR_TITLE_URL + "/1/1";

	private static final String EPISODE_URL_ON_NINE_ANIME = NINE_ANIME_TO + "/" + REGULAR_TITLE_DUB_NINE_ANIME_URL;

	@Test
	public void allTypesOfPossibleUrlsTest() {
		mockEpisodeUrlServices();
		Set<MalTitle> watchingTitles = buildWatchingTitles();
		Set<Anime> expectedAnime = buildExpectedAnime();
		for (MalTitle title : watchingTitles) {
			Anime resultAnime = animeService.buildAnime(buildFanDubSources(), title);
			assertTrue(expectedAnime.contains(resultAnime));
		}
	}

	private void mockEpisodeUrlServices() {
		mockEpisodeUrlServices(EPISODE_URL_ON_ANIMEDIA, EPISODE_URL_ON_NINE_ANIME, buildRegularTitle());
		mockEpisodeUrlServices(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, buildConcretizedTitle());
		mockEpisodeUrlServices(NOT_FOUND_ON_FANDUB_SITE_URL, NOT_FOUND_ON_FANDUB_SITE_URL, buildNotFoundOnSiteTitle());
	}

	private Set<FanDubSource> buildFanDubSources() {
		return Sets.newHashSet(ANIMEDIA, NINEANIME);
	}

	private void mockEpisodeUrlServices(String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, MalTitle watchingTitle) {
		doReturn(episodeUrlOnAnimedia).when(animediaEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
		doReturn(episodeUrlOnNineAnime).when(nineAnimeEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
	}

	private Set<Anime> buildExpectedAnime() {
		return Sets.newHashSet(buildAnime(buildRegularTitle(), EPISODE_URL_ON_ANIMEDIA, EPISODE_URL_ON_NINE_ANIME),
				buildAnime(buildConcretizedTitle(), FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE),
				buildAnime(buildNotFoundOnSiteTitle(), NOT_FOUND_ON_FANDUB_SITE_URL, NOT_FOUND_ON_FANDUB_SITE_URL));
	}

	private Anime buildAnime(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime) {
		return Anime.builder()
				.animeName(watchingTitle.getName())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMAL(watchingTitle.getPosterUrl())
				.animeUrlOnMAL(watchingTitle.getAnimeUrl())
				.fanDubUrl(ANIMEDIA, episodeUrlOnAnimedia)
				.fanDubUrl(NINEANIME, episodeUrlOnNineAnime)
				.build();
	}

	private Set<MalTitle> buildWatchingTitles() {
		return Sets.newHashSet(buildRegularTitle(), buildNotFoundOnSiteTitle(), buildConcretizedTitle());
	}

	private MalTitle buildRegularTitle() {
		return buildWatchingTitle(REGULAR_TITLE_NAME, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL);
	}

	private MalTitle buildNotFoundOnSiteTitle() {
		return buildWatchingTitle(NOT_FOUND_ON_ANIMEDIA_TITLE_NAME, NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL, NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL);
	}

	private MalTitle buildConcretizedTitle() {
		return buildWatchingTitle(CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME, CONCRETIZED_TITLE_POSTER_URL, CONCRETIZED_TITLE_MAL_ANIME_URL);
	}

	private MalTitle buildWatchingTitle(String titleName, String posterUrl, String animeUrl) {
		return MalTitle.builder()
				.id(1)
				.numWatchedEpisodes(0)
				.name(titleName)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl)
				.animeUrl(MY_ANIME_LIST_URL + animeUrl)
				.build();
	}
}