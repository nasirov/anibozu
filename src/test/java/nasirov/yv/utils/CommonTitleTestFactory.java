package nasirov.yv.utils;

import static nasirov.yv.utils.MalTitleTestFactory.CONCRETIZED_TITLE_MAL_ID;
import static nasirov.yv.utils.MalTitleTestFactory.REGULAR_TITLE_MAL_ID;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonEpisode;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.fandub.common.Id;
import nasirov.yv.starter.common.dto.fandub.common.TitleType;
import org.springframework.util.Assert;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class CommonTitleTestFactory {

	public static final String ANIDUB_EPISODE_NAME = "1 серия";

	public static final String ANILIBRIA_EPISODE_NAME = "Серия 1";

	public static final String SHIZA_PROJECT_EPISODE_NAME = "1";

	public static final String REGULAR_TITLE_ID = "2";

	public static final String REGULAR_TITLE_ANIDUB_URL = "anime/full/" + REGULAR_TITLE_ID + "-regular-title-url" + ".html";

	public static final String REGULAR_TITLE_ANILIBRIA_URL = "release/" + REGULAR_TITLE_ID + "-regular-title-url.html";

	public static final String REGULAR_TITLE_SHIZA_PROJECT_URL = "releases/regular-title-url-" + REGULAR_TITLE_ID;

	public static final String CONCRETIZED_TITLE_ID = "4";

	public static final String CONCRETIZED_TITLE_ANIDUB_URL =
			"anime/full/" + CONCRETIZED_TITLE_ID + "-concretized-title-url.html";

	public static final String CONCRETIZED_TITLE_ANILIBRIA_URL =
			"release/" + CONCRETIZED_TITLE_ID + "-concretized-title-url.html";

	public static final String CONCRETIZED_TITLE_SHIZA_PROJECT_URL = "releases/concretized-title-url-" + CONCRETIZED_TITLE_ID;

	private static final Map<FandubSource, Supplier<CommonTitle>> REGULAR_TITLES_METHODS = buildRtm();

	private static final Map<FandubSource, Supplier<CommonTitle>> CONCRETIZED_TITLES_METHODS = buildCtm();

	public static Map<FandubSource, List<CommonTitle>> buildRegularCommonTitles(Set<FandubSource> fandubSources) {
		Assert.isTrue(fandubSources.size() == 3, "fandubSources's size should be 3");
		List<FandubSource> list = new ArrayList<>(fandubSources);
		Map<FandubSource, List<CommonTitle>> result = new LinkedHashMap<>();
		result.put(list.get(0), Lists.newArrayList(CommonTitleTestFactory.buildRegularTitle(list.get(0), 10)));
		result.put(list.get(1), Lists.newArrayList(CommonTitleTestFactory.buildRegularTitle(list.get(1), null)));
		result.put(list.get(2), Lists.newArrayList());
		return result;
	}

	public static Map<FandubSource, List<CommonTitle>> buildConcretizedCommonTitles(Set<FandubSource> fandubSources) {
		Assert.isTrue(fandubSources.size() == 3, "fandubSources's size should be 3");
		List<FandubSource> list = new ArrayList<>(fandubSources);
		Map<FandubSource, List<CommonTitle>> result = new LinkedHashMap<>();
		result.put(list.get(0), Lists.newArrayList());
		result.put(list.get(1), Lists.newArrayList(CommonTitleTestFactory.buildConcretizedTitle(list.get(1))));
		result.put(list.get(2), Lists.newArrayList());
		return result;
	}

	public static Map<FandubSource, List<CommonTitle>> buildNotFoundOnFandubCommonTitles(Set<FandubSource> fandubSources) {
		return fandubSources.stream()
				.collect(Collectors.toMap(Function.identity(), x -> Collections.emptyList(), (o, n) -> o, LinkedHashMap::new));
	}

	public static CommonTitle buildRegularTitle(FandubSource fandubSource, Integer nonDefaultEpisodeNumber) {
		CommonTitle result = REGULAR_TITLES_METHODS.get(fandubSource).get();
		if (null != nonDefaultEpisodeNumber) {
			CommonEpisode episode = result.getEpisodes().get(0);
			result.setEpisodes(Lists.newArrayList(buildEpisode(episode.getUrl(), episode.getName(), nonDefaultEpisodeNumber)));
		}
		return result;
	}

	public static CommonTitle buildConcretizedTitle(FandubSource fandubSource) {
		return CONCRETIZED_TITLES_METHODS.get(fandubSource).get();
	}

	public static CommonTitle getRegular(String url, String episodeUrl, String episodeName) {
		return buildCommonTitle(REGULAR_TITLE_ID, url, REGULAR_TITLE_MAL_ID, TitleType.REGULAR,
				Lists.newArrayList(buildEpisode(episodeUrl, episodeName, 1)));
	}

	public static CommonTitle getConcretized(String url, String episodeUrl, String episodeName) {
		return buildCommonTitle(CONCRETIZED_TITLE_ID, url, CONCRETIZED_TITLE_MAL_ID, TitleType.CONCRETIZED,
				Lists.newArrayList(buildEpisode(episodeUrl, episodeName, 1)));
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

	private static CommonEpisode buildEpisode(String episodeUrl, String episodeName, Integer malEpisodeId) {
		return CommonEpisode.builder()
				.malEpisodeId(malEpisodeId)
				.name(episodeName)
				.id(malEpisodeId)
				.number(malEpisodeId.toString())
				.url(episodeUrl)
				.build();
	}

	private static Map<FandubSource, Supplier<CommonTitle>> buildRtm() {
		return Map.of(FandubSource.ANIDUB,
				() -> getRegular(REGULAR_TITLE_ANIDUB_URL, REGULAR_TITLE_ANIDUB_URL, ANIDUB_EPISODE_NAME), FandubSource.ANILIBRIA,
				() -> getRegular(REGULAR_TITLE_ANILIBRIA_URL, REGULAR_TITLE_ANILIBRIA_URL, ANILIBRIA_EPISODE_NAME),
				FandubSource.SHIZAPROJECT,
				() -> getRegular(REGULAR_TITLE_SHIZA_PROJECT_URL, REGULAR_TITLE_SHIZA_PROJECT_URL, SHIZA_PROJECT_EPISODE_NAME));
	}

	private static Map<FandubSource, Supplier<CommonTitle>> buildCtm() {
		return Map.of(FandubSource.ANIDUB,
				() -> getConcretized(CONCRETIZED_TITLE_ANIDUB_URL, CONCRETIZED_TITLE_ANIDUB_URL, ANIDUB_EPISODE_NAME),
				FandubSource.ANILIBRIA,
				() -> getConcretized(CONCRETIZED_TITLE_ANILIBRIA_URL, CONCRETIZED_TITLE_ANILIBRIA_URL, ANILIBRIA_EPISODE_NAME),
				FandubSource.SHIZAPROJECT,
				() -> getConcretized(CONCRETIZED_TITLE_SHIZA_PROJECT_URL, CONCRETIZED_TITLE_SHIZA_PROJECT_URL,
						SHIZA_PROJECT_EPISODE_NAME));
	}
}
