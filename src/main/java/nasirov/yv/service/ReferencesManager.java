package nasirov.yv.service;


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
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.research.ws.wadl.HTTPMethods.GET;

/**
 * Created by nasirov.yv
 */
@Service
public class ReferencesManager {
	/**
	 * For a title url on animedia and a title on mal
	 */
	private static final String REFERENCE_MAL_TITLE_TO_ANIMEDIA_TITLE = "(?<fullUrl>http://online\\.animedia\\.tv/(?<url>anime/.+)/(?<dataList>\\d{1,3})/(?<firstEpisode>\\d{1,3}))[\\s\\t]+(?<titleOnMAL>.+)";
	
	private static final String CONCERTIZE_EPISODE = ".+[\\s\\t]+((?<min>\\d{1,3})-(?<max>\\d{1,3}|[x]{3}))+";
	
	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;
	
	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;
	
	@Value("classpath:${resources.rawReference.name}")
	private Resource rawReferencesResource;
	
	private HttpCaller httpCaller;
	
	private RequestParametersBuilder requestParametersBuilder;
	
	private AnimediaHTMLParser animediaHTMLParser;
	
	private WrappedObjectMapper wrappedObjectMapper;
	
	private RoutinesIO routinesIO;
	
	@Autowired
	public ReferencesManager(HttpCaller httpCaller,
							 @Qualifier("animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
							 AnimediaHTMLParser animediaHTMLParser,
							 WrappedObjectMapper wrappedObjectMapper,
							 RoutinesIO routinesIO) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.wrappedObjectMapper = wrappedObjectMapper;
		this.routinesIO = routinesIO;
	}
	
	/**
	 * Creates container with the anime references
	 *
	 * @return the references
	 */
	@Cacheable(value = "multiSeasonsReferencesCache")
	public Set<AnimediaMALTitleReferences> getMultiSeasonsReferences() {
		//десериализируем маппинг мультисезонного аниме
		return wrappedObjectMapper.unmarshal(rawToPretty(rawReferencesResource), AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}
	
	private String rawToPretty(Resource rawReferencesResource) {
		return createJsonReferences(routinesIO.readFromResource(rawReferencesResource));
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
			stringBuilder.append("{\"url\":\"").append(matcher.group("url"))
					.append("\",").append("\"dataList\":\"").append(matcher.group("dataList"))
					.append("\",").append("\"firstEpisode\":\"").append(matcher.group("firstEpisode"))
					.append("\",").append("\"titleOnMAL\":\"").append(titleOnMAL.trim().toLowerCase())
					.append("\",");
			if (min != null) {
				stringBuilder.append("\"min\":\"").append(min)
						.append("\",");
			} else {
				stringBuilder.append("\"min\":").append(min)
						.append(",");
			}
			if (max != null) {
				stringBuilder.append("\"max\":\"").append(max)
						.append("\"},");
			} else {
				stringBuilder.append("\"max\":").append(max)
						.append("},");
			}
		}
		stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
	
	private static String[] getConcertizedEpisode(String s) {
		Pattern pattern = Pattern.compile(CONCERTIZE_EPISODE);
		Matcher matcher = pattern.matcher(s);
		String arr[] = null;
		if (matcher.find()) {
			arr = new String[2];
			arr[0] = matcher.group("min");
			arr[1] = matcher.group("max");
		}
		return arr;
	}
	
	/**
	 * Updates multiseasons anime references
	 *
	 * @param references the references
	 */
	public void updateReferences(@NotEmpty Set<AnimediaMALTitleReferences> references) {
		Map<String, Map<String, String>> animediaRequestParameters = requestParametersBuilder.build();
		//обновляем информацию по сериям min,max,currentMax,firstEpisode
		Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache = new HashMap<>();
		for (AnimediaMALTitleReferences ref : references) {
			if (ref.getTitleOnMAL().equalsIgnoreCase("none")) {
				continue;
			}
			String url = animediaOnlineTv + ref.getUrl();
			Map<String, Map<String, String>> seasonsAndEpisodes;
			if (seasonsAndEpisodesCache.containsKey(url)) {
				seasonsAndEpisodes = seasonsAndEpisodesCache.get(url);
			} else {
				HttpResponse response = httpCaller.call(url, GET, animediaRequestParameters);
				seasonsAndEpisodes = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
				seasonsAndEpisodesCache.put(url, seasonsAndEpisodes);
			}
			if (seasonsAndEpisodes != null) {
				for (Map.Entry<String, Map<String, String>> entry : seasonsAndEpisodes.entrySet()) {
					for (Map.Entry<String, String> seasons : entry.getValue().entrySet()) {
						if (ref.getDataList().equals(seasons.getKey())) {
							HttpResponse resp = httpCaller.call(animediaEpisodesList + entry.getKey() + "/" + seasons.getKey(), GET, animediaRequestParameters);
							Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange(resp);
							if (episodesRange.size() != 0) {
								for (Map.Entry<String, List<String>> range : episodesRange.entrySet()) {
									List<String> value = range.getValue();
									String key = range.getKey();
									String firstElement = value.get(0);
									Integer intKey = null;
									Integer intFirstElement = null;
									//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
									//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 101 из 100
									if (!key.equalsIgnoreCase("xxx") && !key.equalsIgnoreCase("xx")) {
										intKey = Integer.parseInt(key);
										intFirstElement = Integer.parseInt(firstElement);
									}
									ref.setCurrentMax(value.get(value.size() - 1));
									ref.setFirstEpisode(firstElement);
									ref.setMin(firstElement);
									ref.setMax(intKey != null && intKey < intFirstElement ? Integer.toString(intFirstElement + intKey) : key);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Compare multiseasons references and user watching titles
	 *
	 * @param references     the  multiseasons references
	 * @param watchingTitles the user watching titles
	 * @return the matched user references
	 */
	public Set<AnimediaMALTitleReferences> getMatchedReferences(@NotEmpty Set<AnimediaMALTitleReferences> references, @NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		Set<AnimediaMALTitleReferences> tempReferences = new LinkedHashSet<>();
		if (references != null && watchingTitles != null) {
			for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
				references.stream()
						.filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle()))
						.forEach(set -> {
							set.setPosterUrl(userMALTitleInfo.getPosterUrl());
							tempReferences.add(new AnimediaMALTitleReferences(set));
						});
			}
		}
		return tempReferences;
	}
	
	public void checkReferences(@NotEmpty Set<Anime> multiSeasonsAnime, @NotEmpty Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> readFromRaw = readFromRaw(allReferences);
		compareMaps(multiSeasonsAnime, readFromRaw);
	}
	
	/**
	 * Updates currentMax matched reference and set titleOnMal for currentlyUpdatedTitle
	 *
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param currentlyUpdatedTitle the currently updated title on animedia
	 */
	public void updateReferences(@NotEmpty Set<AnimediaMALTitleReferences> matchedAnimeFromCache, @NotNull AnimediaMALTitleReferences currentlyUpdatedTitle) {
		matchedAnimeFromCache.stream()
				.filter(set -> set.getUrl().equals(currentlyUpdatedTitle.getUrl())
						&& set.getDataList().equals(currentlyUpdatedTitle.getDataList()))
				.forEach(set -> {
					set.setCurrentMax(currentlyUpdatedTitle.getCurrentMax());
					currentlyUpdatedTitle.setTitleOnMAL(set.getTitleOnMAL());
				});
	}
	
	private Map<String, String> readFromRaw(@NotEmpty Set<AnimediaMALTitleReferences> allReferences) {
		Map<String, String> urlTitle = new HashMap<>();
		allReferences.forEach(set -> urlTitle.put(animediaOnlineTv + set.getUrl() + "/" + set.getDataList() + "/" + set.getFirstEpisode(), set.getTitleOnMAL()));
		return urlTitle;
	}
	
	private boolean compareMaps(@NotEmpty Set<Anime> multi, @NotEmpty Map<String, String> raw) {
		boolean fullMatch = true;
		for (Anime anime : multi) {
			if (!raw.containsKey(anime.getFullUrl())) {
				fullMatch = false;
				System.out.println("В raw нет " + anime.getFullUrl());
			}
		}
		return fullMatch && multi.size() == raw.size();
	}
}
