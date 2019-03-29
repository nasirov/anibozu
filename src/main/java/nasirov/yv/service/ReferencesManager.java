package nasirov.yv.service;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class ReferencesManager {

	/**
	 * For a title url on animedia and a title on mal
	 */
	private static final String REFERENCE_MAL_TITLE_TO_ANIMEDIA_TITLE =
			"(?<fullUrl>https://online\\.animedia\\.tv/(?<url>anime/.+)/(?<dataList>\\d{1,3})/(?<firstEpisode>\\d{1,3}))[\\s\\t]+(?<titleOnMAL>.+)";

	private static final String CONCERTIZE_EPISODE = ".+[\\s\\t]+((?<min>\\d{1,3})-(?<max>\\d{1,3}|[x]{3}))+";

	private static final String NONE = "none";

	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;

	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;

	@Value("${resources.tempFolder.name}")
	private String tempFolderName;

	@Value("${resources.tempRawReferences.name}")
	private String tempRawReferencesName;

	@Value("classpath:${resources.rawReference.name}")
	private Resource rawReferencesResource;

	private HttpCaller httpCaller;

	private RequestParametersBuilder requestParametersBuilder;

	private AnimediaHTMLParser animediaHTMLParser;

	@Autowired
	public ReferencesManager(HttpCaller httpCaller, @Qualifier("animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
			AnimediaHTMLParser animediaHTMLParser) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
	}

	/**
	 * Creates container with the anime references
	 *
	 * @return the references
	 */
	@Cacheable(value = "multiSeasonsReferencesCache")
	public Set<AnimediaMALTitleReferences> getMultiSeasonsReferences() {
		return WrappedObjectMapper.unmarshal(rawToPretty(rawReferencesResource), AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}

	private String rawToPretty(Resource rawReferencesResource) {
		return createJsonReferences(RoutinesIO.readFromResource(rawReferencesResource));
	}

	/**
	 * Creates json from raw references
	 *
	 * @param content the string with all raw references
	 * @return the json references
	 */
	private String createJsonReferences(@NotEmpty String content) {
		Pattern pattern = Pattern.compile(REFERENCE_MAL_TITLE_TO_ANIMEDIA_TITLE);
		Matcher matcher = pattern.matcher(content);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		while (matcher.find()) {
			String titleOnMAL = matcher.group("titleOnMAL");
			String[] concertizedEpisode = getConcertizedEpisode(titleOnMAL);
			String min = null;
			String max = null;
			if (concertizedEpisode != null) {
				min = concertizedEpisode[0];
				max = concertizedEpisode[1];
				titleOnMAL = titleOnMAL.replaceAll("\\d{1,3}-(\\d{1,3}|[x]{3})", "");
			}
			stringBuilder.append("{\"url\":\"").append(matcher.group("url")).append("\",").append("\"dataList\":\"").append(matcher.group("dataList"))
					.append("\",").append("\"firstEpisode\":\"").append(matcher.group("firstEpisode")).append("\",").append("\"titleOnMAL\":\"")
					.append(titleOnMAL.trim().toLowerCase()).append("\",");
			if (min != null) {
				stringBuilder.append("\"min\":\"").append(min).append("\",");
			} else {
				stringBuilder.append("\"min\":").append(min).append(",");
			}
			if (max != null) {
				stringBuilder.append("\"max\":\"").append(max).append("\"},");
			} else {
				stringBuilder.append("\"max\":").append(max).append("},");
			}
		}
		stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
		stringBuilder.append("]");
		return stringBuilder.toString();
	}

	private static String[] getConcertizedEpisode(String s) {
		Pattern pattern = Pattern.compile(CONCERTIZE_EPISODE);
		Matcher matcher = pattern.matcher(s);
		String[] arr = null;
		if (matcher.find()) {
			arr = new String[2];
			arr[0] = matcher.group("min");
			arr[1] = matcher.group("max");
		}
		return arr;
	}

	/**
	 * Updates multiseasons anime references
	 * min,max,first episode,current max
	 *
	 * @param references the references
	 */
	public void updateReferences(@NotEmpty Set<AnimediaMALTitleReferences> references) {
		Map<String, Map<String, String>> animediaRequestParameters = requestParametersBuilder.build();
		Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache = new HashMap<>();
		for (AnimediaMALTitleReferences reference : references) {
			if (reference.getTitleOnMAL().equalsIgnoreCase(NONE)) {
				continue;
			}
			String url = animediaOnlineTv + reference.getUrl();
			Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap;
			if (seasonsAndEpisodesCache.containsKey(url)) {
				animeIdSeasonsAndEpisodesMap = seasonsAndEpisodesCache.get(url);
			} else {
				animeIdSeasonsAndEpisodesMap = getTitleHtmlAndPutInCache(url, animediaRequestParameters, seasonsAndEpisodesCache);
			}
			if (animeIdSeasonsAndEpisodesMap != null && !animeIdSeasonsAndEpisodesMap.isEmpty()) {
				for (Map.Entry<String, Map<String, String>> animeIdSeasonsAndEpisodesEntry : animeIdSeasonsAndEpisodesMap.entrySet()) {
					Map<String, String> seasonsAndEpisodesMap = animeIdSeasonsAndEpisodesEntry.getValue();
					for (Map.Entry<String, String> seasonsAndEpisodesEntry : seasonsAndEpisodesMap.entrySet()) {
						String dataList = seasonsAndEpisodesEntry.getKey();
						if (reference.getDataList().equals(dataList)) {
							String animeId = animeIdSeasonsAndEpisodesEntry.getKey();
							HttpResponse resp = httpCaller.call(animediaEpisodesList + animeId + "/" + dataList, HttpMethod.GET, animediaRequestParameters);
							Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange(resp);
							if (!episodesRange.isEmpty()) {
								for (Map.Entry<String, List<String>> range : episodesRange.entrySet()) {
									List<String> episodesList = range.getValue();
									dataList = range.getKey();
									String firstEpisodeAndMin = episodesList.get(0);
									Integer intDataList = null;
									Integer intFirstEpisodeAndMin = null;
									//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
									//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 101 из 100
									if (!dataList.equalsIgnoreCase("xxx") && !dataList.equalsIgnoreCase("xx")) {
										intDataList = Integer.parseInt(dataList);
										intFirstEpisodeAndMin = Integer.parseInt(firstEpisodeAndMin);
									}
									int lastIndex = episodesList.size() - 1;
									String currentMax = episodesList.get(lastIndex);
									reference.setCurrentMax(currentMax);
									reference.setFirstEpisode(firstEpisodeAndMin);
									reference.setMin(firstEpisodeAndMin);
									reference.setMax(
											intDataList != null && intDataList < intFirstEpisodeAndMin ? Integer.toString(intFirstEpisodeAndMin + intDataList) : dataList);
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	private Map<String, Map<String, String>> getTitleHtmlAndPutInCache(String url, Map<String, Map<String, String>> animediaRequestParameters,
			Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache) {
		HttpResponse response = httpCaller.call(url, HttpMethod.GET, animediaRequestParameters);
		Map<String, Map<String, String>> seasonsAndEpisodes = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
		seasonsAndEpisodesCache.put(url, seasonsAndEpisodes);
		return seasonsAndEpisodes;
	}

	/**
	 * Compare multiseasons references and user watching titles and
	 * Creates container with matched anime
	 *
	 * @param references the  multiseasons references
	 * @param watchingTitles the user watching titles
	 * @return the matched user references
	 */
	public Set<AnimediaMALTitleReferences> getMatchedReferences(@NotEmpty Set<AnimediaMALTitleReferences> references,
			@NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		Set<AnimediaMALTitleReferences> tempReferences = new LinkedHashSet<>();
		if (references != null && watchingTitles != null) {
			for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
				references.stream().filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle())).forEach(set -> {
					set.setPosterUrl(userMALTitleInfo.getPosterUrl());
					tempReferences.add(new AnimediaMALTitleReferences(set));
				});
			}
		}
		return tempReferences;
	}

	/**
	 * Compare multi seasons titles from animedia search list with multi seasons references from resources
	 *
	 * @param multiSeasonsAnime multi seasons titles from animedia
	 * @param allReferences all multi seasons references from resources
	 * @return true if multi seasons references from resources are full, if false then we must add the new reference to the raw mapping
	 */
	public boolean isReferencesAreFull(@NotEmpty Set<Anime> multiSeasonsAnime, @NotEmpty Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> readFromRaw = convertReferencesSetToMap(allReferences);
		return compareMaps(multiSeasonsAnime, readFromRaw);
	}

	/**
	 * Updates currentMax matched reference and set titleOnMal for currentlyUpdatedTitle
	 *
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param currentlyUpdatedTitle the currently updated title on animedia
	 */
	public void updateCurrentMax(@NotEmpty Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			@NotNull AnimediaMALTitleReferences currentlyUpdatedTitle) {
		matchedAnimeFromCache.stream()
				.filter(set -> set.getUrl().equals(currentlyUpdatedTitle.getUrl()) && set.getDataList().equals(currentlyUpdatedTitle.getDataList()))
				.forEach(set -> {
					set.setCurrentMax(currentlyUpdatedTitle.getCurrentMax());
					currentlyUpdatedTitle.setTitleOnMAL(set.getTitleOnMAL());
				});
	}

	private Map<String, String> convertReferencesSetToMap(@NotEmpty Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> urlTitle = new HashMap<>();
		allReferences
				.forEach(set -> urlTitle.put(animediaOnlineTv + set.getUrl() + "/" + set.getDataList() + "/" + set.getFirstEpisode(), set.getTitleOnMAL()));
		return urlTitle;
	}

	private boolean compareMaps(@NotEmpty Set<Anime> multi, @NotEmpty Map<String, String> raw) {
		boolean fullMatch = true;
		for (Anime anime : multi) {
			String fullUrl = anime.getFullUrl();
			if (!raw.containsKey(fullUrl)) {
				fullMatch = false;
				try {
					if (!RoutinesIO.isDirectoryExists(tempFolderName)) {
						RoutinesIO.mkDir(tempFolderName);
					}
				} catch (NotDirectoryException e) {
					log.error("Check system.properties variable resources.tempFolder.name! {} is not a directory!", tempFolderName);
				}
				String prefix = tempFolderName + File.separator;
				RoutinesIO.writeToFile(prefix + tempRawReferencesName, fullUrl, true);
				log.warn("Not found in the raw references {} Please, add missing reference to the resources!", fullUrl);
			}
		}
		return fullMatch;
	}
}
