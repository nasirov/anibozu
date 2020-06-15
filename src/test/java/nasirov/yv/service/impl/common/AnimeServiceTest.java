package nasirov.yv.service.impl.common;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.data.constants.FanDubSource.ANIDUB;
import static nasirov.yv.data.constants.FanDubSource.ANIMEDIA;
import static nasirov.yv.data.constants.FanDubSource.ANIMEPIK;
import static nasirov.yv.data.constants.FanDubSource.JISEDAI;
import static nasirov.yv.data.constants.FanDubSource.NINEANIME;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;
import static nasirov.yv.utils.TestConstants.ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_WITH_SINGLE_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_ANIMEDIA_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_DUB_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.anidub.site.AnidubSiteEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animepik.AnimepikEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {AnimediaEpisodeUrlService.class, NineAnimeEpisodeUrlService.class, AnidubSiteEpisodeUrlService.class,
		JisedaiEpisodeUrlService.class, AnimepikEpisodeUrlService.class})
public class AnimeServiceTest {

	private static final String EPISODE_URL_ON_ANIMEDIA = ANIMEDIA_ONLINE_TV + REGULAR_TITLE_URL + "/1/1";

	private static final String EPISODE_URL_ON_NINE_ANIME = NINE_ANIME_TO + REGULAR_TITLE_DUB_NINE_ANIME_URL;

	private static final String EPISODE_URL_ON_ANIDUB = ANIDUB_URL + REGULAR_TITLE_ANIDUB_SITE_URL;

	private static final String EPISODE_URL_ON_JISEDAI = JISEDAI_URL + REGULAR_TITLE_JISEDAI_SITE_URL;

	private static final String EPISODE_URL_ON_ANIMEPIK = ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_SITE_URL;

	private AnimediaEpisodeUrlService animediaEpisodeUrlService = PowerMockito.mock(AnimediaEpisodeUrlService.class);

	private NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService = PowerMockito.mock(NineAnimeEpisodeUrlService.class);

	private AnidubSiteEpisodeUrlService anidubEpisodeUrlService = PowerMockito.mock(AnidubSiteEpisodeUrlService.class);

	private JisedaiEpisodeUrlService jisedaiEpisodeUrlService = PowerMockito.mock(JisedaiEpisodeUrlService.class);

	private AnimepikEpisodeUrlService animepikEpisodeUrlService = PowerMockito.mock(AnimepikEpisodeUrlService.class);

	private AnimeServiceI animeService;

	@Before
	public void setUp() {
		Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy = new EnumMap<>(FanDubSource.class);
		episodeUrlStrategy.put(ANIMEDIA, animediaEpisodeUrlService);
		episodeUrlStrategy.put(NINEANIME, nineAnimeEpisodeUrlService);
		episodeUrlStrategy.put(ANIDUB, anidubEpisodeUrlService);
		episodeUrlStrategy.put(JISEDAI, jisedaiEpisodeUrlService);
		episodeUrlStrategy.put(ANIMEPIK, animepikEpisodeUrlService);
		animeService = new AnimeService(episodeUrlStrategy);
	}

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
		mockEpisodeUrlServices(EPISODE_URL_ON_ANIMEDIA,
				EPISODE_URL_ON_NINE_ANIME,
				EPISODE_URL_ON_ANIDUB,
				EPISODE_URL_ON_JISEDAI,
				EPISODE_URL_ON_ANIMEPIK,
				buildRegularTitle());
		mockEpisodeUrlServices(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				buildConcretizedTitle());
		mockEpisodeUrlServices(NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				buildNotFoundOnSiteTitle());
	}

	private Set<FanDubSource> buildFanDubSources() {
		return Sets.newHashSet(FanDubSource.values());
	}

	private void mockEpisodeUrlServices(String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, MalTitle watchingTitle) {
		doReturn(episodeUrlOnAnimedia).when(animediaEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
		doReturn(episodeUrlOnNineAnime).when(nineAnimeEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
		doReturn(episodeUrlOnAnidub).when(anidubEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
		doReturn(episodeUrlOnJisedai).when(jisedaiEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
		doReturn(episodeUrlOnAnimepik).when(animepikEpisodeUrlService)
				.getEpisodeUrl(watchingTitle);
	}

	private Set<Anime> buildExpectedAnime() {
		return Sets.newHashSet(buildAnime(buildRegularTitle(),
				EPISODE_URL_ON_ANIMEDIA,
				EPISODE_URL_ON_NINE_ANIME,
				EPISODE_URL_ON_ANIDUB,
				EPISODE_URL_ON_JISEDAI,
				EPISODE_URL_ON_ANIMEPIK),
				buildAnime(buildConcretizedTitle(),
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE),
				buildAnime(buildNotFoundOnSiteTitle(),
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL));
	}

	private Anime buildAnime(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik) {
		return Anime.builder()
				.animeName(watchingTitle.getName())
				.episode(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMAL(watchingTitle.getPosterUrl())
				.animeUrlOnMAL(watchingTitle.getAnimeUrl())
				.fanDubUrl(ANIMEDIA, episodeUrlOnAnimedia)
				.fanDubUrl(NINEANIME, episodeUrlOnNineAnime)
				.fanDubUrl(ANIDUB, episodeUrlOnAnidub)
				.fanDubUrl(JISEDAI, episodeUrlOnJisedai)
				.fanDubUrl(ANIMEPIK, episodeUrlOnAnimepik)
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