package nasirov.yv.utils;

import static nasirov.yv.utils.MalTitleTestFactory.CONCRETIZED_TITLE_MAL_ID;
import static nasirov.yv.utils.MalTitleTestFactory.REGULAR_TITLE_MAL_ID;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.Id;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.TitleType;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class CommonTitleTestFactory {

	public static final String ANIDUB_EPISODE_NAME = "1 серия";

	public static final String ANILIBRIA_EPISODE_NAME = "Серия 1";

	public static final String REGULAR_TITLE_ID = "2";

	public static final String REGULAR_TITLE_ANIDUB_URL = "anime/full/" + REGULAR_TITLE_ID + "-regular-title-url" + ".html";

	public static final String REGULAR_TITLE_ANILIBRIA_URL = "release/" + REGULAR_TITLE_ID + "-regular-title-url.html";

	public static final String CONCRETIZED_TITLE_ID = "4";

	public static final String CONCRETIZED_TITLE_ANIDUB_URL =
			"anime/full/" + CONCRETIZED_TITLE_ID + "-concretized-title-url.html";

	public static final String CONCRETIZED_TITLE_ANILIBRIA_URL =
			"release/" + CONCRETIZED_TITLE_ID + "-concretized-title-url.html";

	private static final Map<FanDubSource, Supplier<CommonTitle>> REGULAR_TITLES_METHODS = buildRtm();

	private static final Map<FanDubSource, Supplier<CommonTitle>> CONCRETIZED_TITLES_METHODS = buildCtm();

	public static Map<FanDubSource, List<CommonTitle>> buildRegularCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(
						Collectors.toMap(Function.identity(), x -> Lists.newArrayList(CommonTitleTestFactory.buildRegularTitle(x))));
	}

	public static Map<FanDubSource, List<CommonTitle>> buildConcretizedCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream()
				.collect(
						Collectors.toMap(Function.identity(),
								x -> Lists.newArrayList(CommonTitleTestFactory.buildConcretizedTitle(x))));
	}

	public static Map<FanDubSource, List<CommonTitle>> buildNotFoundOnFandubCommonTitles(Set<FanDubSource> fanDubSources) {
		return fanDubSources.stream().collect(Collectors.toMap(Function.identity(), x -> Collections.emptyList()));
	}

	public static CommonTitle buildRegularTitle(FanDubSource fanDubSource) {
		return REGULAR_TITLES_METHODS.get(fanDubSource).get();
	}

	public static CommonTitle buildConcretizedTitle(FanDubSource fanDubSource) {
		return CONCRETIZED_TITLES_METHODS.get(fanDubSource).get();
	}

	public static CommonTitle getRegular(String url, String episodeUrl, String episodeName) {
		return buildCommonTitle(REGULAR_TITLE_ID, url, REGULAR_TITLE_MAL_ID, TitleType.REGULAR, Lists.newArrayList(
				CommonEpisode.builder().malEpisodeId(1).name(episodeName).id(1).number("1").url(episodeUrl).build()));
	}

	public static CommonTitle getConcretized(String url, String episodeUrl, String episodeName) {
		return buildCommonTitle(CONCRETIZED_TITLE_ID, url, CONCRETIZED_TITLE_MAL_ID, TitleType.CONCRETIZED, Lists.newArrayList(
				CommonEpisode.builder().malEpisodeId(1).name(episodeName).id(1).number("1").url(episodeUrl).build()));
	}

	private static CommonTitle buildCommonTitle(String id, String url, Integer malId, TitleType type,
			List<CommonEpisode> episodes) {
		return CommonTitle.builder()
				.id(Id.builder().id(id).build())
				.url(url)
				.malId(malId)
				.type(type)
				.originalName("original name")
				.ruName("название на русском")
				.episodes(episodes)
				.build();
	}

	private static Map<FanDubSource, Supplier<CommonTitle>> buildRtm() {
		return Map.of(FanDubSource.ANIDUB,
				() -> getRegular(REGULAR_TITLE_ANIDUB_URL, REGULAR_TITLE_ANIDUB_URL, ANIDUB_EPISODE_NAME), FanDubSource.ANILIBRIA,
				() -> getRegular(REGULAR_TITLE_ANILIBRIA_URL, REGULAR_TITLE_ANILIBRIA_URL, ANILIBRIA_EPISODE_NAME));
	}

	private static Map<FanDubSource, Supplier<CommonTitle>> buildCtm() {
		return Map.of(FanDubSource.ANIDUB,
				() -> getConcretized(CONCRETIZED_TITLE_ANIDUB_URL, CONCRETIZED_TITLE_ANIDUB_URL, ANIDUB_EPISODE_NAME),
				FanDubSource.ANILIBRIA,
				() -> getConcretized(CONCRETIZED_TITLE_ANILIBRIA_URL, CONCRETIZED_TITLE_ANILIBRIA_URL, ANILIBRIA_EPISODE_NAME));
	}
}
