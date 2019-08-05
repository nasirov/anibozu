package nasirov.yv.service;

import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodesUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedAndOngoing;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;
import static nasirov.yv.util.AnimediaUtils.isTitleUpdated;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
public class ReferencesManager {

	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;

	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;

	@Value("${urls.online.animedia.anime.episodes.postfix}")
	private String animediaEpisodesListPostfix;

	@Value("${urls.raw.githubusercontent.com.references}")
	private String referencesFromGitHubUrl;

	@Value("${resources.tempFolder.name}")
	private String tempFolderName;

	@Value("${resources.tempRawReferences.name}")
	private String tempRawReferencesName;

	@Value("classpath:${resources.rawReference.name}")
	private Resource referencesResourceJson;

	private Map<String, Map<String, String>> animediaRequestParameters;

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

	@PostConstruct
	public void init() {
		animediaRequestParameters = requestParametersBuilder.build();
	}

	/**
	 * Creates container with the anime references
	 *
	 * @return the references
	 */
	public Set<AnimediaMALTitleReferences> getMultiSeasonsReferences() {
		HttpResponse response = httpCaller.call(referencesFromGitHubUrl, HttpMethod.GET, animediaRequestParameters);
		return WrappedObjectMapper.unmarshal(response.getContent(), AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}

	/**
	 * Updates multiseasons anime references minConcretizedEpisodeOnAnimedia,maxConcretizedEpisodeOnAnimedia,first episode,current max
	 *
	 * @param references the references
	 */
	public void updateReferences(@NotEmpty Set<AnimediaMALTitleReferences> references) {
		Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache = new HashMap<>();
		for (AnimediaMALTitleReferences reference : references) {
			if (isTitleUpdated(reference) || isTitleNotFoundOnMAL(reference)) {
				continue;
			}
			String url = animediaOnlineTv + reference.getUrl();
			Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = seasonsAndEpisodesCache.get(url);
			if (animeIdSeasonsAndEpisodesMap == null) {
				animeIdSeasonsAndEpisodesMap = getTitleHtmlAndPutInCache(url, seasonsAndEpisodesCache);
			}
			String animeId = Stream.of(animeIdSeasonsAndEpisodesMap).flatMap(x -> x.entrySet().stream()).findAny().map(Entry::getKey).orElse(null);
			Stream.of(animeIdSeasonsAndEpisodesMap).flatMap(map -> map.entrySet().stream())
					.flatMap(animeIdSeasonsAndEpisodesEntry -> animeIdSeasonsAndEpisodesEntry.getValue().entrySet().stream())
					.filter(seasonsAndEpisodesEntry -> seasonsAndEpisodesEntry.getKey().equals(reference.getDataList())).forEach(x -> {
				HttpResponse responseHtmlWithEpisodesInConcretizedDataList = httpCaller
						.call(animediaEpisodesList + animeId + "/" + reference.getDataList() + animediaEpisodesListPostfix,
								HttpMethod.GET,
								animediaRequestParameters);
				Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange(responseHtmlWithEpisodesInConcretizedDataList);
				Stream.of(episodesRange).flatMap(episodesRangeMap -> episodesRangeMap.entrySet().stream()).forEach(episodesRangeMapEntry -> {
					List<String> episodesList = episodesRangeMapEntry.getValue();
					String maxEpisodes = episodesRangeMapEntry.getKey();
					if (isTitleConcretizedAndOngoing(reference)) {
						enrichConcretizedAndOngoingReference(reference, episodesList);
					} else {
						enrichRegularReference(reference, maxEpisodes, episodesList);
					}
				});
			});
		}
	}

	private void enrichRegularReference(AnimediaMALTitleReferences reference, String maxEpisodes, List<String> episodesList) {
		Integer intMaxEpisodes = null;
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		if (!isMaxEpisodesUndefined(maxEpisodes)) {
			intMaxEpisodes = Integer.parseInt(maxEpisodes);
		}
		reference.setFirstEpisode(correctFirstEpisodeAndMin);
		reference.setMinConcretizedEpisodeOnAnimedia(correctFirstEpisodeAndMin);
		reference.setMaxConcretizedEpisodeOnAnimedia(getCorrectMaxConcretizedEpisodeOnAnimedia(intMaxEpisodes, correctFirstEpisodeAndMin, maxEpisodes));
		reference.setEpisodesRange(episodesList);
		reference.setCurrentMax(correctCurrentMax);
	}

	private void enrichConcretizedAndOngoingReference(AnimediaMALTitleReferences reference, List<String> episodesList) {
		String currentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		reference.setCurrentMax(currentMax);
	}

	private String getCorrectMaxConcretizedEpisodeOnAnimedia(Integer intMaxEpisodes, String firstEpisodeAndMin, String maxEpisodes) {
		int intFirstEpisodeAndMin = Integer.parseInt(firstEpisodeAndMin);
		return intMaxEpisodes != null && intMaxEpisodes < intFirstEpisodeAndMin ? Integer.toString(intFirstEpisodeAndMin + intMaxEpisodes) : maxEpisodes;
	}

	private Map<String, Map<String, String>> getTitleHtmlAndPutInCache(String url,
			Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache) {
		HttpResponse response = httpCaller.call(url, HttpMethod.GET, animediaRequestParameters);
		Map<String, Map<String, String>> seasonsAndEpisodes = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
		seasonsAndEpisodesCache.put(url, seasonsAndEpisodes);
		return seasonsAndEpisodes;
	}

	/**
	 * Compare multiseasons references and user watching titles and Creates container with matched anime Set poster url from MAL
	 *
	 * @param references     the  multiseasons references
	 * @param watchingTitles the user watching titles
	 * @return the matched user references
	 */
	public Set<AnimediaMALTitleReferences> getMatchedReferences(@NotEmpty Set<AnimediaMALTitleReferences> references,
			@NotEmpty Set<UserMALTitleInfo> watchingTitles) {
		Set<AnimediaMALTitleReferences> tempReferences = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			references.stream().filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle())).forEach(set -> {
				set.setPosterUrl(userMALTitleInfo.getPosterUrl());
				tempReferences.add(new AnimediaMALTitleReferences(set));
			});
		}
		return tempReferences;
	}

	/**
	 * Compare multi seasons titles from animedia search list with multi seasons references from resources
	 *
	 * @param multiSeasonsAnime multi seasons titles from animedia
	 * @param allReferences     all multi seasons references from resources
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
					set.setCurrentMax(getCorrectCurrentMax(currentlyUpdatedTitle.getCurrentMax()));
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
		Set<Anime> missingReferences = new LinkedHashSet<>();
		for (Anime anime : multi) {
			String fullUrl = anime.getFullUrl();
			if (!raw.containsKey(fullUrl)) {
				fullMatch = false;
				missingReferences.add(anime);
			}
		}
		if (!missingReferences.isEmpty()) {
			try {
				if (!RoutinesIO.isDirectoryExists(tempFolderName)) {
					RoutinesIO.mkDir(tempFolderName);
				}
				String prefix = tempFolderName + File.separator;
				RoutinesIO.marshalToFile(prefix + tempRawReferencesName, missingReferences);
				log.warn("NOT FOUND IN THE RAW REFERENCES {} PLEASE, ADD MISSING REFERENCE TO THE RESOURCES!", missingReferences.toString());
			} catch (NotDirectoryException e) {
				log.error("CHECK system.properties VARIABLE resources.tempfolder.name! {} IS NOT A DIRECTORY!", tempFolderName);
			}
		}
		return fullMatch;
	}
}
