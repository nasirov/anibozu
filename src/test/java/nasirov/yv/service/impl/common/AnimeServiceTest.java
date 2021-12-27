package nasirov.yv.service.impl.common;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME;
import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_URL;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANYTHING_GROUP;
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
import static nasirov.yv.utils.CommonTitleTestBuilder.ANYTHING_GROUP_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JAM_CLUB_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JISEDAI_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.JUTSU_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.NINE_ANIME_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.SHIZA_PROJECT_EPISODE_NAME;
import static nasirov.yv.utils.CommonTitleTestBuilder.SOVET_ROMANTICA_EPISODE_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_STATIC_CONTENT_URL;
import static nasirov.yv.utils.TestConstants.MY_ANIME_LIST_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_ID;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_FANDUB_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ORIGINAL_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_POSTER_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.Anime;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public class AnimeServiceTest extends AbstractTest {

	@Test
	public void shouldBuildAnimeWithAvailableUrls() {
		//given
		MalTitle regularTitle = buildRegularTitle();
		Set<FanDubSource> fanDubSources = buildOrderedFanDubSources();
		mockGetCommonTitles(regularTitle, buildRegularCommonTitles(fanDubSources), fanDubSources);
		Anime expectedAnime = buildExpectedAnimeWithAvailableUrls();
		//when
		Anime result = animeService.buildAnime(fanDubSources, regularTitle)
				.block();
		//then
		assertEquals(expectedAnime, result);
	}

	@Test
	public void shouldBuildAnimeWithNotAvailableUrls() {
		//given
		MalTitle concretizedTitle = buildConcretizedTitle();
		Set<FanDubSource> fanDubSources = buildOrderedFanDubSources();
		mockGetCommonTitles(concretizedTitle, buildConcretizedCommonTitles(fanDubSources), fanDubSources);
		Anime expectedAnime = buildExpectedAnimeWithNotAvailableUrls();
		//when
		Anime result = animeService.buildAnime(fanDubSources, concretizedTitle)
				.block();
		//then
		assertEquals(expectedAnime, result);
	}

	@Test
	public void shouldBuildAnimeWithNotFoundOnFandubUrls() {
		//given
		MalTitle notFoundOnFandubTitle = buildNotFoundOnFandubTitle();
		Set<FanDubSource> fanDubSources = buildOrderedFanDubSources();
		mockGetCommonTitles(notFoundOnFandubTitle, buildNotFoundOnFandubCommonTitles(fanDubSources), fanDubSources);
		Anime expectedAnime = buildExpectedAnimeWithNotOnFandubUrls();
		//when
		Anime result = animeService.buildAnime(fanDubSources, notFoundOnFandubTitle)
				.block();
		//then
		assertEquals(expectedAnime, result);
	}

	private Map<FanDubSource, List<CommonTitle>> buildRegularCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Lists.newArrayList(CommonTitleTestBuilder.buildRegularTitle(x))));
	}

	private Map<FanDubSource, List<CommonTitle>> buildConcretizedCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Lists.newArrayList(CommonTitleTestBuilder.buildConcretizedTitle(x))));
	}

	private Map<FanDubSource, List<CommonTitle>> buildNotFoundOnFandubCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Collections.emptyList()));
	}

	private void mockGetCommonTitles(MalTitle malTitle, Map<FanDubSource, List<CommonTitle>> commonTitles, Set<FanDubSource> fanDubSources) {
		doReturn(Mono.just(commonTitles)).when(httpRequestService)
				.performHttpRequest(argThat(x -> x.getUrl()
						.equals(externalServicesProps.getFandubTitlesServiceUrl() + "titles?fanDubSources=" + fanDubSources.stream()
								.map(FanDubSource::name)
								.collect(Collectors.joining(",")) + "&malId=" + malTitle.getId() + "&malEpisodeId=" + (malTitle.getNumWatchedEpisodes() + 1))));
	}

	private Set<FanDubSource> buildOrderedFanDubSources() {
		Set<FanDubSource> result = new LinkedHashSet<>();
		result.add(ANIMEDIA);
		result.add(NINEANIME);
		result.add(ANIDUB);
		result.add(JISEDAI);
		result.add(ANIMEPIK);
		result.add(ANILIBRIA);
		result.add(JUTSU);
		result.add(SOVETROMANTICA);
		result.add(SHIZAPROJECT);
		result.add(JAMCLUB);
		result.add(ANYTHING_GROUP);
		return result;
	}

	private Anime buildExpectedAnimeWithAvailableUrls() {
		Map<FanDubSource, String> fandubUrls = fanDubProps.getUrls();
		return buildAnime(buildRegularTitle(),
				fandubUrls.get(ANIMEDIA) + REGULAR_TITLE_ANIMEDIA_URL + "/1/1",
				fandubUrls.get(NINEANIME) + REGULAR_TITLE_NINE_ANIME_URL + "/ep-1",
				fandubUrls.get(ANIDUB) + REGULAR_TITLE_ANIDUB_URL,
				fandubUrls.get(JISEDAI) + REGULAR_TITLE_JISEDAI_URL,
				fandubUrls.get(ANIMEPIK) + REGULAR_TITLE_ANIMEPIK_URL,
				fandubUrls.get(ANILIBRIA) + REGULAR_TITLE_ANILIBRIA_URL,
				fandubUrls.get(JUTSU) + REGULAR_TITLE_JUTSU_URL + "/episode-1.html",
				fandubUrls.get(SOVETROMANTICA) + REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles",
				fandubUrls.get(SHIZAPROJECT) + REGULAR_TITLE_SHIZA_PROJECT_URL,
				fandubUrls.get(JAMCLUB) + REGULAR_TITLE_JAM_CLUB_URL,
				fandubUrls.get(ANYTHING_GROUP) + REGULAR_TITLE_ANYTHING_GROUP_URL,
				null);
	}

	private Anime buildExpectedAnimeWithNotAvailableUrls() {
		return buildAnime(buildConcretizedTitle(),
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_URL,
				NOT_AVAILABLE_EPISODE_NAME);
	}

	private Anime buildExpectedAnimeWithNotOnFandubUrls() {
		return buildAnime(buildNotFoundOnFandubTitle(),
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_URL,
				TITLE_NOT_FOUND_EPISODE_NAME);
	}

	private Anime buildAnime(MalTitle watchingTitle, String episodeUrlOnAnimedia, String episodeUrlOnNineAnime, String episodeUrlOnAnidub,
			String episodeUrlOnJisedai, String episodeUrlOnAnimepik, String episodeUrlOnAnilibria, String episodeUrlOnJutsu,
			String episodeUrlOnSovetRomantica, String episodeUrlOnShizaProject, String episodeUrlOnJamClub, String episodeUrlOnAnythingGroup,
			String episodeNameForAll) {
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
				.fanDubUrl(ANYTHING_GROUP, episodeUrlOnAnythingGroup)
				.fanDubEpisodeName(ANIMEDIA, episodeNameForAll == null ? ANIMEDIA_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(NINEANIME, episodeNameForAll == null ? NINE_ANIME_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(ANIDUB, episodeNameForAll == null ? ANIDUB_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(JISEDAI, episodeNameForAll == null ? JISEDAI_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(ANIMEPIK, episodeNameForAll == null ? ANIMEPIK_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(ANILIBRIA, episodeNameForAll == null ? ANILIBRIA_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(JUTSU, episodeNameForAll == null ? JUTSU_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(SOVETROMANTICA, episodeNameForAll == null ? SOVET_ROMANTICA_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(SHIZAPROJECT, episodeNameForAll == null ? SHIZA_PROJECT_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(JAMCLUB, episodeNameForAll == null ? JAM_CLUB_EPISODE_NAME : episodeNameForAll)
				.fanDubEpisodeName(ANYTHING_GROUP, episodeNameForAll == null ? ANYTHING_GROUP_EPISODE_NAME : episodeNameForAll)
				.build();
	}

	private MalTitle buildRegularTitle() {
		return buildWatchingTitle(REGULAR_TITLE_ORIGINAL_NAME, REGULAR_TITLE_POSTER_URL, REGULAR_TITLE_MAL_ANIME_URL, REGULAR_TITLE_MAL_ID, 0);
	}

	private MalTitle buildNotFoundOnFandubTitle() {
		return buildWatchingTitle(NOT_FOUND_ON_FANDUB_TITLE_ORIGINAL_NAME,
				NOT_FOUND_ON_FANDUB_TITLE_POSTER_URL,
				NOT_FOUND_ON_FANDUB_TITLE_MAL_ANIME_URL,
				NOT_FOUND_ON_FANDUB_TITLE_ID,
				0);
	}

	private MalTitle buildConcretizedTitle() {
		return buildWatchingTitle(CONCRETIZED_TITLE_ORIGINAL_NAME,
				CONCRETIZED_TITLE_POSTER_URL,
				CONCRETIZED_TITLE_MAL_ANIME_URL,
				CONCRETIZED_TITLE_MAL_ID,
				10);
	}

	private MalTitle buildWatchingTitle(String titleName, String posterUrl, String animeUrl, int id, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(id)
				.numWatchedEpisodes(numWatchedEpisodes)
				.name(titleName)
				.posterUrl(MY_ANIME_LIST_STATIC_CONTENT_URL + posterUrl)
				.animeUrl(MY_ANIME_LIST_URL + animeUrl)
				.build();
	}
}