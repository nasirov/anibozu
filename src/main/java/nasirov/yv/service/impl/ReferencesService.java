package nasirov.yv.service.impl;

import static java.util.Optional.ofNullable;
import static nasirov.yv.util.AnimediaUtils.getAnimeId;
import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getDataListsAndMaxEpisodesMap;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isMaxEpisodeUndefined;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedAndOngoing;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;
import static nasirov.yv.util.AnimediaUtils.isTitleUpdated;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.http.feign.AnimediaFeignClient;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.service.ReferencesServiceI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReferencesService implements ReferencesServiceI {

	private final AnimediaFeignClient animediaFeignClient;

	private final GitHubFeignClient gitHubFeignClient;

	private final AnimediaHTMLParser animediaHTMLParser;

	/**
	 * Creates container with the anime references
	 *
	 * @return the references
	 */
	@Override
	public Set<AnimediaMALTitleReferences> getMultiSeasonsReferences() {
		ResponseEntity<Set<AnimediaMALTitleReferences>> response = gitHubFeignClient.getReferences();
		return ofNullable(response.getBody()).orElseGet(Collections::emptySet);
	}

	/**
	 * Updates multiseasons anime references minConcretizedEpisodeOnAnimedia,maxConcretizedEpisodeOnAnimedia,first episode,current max
	 *
	 * @param references references for update
	 */
	@Override
	public void updateReferences(Set<AnimediaMALTitleReferences> references) {
		Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache = new HashMap<>();
		references.stream()
				.filter(this::isReferenceNeedUpdate)
				.forEach(x -> handleReference(x, seasonsAndEpisodesCache));
	}

	private boolean isReferenceNeedUpdate(AnimediaMALTitleReferences reference) {
		return !(isTitleUpdated(reference) || isTitleNotFoundOnMAL(reference));
	}

	private void handleReference(AnimediaMALTitleReferences reference, Map<String, Map<String, Map<String, String>>> seasonsAndEpisodesCache) {
		String url = reference.getUrl();
		Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap =
				ofNullable(seasonsAndEpisodesCache.get(url)).orElseGet(() -> getTitleHtmlAndPutInCache(
				url,
				seasonsAndEpisodesCache));
		String animeId = getAnimeId(animeIdDataListsAndMaxEpisodesMap);
		Map<String, String> dataListsAndMaxEpisodesMap = getDataListsAndMaxEpisodesMap(animeIdDataListsAndMaxEpisodesMap);
		Stream.of(dataListsAndMaxEpisodesMap)
				.flatMap(map -> map.entrySet()
						.stream())
				.filter(dataListsAndMaxEpisodesMapEntry -> dataListsAndMaxEpisodesMapEntry.getKey()
						.equals(reference.getDataList()))
				.forEach(x -> handleDataList(animeId, reference));
	}

	private void handleDataList(String animeId, AnimediaMALTitleReferences reference) {
		ResponseEntity<String> responseHtmlWithEpisodesInConcretizedDataList = animediaFeignClient.getDataListWithEpisodes(animeId,
				reference.getDataList());
		Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange(responseHtmlWithEpisodesInConcretizedDataList.getBody());
		Stream.of(episodesRange)
				.flatMap(episodesRangeMap -> episodesRangeMap.entrySet()
						.stream())
				.forEach(x -> enrichReference(x, reference));
	}

	private void enrichReference(Entry<String, List<String>> maxEpisodesAndEpisodesRangeEntry, AnimediaMALTitleReferences reference) {
		String maxEpisodes = maxEpisodesAndEpisodesRangeEntry.getKey();
		List<String> episodesList = maxEpisodesAndEpisodesRangeEntry.getValue();
		if (isTitleConcretizedAndOngoing(reference)) {
			enrichConcretizedAndOngoingReference(reference, episodesList);
		} else {
			enrichRegularReference(reference, maxEpisodes, episodesList);
		}
	}

	/**
	 * Compare multiseasons references and user watching titles and Creates container with matched anime Set poster url from MAL
	 *
	 * @param references     the  multiseasons references
	 * @param watchingTitles the user watching titles
	 * @return the matched user references
	 */
	@Override
	public Set<AnimediaMALTitleReferences> getMatchedReferences(Set<AnimediaMALTitleReferences> references, Set<UserMALTitleInfo> watchingTitles) {
		return watchingTitles.stream()
				.map(x -> findMatchedReference(x, references))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private AnimediaMALTitleReferences findMatchedReference(UserMALTitleInfo userMALTitleInfo, Set<AnimediaMALTitleReferences> references) {
		return references.stream()
				.filter(x -> x.getTitleOnMAL()
						.equals(userMALTitleInfo.getTitle()))
				.map(x -> setPosterUrlFromMAL(userMALTitleInfo, x))
				.findFirst()
				.orElse(null);
	}

	private AnimediaMALTitleReferences setPosterUrlFromMAL(UserMALTitleInfo userMALTitleInfo, AnimediaMALTitleReferences reference) {
		reference.setPosterUrl(userMALTitleInfo.getPosterUrl());
		return reference;
	}

	/**
	 * Updates currentMax and episodes range for matched reference and set titleOnMal for currentlyUpdatedTitle
	 *
	 * @param matchedAnimeFromCache the matched user anime from cache
	 * @param currentlyUpdatedTitle the currently updated title on animedia
	 */
	@Override
	public void updateCurrentMaxAndEpisodesRange(Set<AnimediaMALTitleReferences> matchedAnimeFromCache,
			AnimediaMALTitleReferences currentlyUpdatedTitle) {
		matchedAnimeFromCache.stream()
				.filter(set -> set.getUrl()
						.equals(currentlyUpdatedTitle.getUrl()) && set.getDataList()
						.equals(currentlyUpdatedTitle.getDataList()))
				.forEach(ref -> {
					ref.setCurrentMax(getCorrectCurrentMax(currentlyUpdatedTitle.getCurrentMax()));
					ref.getEpisodesRange()
							.add(currentlyUpdatedTitle.getCurrentMax());
					currentlyUpdatedTitle.setTitleOnMAL(ref.getTitleOnMAL());
				});
	}

	private void enrichRegularReference(AnimediaMALTitleReferences reference, String maxEpisodes, List<String> episodesList) {
		Integer intMaxEpisodes = null;
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		if (!isMaxEpisodeUndefined(maxEpisodes)) {
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
		ResponseEntity<String> response = animediaFeignClient.getAnimePageWithDataLists(url);
		Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(response.getBody());
		seasonsAndEpisodesCache.put(url, animeIdDataListsAndMaxEpisodesMap);
		return animeIdDataListsAndMaxEpisodesMap;
	}

}
