package nasirov.yv.service.impl.common;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_URL;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JAMCLUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SHIZAPROJECT;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIDUB_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANILIBRIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEDIA_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.ANIMEPIK_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JAM_CLUB_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JISEDAI_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JUTSU_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.NINE_ANIME_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.SHIZA_PROJECT_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.SOVET_ROMANTICA_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.ANIMEDIA_ONLINE_TV;
import static nasirov.yv.utils.TestConstants.ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.JAM_CLUB_URL;
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
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.SOVET_ROMANTICA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.impl.fandub.AnidubEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnilibriaEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnimediaEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnimepikEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JamClubEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JisedaiEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JutsuEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.NineAnimeEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.ShizaProjectEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.SovetRomanticaEpisodeNameAndUrlService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@ExtendWith(MockitoExtension.class)
public class AnimeServiceTest {

	private static final String EPISODE_URL_ON_ANIMEDIA = ANIMEDIA_ONLINE_TV + REGULAR_TITLE_ANIMEDIA_URL + "/1/1";

	private static final String EPISODE_URL_ON_NINE_ANIME = NINE_ANIME_TO + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1";

	private static final String EPISODE_URL_ON_ANIDUB = ANIDUB_URL + REGULAR_TITLE_ANIDUB_URL;

	private static final String EPISODE_URL_ON_JISEDAI = JISEDAI_URL + REGULAR_TITLE_JISEDAI_URL;

	private static final String EPISODE_URL_ON_ANIMEPIK = ANIMEPIK_URL + REGULAR_TITLE_ANIMEPIK_URL;

	private static final String EPISODE_URL_ON_ANILIBRIA = ANILIBRIA_URL + REGULAR_TITLE_ANILIBRIA_URL;

	private static final String EPISODE_URL_ON_JUTSU = JUTSU_URL + REGULAR_TITLE_JUTSU_URL + "/episode-1.html";

	private static final String EPISODE_URL_ON_SOVET_ROMANTICA = SOVET_ROMANTICA_URL + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles";

	private static final String EPISODE_URL_ON_SHIZA_PROJECT = SHIZA_PROJECT_URL + REGULAR_TITLE_SHIZA_PROJECT_URL;

	private static final String EPISODE_URL_ON_JAM_CLUB = JAM_CLUB_URL + REGULAR_TITLE_JAM_CLUB_URL;

	@Mock
	private AnimediaEpisodeNameAndUrlService animediaEpisodeUrlService;

	@Mock
	private NineAnimeEpisodeNameAndUrlService nineAnimeEpisodeUrlService;

	@Mock
	private AnidubEpisodeNameAndUrlService anidubEpisodeUrlService;

	@Mock
	private JisedaiEpisodeNameAndUrlService jisedaiEpisodeUrlService;

	@Mock
	private AnimepikEpisodeNameAndUrlService animepikEpisodeUrlService;

	@Mock
	private AnilibriaEpisodeNameAndUrlService anilibriaEpisodeUrlService;

	@Mock
	private JutsuEpisodeNameAndUrlService jutsuEpisodeUrlService;

	@Mock
	private SovetRomanticaEpisodeNameAndUrlService sovetRomanticaEpisodeUrlService;

	@Mock
	private ShizaProjectEpisodeNameAndUrlService shizaProjectEpisodeUrlService;

	@Mock
	private JamClubEpisodeNameAndUrlService jamClubEpisodeNameAndUrlService;

	private AnimeServiceI animeService;

	@BeforeEach
	public void setUp() {
		Map<FanDubSource, EpisodeNameAndUrlServiceI> episodeUrlStrategy = new EnumMap<>(FanDubSource.class);
		episodeUrlStrategy.put(ANIMEDIA, animediaEpisodeUrlService);
		episodeUrlStrategy.put(NINEANIME, nineAnimeEpisodeUrlService);
		episodeUrlStrategy.put(ANIDUB, anidubEpisodeUrlService);
		episodeUrlStrategy.put(JISEDAI, jisedaiEpisodeUrlService);
		episodeUrlStrategy.put(ANIMEPIK, animepikEpisodeUrlService);
		episodeUrlStrategy.put(ANILIBRIA, anilibriaEpisodeUrlService);
		episodeUrlStrategy.put(JUTSU, jutsuEpisodeUrlService);
		episodeUrlStrategy.put(SOVETROMANTICA, sovetRomanticaEpisodeUrlService);
		episodeUrlStrategy.put(SHIZAPROJECT, shizaProjectEpisodeUrlService);
		episodeUrlStrategy.put(JAMCLUB, jamClubEpisodeNameAndUrlService);
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
				.map(x -> animeService.buildAnime(fanDubSources, x)
						.block())
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
				EPISODE_URL_ON_SOVET_ROMANTICA,
				EPISODE_URL_ON_SHIZA_PROJECT,
				EPISODE_URL_ON_JAM_CLUB);
		mockEpisodeUrlServices(buildConcretizedTitle(),
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL);
		mockEpisodeUrlServices(buildNotFoundOnSiteTitle(),
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL);
	}

	private Set<FanDubSource> buildFanDubSources() {
		return Sets.newHashSet(FanDubSource.values());
	}

	private void mockEpisodeUrlServices(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, String episodeUrlOnAnilibria, String episodeUrlOnJutsu,
			String episodeUrlOnSovetRomantica, String episodeUrlOnShizaProject, String episodeUrlOnJamClub) {
		doReturn(Mono.just(Pair.of(ANIMEDIA_EPISODE_NAME, episodeUrlOnAnimedia))).when(animediaEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(NINE_ANIME_EPISODE_NAME, episodeUrlOnNineAnime))).when(nineAnimeEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(ANIDUB_EPISODE_NAME, episodeUrlOnAnidub))).when(anidubEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(JISEDAI_EPISODE_NAME, episodeUrlOnJisedai))).when(jisedaiEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(ANIMEPIK_EPISODE_NAME, episodeUrlOnAnimepik))).when(animepikEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(ANILIBRIA_EPISODE_NAME, episodeUrlOnAnilibria))).when(anilibriaEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(JUTSU_EPISODE_NAME, episodeUrlOnJutsu))).when(jutsuEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(SOVET_ROMANTICA_EPISODE_NAME, episodeUrlOnSovetRomantica))).when(sovetRomanticaEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(SHIZA_PROJECT_EPISODE_NAME, episodeUrlOnShizaProject))).when(shizaProjectEpisodeUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
		doReturn(Mono.just(Pair.of(JAM_CLUB_EPISODE_NAME, episodeUrlOnJamClub))).when(jamClubEpisodeNameAndUrlService)
				.getEpisodeNameAndUrl(watchingTitle);
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
				EPISODE_URL_ON_SOVET_ROMANTICA,
				EPISODE_URL_ON_SHIZA_PROJECT,
				EPISODE_URL_ON_JAM_CLUB),
				buildAnime(buildConcretizedTitle(),
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL,
						NOT_AVAILABLE_EPISODE_URL),
				buildAnime(buildNotFoundOnSiteTitle(),
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL,
						TITLE_NOT_FOUND_EPISODE_URL));
	}

	private Anime buildAnime(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, String episodeUrlOnAnilibria, String episodeUrlOnJutsu,
			String episodeUrlOnSovetRomantica, String episodeUrlOnShizaProject, String episodeUrlOnJamClub) {
		return Anime.builder()
				.animeName(watchingTitle.getName())
				.malEpisodeNumber(getNextEpisodeForWatch(watchingTitle).toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl())
				.fanDubUrl(ANIMEDIA, episodeUrlOnAnimedia)
				.fanDubUrl(NINEANIME, episodeUrlOnNineAnime)
				.fanDubUrl(ANIDUB, episodeUrlOnAnidub)
				.fanDubUrl(JISEDAI, episodeUrlOnJisedai)
				.fanDubUrl(ANIMEPIK, episodeUrlOnAnimepik)
				.fanDubUrl(ANILIBRIA, episodeUrlOnAnilibria)
				.fanDubUrl(JUTSU, episodeUrlOnJutsu)
				.fanDubUrl(SOVETROMANTICA, episodeUrlOnSovetRomantica)
				.fanDubUrl(SHIZAPROJECT, episodeUrlOnShizaProject)
				.fanDubUrl(JAMCLUB, episodeUrlOnJamClub)
				.fanDubEpisodeName(ANIMEDIA, ANIMEDIA_EPISODE_NAME)
				.fanDubEpisodeName(NINEANIME, NINE_ANIME_EPISODE_NAME)
				.fanDubEpisodeName(ANIDUB, ANIDUB_EPISODE_NAME)
				.fanDubEpisodeName(JISEDAI, JISEDAI_EPISODE_NAME)
				.fanDubEpisodeName(ANIMEPIK, ANIMEPIK_EPISODE_NAME)
				.fanDubEpisodeName(ANILIBRIA, ANILIBRIA_EPISODE_NAME)
				.fanDubEpisodeName(JUTSU, JUTSU_EPISODE_NAME)
				.fanDubEpisodeName(SOVETROMANTICA, SOVET_ROMANTICA_EPISODE_NAME)
				.fanDubEpisodeName(SHIZAPROJECT, SHIZA_PROJECT_EPISODE_NAME)
				.fanDubEpisodeName(JAMCLUB, JAM_CLUB_EPISODE_NAME)
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