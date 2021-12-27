package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_NINE_ANIME_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_ID;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_NINE_ANIME_ID;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.NOT_FOUND_ON_MAL_TITLE_SOVET_ROMANTICA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEDIA_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANYTHING_GROUP_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JAM_CLUB_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JUTSU_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NINE_ANIME_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.TitleType;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class CommonTitleTestBuilder {

	public static final String ANIMEDIA_EPISODE_NAME = "Серия 1";

	public static final String NINE_ANIME_EPISODE_NAME = "1";

	public static final String ANIDUB_EPISODE_NAME = "1 серия";

	public static final String JISEDAI_EPISODE_NAME = "Серия 1";

	public static final Integer ANIMEPIK_EPISODE_NUMBER = 1;

	public static final String ANIMEPIK_EPISODE_POSTFIX = " серия";

	public static final String ANIMEPIK_EPISODE_NAME = ANIMEPIK_EPISODE_NUMBER + ANIMEPIK_EPISODE_POSTFIX;

	public static final String ANILIBRIA_EPISODE_NAME = "Серия 1";

	public static final String JUTSU_EPISODE_NAME = "1 серия";

	public static final String SOVET_ROMANTICA_EPISODE_NAME = "Эпизод 1";

	public static final String SHIZA_PROJECT_EPISODE_NAME = "1";

	public static final String JAM_CLUB_EPISODE_NAME = "1 серия";

	public static final String ANYTHING_GROUP_EPISODE_NAME = "01. Серия | Foo";

	private static final Map<FanDubSource, Supplier<CommonTitle>> REGULAR_TITLES_METHODS = buildRtm();

	private static final Map<FanDubSource, Supplier<CommonTitle>> CONCRETIZED_TITLES_METHODS = buildCtm();

	private static final Map<FanDubSource, Supplier<CommonTitle>> NOT_FOUND_TITLES_METHODS = buildNftm();

	public static CommonTitle buildRegularTitle(FanDubSource fanDubSource) {
		return REGULAR_TITLES_METHODS.get(fanDubSource)
				.get();
	}

	public static CommonTitle buildConcretizedTitle(FanDubSource fanDubSource) {
		return CONCRETIZED_TITLES_METHODS.get(fanDubSource)
				.get();
	}

	public static CommonTitle buildNotFoundOnMalTitle(FanDubSource fanDubSource) {
		return NOT_FOUND_TITLES_METHODS.get(fanDubSource)
				.get();
	}

	public static CommonTitle getAnimediaRegular() {
		return getRegular(REGULAR_TITLE_ANIMEDIA_URL, 1, buildEpisodeUrl(REGULAR_TITLE_ANIMEDIA_URL, 1), null, ANIMEDIA_EPISODE_NAME);
	}

	public static CommonTitle getAnimediaConcretized() {
		return getConcretized(CONCRETIZED_TITLE_ANIMEDIA_URL, 1, buildEpisodeUrl(CONCRETIZED_TITLE_ANIMEDIA_URL, 1), null, ANIMEDIA_EPISODE_NAME);
	}

	public static CommonTitle getAnimediaNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_ANIMEDIA_URL, 1, null);
	}

	public static CommonTitle getAnidubRegular() {
		return getRegular(REGULAR_TITLE_ANIDUB_URL, null, buildEpisodeUrl(REGULAR_TITLE_ANIDUB_URL, null), null, ANIDUB_EPISODE_NAME);
	}

	public static CommonTitle getAnidubConcretized() {
		return getConcretized(CONCRETIZED_TITLE_ANIDUB_URL, null, buildEpisodeUrl(CONCRETIZED_TITLE_ANIDUB_URL, null), null, ANIDUB_EPISODE_NAME);
	}

	public static CommonTitle getAnidubNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_ANIDUB_URL, null, null);
	}

	public static CommonTitle getAnimepikRegular() {
		return getRegular(REGULAR_TITLE_ANIMEPIK_URL, null, buildEpisodeUrl(REGULAR_TITLE_ANIMEPIK_URL, null), null, ANIMEPIK_EPISODE_NAME);
	}

	public static CommonTitle getAnimepikConcretized() {
		return getConcretized(CONCRETIZED_TITLE_ANIMEPIK_URL, null, buildEpisodeUrl(CONCRETIZED_TITLE_ANIMEPIK_URL, null), null, ANIMEPIK_EPISODE_NAME);
	}

	public static CommonTitle getAnimepikNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_ANIMEPIK_URL, null, null);
	}

	public static CommonTitle getAnilibriaRegular() {
		return getRegular(REGULAR_TITLE_ANILIBRIA_URL, null, buildEpisodeUrl(REGULAR_TITLE_ANILIBRIA_URL, null), null, ANILIBRIA_EPISODE_NAME);
	}

	public static CommonTitle getAnilibriaConcretized() {
		return getConcretized(CONCRETIZED_TITLE_ANILIBRIA_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_ANILIBRIA_URL, null),
				null,
				ANILIBRIA_EPISODE_NAME);
	}

	public static CommonTitle getAnilibriaNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_ANILIBRIA_URL, null, null);
	}

	public static CommonTitle getJisedaiRegular() {
		return getRegular(REGULAR_TITLE_JISEDAI_URL, null, buildEpisodeUrl(REGULAR_TITLE_JISEDAI_URL, null), null, JISEDAI_EPISODE_NAME);
	}

	public static CommonTitle getJisedaiConcretized() {
		return getConcretized(CONCRETIZED_TITLE_JISEDAI_URL, null, buildEpisodeUrl(CONCRETIZED_TITLE_JISEDAI_URL, null), null, JISEDAI_EPISODE_NAME);
	}

	public static CommonTitle getJisedaiNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_JISEDAI_URL, null, null);
	}

	public static CommonTitle getJutsuRegular() {
		return getRegular(REGULAR_TITLE_JUTSU_URL, null, buildEpisodeUrl(REGULAR_TITLE_JUTSU_URL + "/episode-1.html", null), null, JUTSU_EPISODE_NAME);
	}

	public static CommonTitle getJutsuConcretized() {
		return getConcretized(CONCRETIZED_TITLE_JUTSU_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_JUTSU_URL + "/episode-1.html", null),
				null,
				JUTSU_EPISODE_NAME);
	}
	public static CommonTitle getJutsuNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_JUTSU_URL, null, null);
	}

	public static CommonTitle getNineAnimeRegular() {
		return getRegular(REGULAR_TITLE_NINE_ANIME_URL,
				null,
				buildEpisodeUrl(REGULAR_TITLE_NINE_ANIME_URL + "/ep-1", null),
				REGULAR_TITLE_NINE_ANIME_ID,
				NINE_ANIME_EPISODE_NAME);
	}

	public static CommonTitle getNineAnimeConcretized() {
		return getConcretized(CONCRETIZED_TITLE_NINE_ANIME_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_NINE_ANIME_URL + "/ep-1", null),
				CONCRETIZED_TITLE_NINE_ANIME_ID,
				NINE_ANIME_EPISODE_NAME);
	}

	public static CommonTitle getNineAnimeNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_NINE_ANIME_URL, null, NOT_FOUND_ON_MAL_TITLE_NINE_ANIME_ID);
	}

	public static CommonTitle getSovetRomanticaRegular() {
		return getRegular(REGULAR_TITLE_SOVET_ROMANTICA_URL,
				null,
				buildEpisodeUrl(REGULAR_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles", null),
				null,
				SOVET_ROMANTICA_EPISODE_NAME);
	}

	public static CommonTitle getSovetRomanticaConcretized() {
		return getConcretized(CONCRETIZED_TITLE_SOVET_ROMANTICA_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_SOVET_ROMANTICA_URL + "/episode_1-subtitles", null),
				null,
				SOVET_ROMANTICA_EPISODE_NAME);
	}

	public static CommonTitle getSovetRomanticaNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_SOVET_ROMANTICA_URL, null, null);
	}

	public static CommonTitle getShizaProjectRegular() {
		return getRegular(REGULAR_TITLE_SHIZA_PROJECT_URL,
				null,
				buildEpisodeUrl(REGULAR_TITLE_SHIZA_PROJECT_URL, null),
				null,
				SHIZA_PROJECT_EPISODE_NAME);
	}

	public static CommonTitle getShizaProjectConcretized() {
		return getConcretized(CONCRETIZED_TITLE_SHIZA_PROJECT_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_SHIZA_PROJECT_URL, null),
				null,
				SHIZA_PROJECT_EPISODE_NAME);
	}

	public static CommonTitle getShizaProjectNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_SHIZA_PROJECT_URL, null, null);
	}

	public static CommonTitle getJamClubRegular() {
		return getRegular(REGULAR_TITLE_JAM_CLUB_URL, null, buildEpisodeUrl(REGULAR_TITLE_JAM_CLUB_URL, null), null, JAM_CLUB_EPISODE_NAME);
	}

	public static CommonTitle getJamClubConcretized() {
		return getConcretized(CONCRETIZED_TITLE_JAM_CLUB_URL, null, buildEpisodeUrl(CONCRETIZED_TITLE_JAM_CLUB_URL, null), null, JAM_CLUB_EPISODE_NAME);
	}

	public static CommonTitle getJamClubNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_JAM_CLUB_URL, null, null);
	}

	public static CommonTitle getAnythingGroupRegular() {
		return getRegular(REGULAR_TITLE_ANYTHING_GROUP_URL,
				null,
				buildEpisodeUrl(REGULAR_TITLE_ANYTHING_GROUP_URL, null),
				null,
				ANYTHING_GROUP_EPISODE_NAME);
	}

	public static CommonTitle getAnythingGroupConcretized() {
		return getConcretized(CONCRETIZED_TITLE_ANYTHING_GROUP_URL,
				null,
				buildEpisodeUrl(CONCRETIZED_TITLE_ANYTHING_GROUP_URL, null),
				null,
				ANYTHING_GROUP_EPISODE_NAME);
	}

	public static CommonTitle getAnythingGroupNotFoundOnMal() {
		return getNotFoundOnMal(NOT_FOUND_ON_MAL_TITLE_ANYTHING_GROUP_URL, null, null);
	}

	public static CommonTitle getRegular(String url, Integer dataList, String episodeUrl, String dataId, String episodeName) {
		return buildCommonTitle(REGULAR_TITLE_ID,
				url,
				REGULAR_TITLE_MAL_ID,
				dataList,
				TitleType.REGULAR,
				Lists.newArrayList(CommonEpisode.builder()
						.malEpisodeId(1)
						.name(episodeName)
						.id(1)
						.url(episodeUrl)
						.build()));
	}

	public static CommonTitle getConcretized(String url, Integer dataList, String episodeUrl, String dataId, String episodeName) {
		return buildCommonTitle(CONCRETIZED_TITLE_ID,
				url,
				CONCRETIZED_TITLE_MAL_ID,
				dataList,
				TitleType.CONCRETIZED,
				Lists.newArrayList(CommonEpisode.builder()
						.malEpisodeId(1)
						.name(episodeName)
						.id(1)
						.url(episodeUrl)
						.build()));
	}

	public static String buildEpisodeUrl(String url, Integer dataList) {
		return dataList != null ? url + "/" + dataList + "/" + 1 : url;
	}

	private static CommonTitle getNotFoundOnMal(String url, Integer dataList, String dataId) {
		return buildCommonTitle(NOT_FOUND_ON_MAL_TITLE_ID, url, null, dataList, null, Collections.emptyList());
	}

	private static CommonTitle buildCommonTitle(String id, String url, Integer malId, Integer dataList, TitleType type, List<CommonEpisode> episodes) {
		return CommonTitle.builder()
				.id(id)
				.url(url)
				.malId(malId)
				.dataList(dataList)
				.type(type)
				.episodes(episodes)
				.build();
	}

	private static Map<FanDubSource, Supplier<CommonTitle>> buildRtm() {
		Map<FanDubSource, Supplier<CommonTitle>> result = new HashMap<>();
		result.put(FanDubSource.ANIDUB, CommonTitleTestBuilder::getAnidubRegular);
		result.put(FanDubSource.ANILIBRIA, CommonTitleTestBuilder::getAnilibriaRegular);
		result.put(FanDubSource.ANIMEDIA, CommonTitleTestBuilder::getAnimediaRegular);
		result.put(FanDubSource.ANIMEPIK, CommonTitleTestBuilder::getAnimepikRegular);
		result.put(FanDubSource.ANYTHING_GROUP, CommonTitleTestBuilder::getAnythingGroupRegular);
		result.put(FanDubSource.JAMCLUB, CommonTitleTestBuilder::getJamClubRegular);
		result.put(FanDubSource.JISEDAI, CommonTitleTestBuilder::getJisedaiRegular);
		result.put(FanDubSource.JUTSU, CommonTitleTestBuilder::getJutsuRegular);
		result.put(FanDubSource.SHIZAPROJECT, CommonTitleTestBuilder::getShizaProjectRegular);
		result.put(FanDubSource.SOVETROMANTICA, CommonTitleTestBuilder::getSovetRomanticaRegular);
		result.put(FanDubSource.NINEANIME, CommonTitleTestBuilder::getNineAnimeRegular);
		return result;
	}

	private static Map<FanDubSource, Supplier<CommonTitle>> buildCtm() {
		Map<FanDubSource, Supplier<CommonTitle>> result = new HashMap<>();
		result.put(FanDubSource.ANIDUB, CommonTitleTestBuilder::getAnidubConcretized);
		result.put(FanDubSource.ANILIBRIA, CommonTitleTestBuilder::getAnilibriaConcretized);
		result.put(FanDubSource.ANIMEDIA, CommonTitleTestBuilder::getAnimediaConcretized);
		result.put(FanDubSource.ANIMEPIK, CommonTitleTestBuilder::getAnimepikConcretized);
		result.put(FanDubSource.ANYTHING_GROUP, CommonTitleTestBuilder::getAnythingGroupConcretized);
		result.put(FanDubSource.JAMCLUB, CommonTitleTestBuilder::getJamClubConcretized);
		result.put(FanDubSource.JISEDAI, CommonTitleTestBuilder::getJisedaiConcretized);
		result.put(FanDubSource.JUTSU, CommonTitleTestBuilder::getJutsuConcretized);
		result.put(FanDubSource.SHIZAPROJECT, CommonTitleTestBuilder::getShizaProjectConcretized);
		result.put(FanDubSource.SOVETROMANTICA, CommonTitleTestBuilder::getSovetRomanticaConcretized);
		result.put(FanDubSource.NINEANIME, CommonTitleTestBuilder::getNineAnimeConcretized);
		return result;
	}

	private static Map<FanDubSource, Supplier<CommonTitle>> buildNftm() {
		Map<FanDubSource, Supplier<CommonTitle>> result = new HashMap<>();
		result.put(FanDubSource.ANIDUB, CommonTitleTestBuilder::getAnidubNotFoundOnMal);
		result.put(FanDubSource.ANILIBRIA, CommonTitleTestBuilder::getAnilibriaNotFoundOnMal);
		result.put(FanDubSource.ANIMEDIA, CommonTitleTestBuilder::getAnimediaNotFoundOnMal);
		result.put(FanDubSource.ANIMEPIK, CommonTitleTestBuilder::getAnimepikNotFoundOnMal);
		result.put(FanDubSource.ANYTHING_GROUP, CommonTitleTestBuilder::getAnythingGroupNotFoundOnMal);
		result.put(FanDubSource.JAMCLUB, CommonTitleTestBuilder::getJamClubNotFoundOnMal);
		result.put(FanDubSource.JISEDAI, CommonTitleTestBuilder::getJisedaiNotFoundOnMal);
		result.put(FanDubSource.JUTSU, CommonTitleTestBuilder::getJutsuNotFoundOnMal);
		result.put(FanDubSource.SHIZAPROJECT, CommonTitleTestBuilder::getShizaProjectNotFoundOnMal);
		result.put(FanDubSource.SOVETROMANTICA, CommonTitleTestBuilder::getSovetRomanticaNotFoundOnMal);
		result.put(FanDubSource.NINEANIME, CommonTitleTestBuilder::getNineAnimeNotFoundOnMal);
		return result;
	}
}
