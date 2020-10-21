package nasirov.yv.service.impl.common;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;
import static nasirov.yv.utils.TestConstants.ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.JUTSU_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NINE_ANIME_TO;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.SOVET_ROMANTICA_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nasirov.yv.data.front.Anime;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.service.AnimeServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.anidub.AnidubEpisodeUrlService;
import nasirov.yv.service.impl.fandub.anilibria.AnilibriaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animepik.AnimepikEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jutsu.JutsuEpisodeUrlService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import nasirov.yv.service.impl.fandub.sovet_romantica.SovetRomanticaEpisodeUrlService;
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
@PrepareForTest(value = {AnimediaEpisodeUrlService.class, NineAnimeEpisodeUrlService.class, AnidubEpisodeUrlService.class,
		JisedaiEpisodeUrlService.class, AnimepikEpisodeUrlService.class})
public class AnimeServiceTest {

	private static final String EPISODE_URL_ON_ANIMEDIA = ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/1";

	private static final String EPISODE_URL_ON_NINE_ANIME = NINE_ANIME_TO + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1";

	private static final String EPISODE_URL_ON_ANIDUB = ANIDUB_URL + REGULAR_TITLE_ANIDUB_URL;

	private static final String EPISODE_URL_ON_JISEDAI = JISEDAI_URL + REGULAR_TITLE_JISEDAI_URL;

	private static final String EPISODE_URL_ON_ANIMEPIK = ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL;

	private static final String EPISODE_URL_ON_ANILIBRIA = ANILIBRIA_URL + REGULAR_TITLE_ANILIBRIA_URL;

	private static final String EPISODE_URL_ON_JUTSU = JUTSU_URL + REGULAR_TITLE_JUTSU_URL + "/episode-1.html";

	private static final String EPISODE_URL_ON_SOVET_ROMANTICA = SOVET_ROMANTICA_URL + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles";

	private AnimediaEpisodeUrlService animediaEpisodeUrlService = PowerMockito.mock(AnimediaEpisodeUrlService.class);

	private NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService = PowerMockito.mock(NineAnimeEpisodeUrlService.class);

	private AnidubEpisodeUrlService anidubEpisodeUrlService = PowerMockito.mock(AnidubEpisodeUrlService.class);

	private JisedaiEpisodeUrlService jisedaiEpisodeUrlService = PowerMockito.mock(JisedaiEpisodeUrlService.class);

	private AnimepikEpisodeUrlService animepikEpisodeUrlService = PowerMockito.mock(AnimepikEpisodeUrlService.class);

	private AnilibriaEpisodeUrlService anilibriaEpisodeUrlService = PowerMockito.mock(AnilibriaEpisodeUrlService.class);

	private JutsuEpisodeUrlService jutsuEpisodeUrlService = PowerMockito.mock(JutsuEpisodeUrlService.class);

	private SovetRomanticaEpisodeUrlService sovetRomanticaEpisodeUrlService = PowerMockito.mock(SovetRomanticaEpisodeUrlService.class);

	private AnimeServiceI animeService;

	@Before
	public void setUp() {
		Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy = new EnumMap<>(FanDubSource.class);
		episodeUrlStrategy.put(ANIMEDIA, animediaEpisodeUrlService);
		episodeUrlStrategy.put(NINEANIME, nineAnimeEpisodeUrlService);
		episodeUrlStrategy.put(ANIDUB, anidubEpisodeUrlService);
		episodeUrlStrategy.put(JISEDAI, jisedaiEpisodeUrlService);
		episodeUrlStrategy.put(ANIMEPIK, animepikEpisodeUrlService);
		episodeUrlStrategy.put(ANILIBRIA, anilibriaEpisodeUrlService);
		episodeUrlStrategy.put(JUTSU, jutsuEpisodeUrlService);
		episodeUrlStrategy.put(SOVETROMANTICA, sovetRomanticaEpisodeUrlService);
		animeService = new AnimeService(episodeUrlStrategy);
	}

	@Test
	public void shouldReturnAllTypesOfPossibleUrls() {
		//given
		mockEpisodeUrlServices();
		Set<MalTitle> watchingTitles = buildWatchingTitles();
		Set<Anime> expectedAnime = buildExpectedAnime();
		Set<FanDubSource> fanDubSources = buildFanDubSources();
		//when
		List<Anime> result = watchingTitles.stream()
				.map(x -> animeService.buildAnime(fanDubSources, x))
				.collect(Collectors.toList());
		//then
		assertEquals(expectedAnime.size(), result.size());
		result.forEach(x -> assertTrue(expectedAnime.contains(x)));
	}

	private void mockEpisodeUrlServices() {
		mockEpisodeUrlServices(buildRegularTitle(),
				EPISODE_URL_ON_ANIMEDIA,
				EPISODE_URL_ON_NINE_ANIME,
				EPISODE_URL_ON_ANIDUB,
				EPISODE_URL_ON_JISEDAI,
				EPISODE_URL_ON_ANIMEPIK,
				EPISODE_URL_ON_ANILIBRIA,
				EPISODE_URL_ON_JUTSU,
				EPISODE_URL_ON_SOVET_ROMANTICA);
		mockEpisodeUrlServices(buildConcretizedTitle(),
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
				FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
		mockEpisodeUrlServices(buildNotFoundOnSiteTitle(),
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL,
				NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	private Set<FanDubSource> buildFanDubSources() {
		return Sets.newHashSet(FanDubSource.values());
	}

	private void mockEpisodeUrlServices(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, String episodeUrlOnAnilibria, String episodeUrlOnJutsu,
			String episodeUrlOnSovetRomantica) {
		doReturn(episodeUrlOnAnimedia).when(animediaEpisodeUrlService)
				.getEpisodeUrl(ANIMEDIA, watchingTitle);
		doReturn(episodeUrlOnNineAnime).when(nineAnimeEpisodeUrlService)
				.getEpisodeUrl(NINEANIME, watchingTitle);
		doReturn(episodeUrlOnAnidub).when(anidubEpisodeUrlService)
				.getEpisodeUrl(ANIDUB, watchingTitle);
		doReturn(episodeUrlOnJisedai).when(jisedaiEpisodeUrlService)
				.getEpisodeUrl(JISEDAI, watchingTitle);
		doReturn(episodeUrlOnAnimepik).when(animepikEpisodeUrlService)
				.getEpisodeUrl(ANIMEPIK, watchingTitle);
		doReturn(episodeUrlOnAnilibria).when(anilibriaEpisodeUrlService)
				.getEpisodeUrl(ANILIBRIA, watchingTitle);
		doReturn(episodeUrlOnJutsu).when(jutsuEpisodeUrlService)
				.getEpisodeUrl(JUTSU, watchingTitle);
		doReturn(episodeUrlOnSovetRomantica).when(sovetRomanticaEpisodeUrlService)
				.getEpisodeUrl(SOVETROMANTICA, watchingTitle);
	}

	private Set<Anime> buildExpectedAnime() {
		return Sets.newHashSet(buildAnime(buildRegularTitle(),
				EPISODE_URL_ON_ANIMEDIA,
				EPISODE_URL_ON_NINE_ANIME,
				EPISODE_URL_ON_ANIDUB,
				EPISODE_URL_ON_JISEDAI,
				EPISODE_URL_ON_ANIMEPIK,
				EPISODE_URL_ON_ANILIBRIA,
				EPISODE_URL_ON_JUTSU,
				EPISODE_URL_ON_SOVET_ROMANTICA),
				buildAnime(buildConcretizedTitle(),
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
						FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE,
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
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL,
						NOT_FOUND_ON_FANDUB_SITE_URL));
	}

	private Anime buildAnime(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, String episodeUrlOnAnilibria, String episodeUrlOnJutsu,
			String episodeUrlOnSovetRomantica) {
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
				.fanDubUrl(ANILIBRIA, episodeUrlOnAnilibria)
				.fanDubUrl(JUTSU, episodeUrlOnJutsu)
				.fanDubUrl(SOVETROMANTICA, episodeUrlOnSovetRomantica)
				.build();
	}

	private Set<MalTitle> buildWatchingTitles() {
		return Sets.newHashSet(buildRegularTitle(), buildNotFoundOnSiteTitle(), buildConcretizedTitle());
	}

	private MalTitle buildRegularTitle() {
		return buildWatchingTitle(REGULAR_TITLE_ORIGINAL_NAME, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL);
	}

	private MalTitle buildNotFoundOnSiteTitle() {
		return buildWatchingTitle(NOT_FOUND_ON_MAL_TITLE_ORIGINAL_NAME, NOT_FOUND_ON_MAL_TITLE_POSTER_URL, NOT_FOUND_ON_MAL_TITLE_MAL_ANIME_URL);
	}

	private MalTitle buildConcretizedTitle() {
		return buildWatchingTitle(CONCRETIZED_TITLE_ORIGINAL_NAME, CONCRETIZED_TITLE_POSTER_URL, CONCRETIZED_TITLE_MAL_ANIME_URL);
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