package nasirov.yv.util;

import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.serialization.UserMALTitleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sun.research.ws.wadl.HTTPMethods.GET;
import static nasirov.yv.enums.Constants.ANIMEDIA_ANIME_EPISODES_LIST;
import static nasirov.yv.enums.Constants.ONLINE_ANIMEDIA_TV;

/**
 * Created by Хикка on 23.12.2018.
 */
@Component
public class SeasonAndEpisodeChecker {
	@Value("${cache.userMatchedAnime.name}")
	private String userMatchedAnimeCacheName;
	
	private HttpCaller httpCaller;
	
	private RequestParametersBuilder requestParametersBuilder;
	
	private AnimediaHTMLParser animediaHTMLParser;
	
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepositoryRepository;
	
	private CacheManager cacheManager;
	
	@Autowired
	public SeasonAndEpisodeChecker(HttpCaller httpCaller,
								   @Qualifier("animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
								   AnimediaHTMLParser animediaHTMLParser,
								   NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepositoryRepository,
								   CacheManager cacheManager) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaHTMLParser = animediaHTMLParser;
		this.notFoundAnimeOnAnimediaRepositoryRepository = notFoundAnimeOnAnimediaRepositoryRepository;
		this.cacheManager = cacheManager;
	}
	
	public Set<AnimediaMALTitleReferences> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles, Set<AnimediaMALTitleReferences> references, Set<AnimediaTitleSearchInfo> animediaSearchList, String username) {
		Map<String, Map<String, String>> animediaRequestParameters = requestParametersBuilder.build();
		System.out.println("MATCHED DATA:");
		Set<AnimediaMALTitleReferences> finalMatchedAnime = new LinkedHashSet<>();
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences = references.stream().filter(set -> set.getTitleOnMAL().equals(userMALTitleInfo.getTitle())).collect(Collectors.toSet());
			//инкремент на номер следующей непросмотренной серии
			Integer nextNumberOfEpisodeForWatch = userMALTitleInfo.getNumWatchedEpisodes() + 1;
			//Integer episodeNumberForWatch;
			//Map<String, String> nextEpisodeForWatchFinalUrl;
			if (matchedMultiSeasonsReferences.size() == 1) {
//				for (AnimediaMALTitleReferences animediaMALTitleReferences : matchedMultiSeasonsReferences) {
//					episodeNumberForWatch = Integer.parseInt(animediaMALTitleReferences.getFirstEpisode()) + userMALTitleInfo.getNumWatchedEpisodes();
//					nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, animediaMALTitleReferences, episodeNumberForWatch);
//					Stream.of(nextEpisodeForWatchFinalUrl)
//							.flatMap(map -> map.entrySet().stream())
//							.forEach(entry -> {
//								animediaMALTitleReferences.setNumberOfEpisodeForWatch(entry.getKey());
//								animediaMALTitleReferences.setFinalUrl(entry.getValue());
//							});
//					finalMatchedAnime.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
//				}
				handleOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, userMALTitleInfo, nextNumberOfEpisodeForWatch, finalMatchedAnime);
			} else if (matchedMultiSeasonsReferences.size() > 1) {
//				for (AnimediaMALTitleReferences animediaMALTitleReferences : matchedMultiSeasonsReferences) {
//					String maxEpisodesInDataListOnAnimedia = animediaMALTitleReferences.getMax();
//					Integer intMax = maxEpisodesInDataListOnAnimedia.equalsIgnoreCase("xx")
//							|| maxEpisodesInDataListOnAnimedia.equalsIgnoreCase("xxx")
//							? 0 : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
//					if (nextNumberOfEpisodeForWatch >= Integer.parseInt(animediaMALTitleReferences.getFirstEpisode())
//							&& (nextNumberOfEpisodeForWatch <= intMax
//							|| intMax.equals(0))) {
//						nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, animediaMALTitleReferences, nextNumberOfEpisodeForWatch);
//						Stream.of(nextEpisodeForWatchFinalUrl)
//								.flatMap(map -> map.entrySet().stream())
//								.forEach(entry -> {
//									animediaMALTitleReferences.setNumberOfEpisodeForWatch(entry.getKey());
//									animediaMALTitleReferences.setFinalUrl(entry.getValue());
//								});
//						finalMatchedAnime.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
//						break;
//					}
//				}
				handleMoreThanOneMatchedResultInMultiSeasonsReferences(matchedMultiSeasonsReferences, nextNumberOfEpisodeForWatch, finalMatchedAnime);
			} else {
				long matchedCountOfSingleSeasonAnime = animediaSearchList.stream().filter(list -> list.getKeywords().contains(userMALTitleInfo.getTitle())).count();
				if (matchedCountOfSingleSeasonAnime == 1) {
//					Optional<AnimediaTitleSearchInfo> first = animediaSearchList.stream().filter(list -> list.getKeywords().contains(userMALTitleInfo.getTitle())).findFirst();
//					AnimediaTitleSearchInfo matchedOfSingleSeasonAnime = first.orElse(null);
//					if (matchedOfSingleSeasonAnime != null) {
//						String url = matchedOfSingleSeasonAnime.getUrl();
//						HttpResponse response = httpCaller.call(ONLINE_ANIMEDIA_TV.getDescription() + url, GET, animediaRequestParameters);
//						Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
//						for (Map.Entry<String, Map<String, String>> animeIdDataListsAndEpisodes : animeIdSeasonsAndEpisodesMap.entrySet()) {
//							for (Map.Entry<String, String> dataListsAndEpisodes : animeIdDataListsAndEpisodes.getValue().entrySet()) {
//								String dataList = dataListsAndEpisodes.getKey();
//								HttpResponse resp = httpCaller.call(ANIMEDIA_ANIME_EPISODES_LIST.getDescription() + animeIdDataListsAndEpisodes.getKey() + "/" + dataList, GET, animediaRequestParameters);
//								Map<String, List<String>> maxEpisodesAndEpisodesRange = animediaHTMLParser.getEpisodesRange(resp);
//								for (Map.Entry<String, List<String>> maxEpisodesAndEpisodesRangeEntry : maxEpisodesAndEpisodesRange.entrySet()) {
//									List<String> episodesRangeFromMinToMax = maxEpisodesAndEpisodesRangeEntry.getValue();
//									//episodeNumberForWatch = Integer.parseInt(episodesRangeFromMinToMax.get(userMALTitleInfo.getNumWatchedEpisodes()));
//									nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, episodesRangeFromMinToMax, url, dataList, userMALTitleInfo);
//									Stream.of(nextEpisodeForWatchFinalUrl)
//											.flatMap(map -> map.entrySet().stream())
//											.forEach(entry -> finalMatchedAnime.add(new AnimediaMALTitleReferences(url,
//													dataList,
//													episodesRangeFromMinToMax.get(0),
//													userMALTitleInfo.getTitle(),
//													episodesRangeFromMinToMax.get(0),
//													maxEpisodesAndEpisodesRangeEntry.getKey(),
//													episodesRangeFromMinToMax.get(episodesRangeFromMinToMax.size() - 1),
//													matchedOfSingleSeasonAnime.getPosterUrl(),
//													entry.getValue(), entry.getKey())));
//								}
//							}
//						}
//					}
					handleOneMatchedResultInAnimediaSearchList(animediaSearchList, userMALTitleInfo, nextNumberOfEpisodeForWatch, animediaRequestParameters, finalMatchedAnime);
				} else if (matchedCountOfSingleSeasonAnime > 1) {
					handleMoreThanOneMatchedResultInAnimediaSearchList(animediaSearchList, userMALTitleInfo);
				} else {
					if (matchedCountOfSingleSeasonAnime == 0) {
//					System.out.println("Аниме " + userMALTitleInfo.getTitle() + " пока нет на анимедии");
//					if (!notFoundAnimeOnAnimediaRepositoryRepository.exitsByTitle(userMALTitleInfo.getTitle())) {
//						notFoundAnimeOnAnimediaRepositoryRepository.saveAndFlush(userMALTitleInfo);
//					}
						handleZeroMatchedResultInAnimediaSearchList(userMALTitleInfo);
					}
				}
			}
		}
		Cache userMatchedAnimeCache = cacheManager.getCache(userMatchedAnimeCacheName);
		userMatchedAnimeCache.putIfAbsent(username, finalMatchedAnime);
		return finalMatchedAnime;
	}
	
	private void handleOneMatchedResultInMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
																UserMALTitleInfo userMALTitleInfo,
																Integer nextNumberOfEpisodeForWatch,
																Set<AnimediaMALTitleReferences> finalMatchedAnime) {
		for (AnimediaMALTitleReferences animediaMALTitleReferences : matchedMultiSeasonsReferences) {
			Integer episodeNumberForWatch = Integer.parseInt(animediaMALTitleReferences.getFirstEpisode()) + userMALTitleInfo.getNumWatchedEpisodes();
			Map<String, String> nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, animediaMALTitleReferences, episodeNumberForWatch);
			Stream.of(nextEpisodeForWatchFinalUrl)
					.flatMap(map -> map.entrySet().stream())
					.forEach(entry -> {
						animediaMALTitleReferences.setNumberOfEpisodeForWatch(entry.getKey());
						animediaMALTitleReferences.setFinalUrl(entry.getValue());
					});
			finalMatchedAnime.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
		}
	}
	
	private void handleMoreThanOneMatchedResultInMultiSeasonsReferences(Set<AnimediaMALTitleReferences> matchedMultiSeasonsReferences,
																		Integer nextNumberOfEpisodeForWatch,
																		Set<AnimediaMALTitleReferences> finalMatchedAnime) {
		for (AnimediaMALTitleReferences animediaMALTitleReferences : matchedMultiSeasonsReferences) {
			String maxEpisodesInDataListOnAnimedia = animediaMALTitleReferences.getMax();
			Integer intMax = maxEpisodesInDataListOnAnimedia.equalsIgnoreCase("xx")
					|| maxEpisodesInDataListOnAnimedia.equalsIgnoreCase("xxx")
					? 0 : Integer.parseInt(maxEpisodesInDataListOnAnimedia);
			if (nextNumberOfEpisodeForWatch >= Integer.parseInt(animediaMALTitleReferences.getFirstEpisode())
					&& (nextNumberOfEpisodeForWatch <= intMax
					|| intMax.equals(0))) {
				Map<String, String> nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, animediaMALTitleReferences, nextNumberOfEpisodeForWatch);
				Stream.of(nextEpisodeForWatchFinalUrl)
						.flatMap(map -> map.entrySet().stream())
						.forEach(entry -> {
							animediaMALTitleReferences.setNumberOfEpisodeForWatch(entry.getKey());
							animediaMALTitleReferences.setFinalUrl(entry.getValue());
						});
				finalMatchedAnime.add(new AnimediaMALTitleReferences(animediaMALTitleReferences));
				break;
			}
		}
	}
	
	private void handleOneMatchedResultInAnimediaSearchList(Set<AnimediaTitleSearchInfo> animediaSearchList,
															UserMALTitleInfo userMALTitleInfo,
															Integer nextNumberOfEpisodeForWatch,
															Map<String, Map<String, String>> animediaRequestParameters,
															Set<AnimediaMALTitleReferences> finalMatchedAnime) {
		Optional<AnimediaTitleSearchInfo> first = animediaSearchList.stream().filter(list -> list.getKeywords().contains(userMALTitleInfo.getTitle())).findFirst();
		AnimediaTitleSearchInfo matchedOfSingleSeasonAnime = first.orElse(null);
		if (matchedOfSingleSeasonAnime != null) {
			String url = matchedOfSingleSeasonAnime.getUrl();
			HttpResponse response = httpCaller.call(ONLINE_ANIMEDIA_TV.getDescription() + url, GET, animediaRequestParameters);
			Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
			for (Map.Entry<String, Map<String, String>> animeIdDataListsAndEpisodes : animeIdSeasonsAndEpisodesMap.entrySet()) {
				for (Map.Entry<String, String> dataListsAndEpisodes : animeIdDataListsAndEpisodes.getValue().entrySet()) {
					String dataList = dataListsAndEpisodes.getKey();
					HttpResponse resp = httpCaller.call(ANIMEDIA_ANIME_EPISODES_LIST.getDescription() + animeIdDataListsAndEpisodes.getKey() + "/" + dataList, GET, animediaRequestParameters);
					Map<String, List<String>> maxEpisodesAndEpisodesRange = animediaHTMLParser.getEpisodesRange(resp);
					for (Map.Entry<String, List<String>> maxEpisodesAndEpisodesRangeEntry : maxEpisodesAndEpisodesRange.entrySet()) {
						List<String> episodesRangeFromMinToMax = maxEpisodesAndEpisodesRangeEntry.getValue();
						//episodeNumberForWatch = Integer.parseInt(episodesRangeFromMinToMax.get(userMALTitleInfo.getNumWatchedEpisodes()));
						Map<String, String> nextEpisodeForWatchFinalUrl = printResult(nextNumberOfEpisodeForWatch, episodesRangeFromMinToMax, url, dataList, userMALTitleInfo);
						Stream.of(nextEpisodeForWatchFinalUrl)
								.flatMap(map -> map.entrySet().stream())
								.forEach(entry -> finalMatchedAnime.add(new AnimediaMALTitleReferences(url,
										dataList,
										episodesRangeFromMinToMax.get(0),
										userMALTitleInfo.getTitle(),
										episodesRangeFromMinToMax.get(0),
										maxEpisodesAndEpisodesRangeEntry.getKey(),
										episodesRangeFromMinToMax.get(episodesRangeFromMinToMax.size() - 1),
										matchedOfSingleSeasonAnime.getPosterUrl(),
										entry.getValue(), entry.getKey())));
					}
				}
			}
		}
	}
	
	private void handleZeroMatchedResultInAnimediaSearchList(UserMALTitleInfo userMALTitleInfo) {
		System.out.println("Аниме " + userMALTitleInfo.getTitle() + " пока нет на анимедии");
		if (!notFoundAnimeOnAnimediaRepositoryRepository.exitsByTitle(userMALTitleInfo.getTitle())) {
			notFoundAnimeOnAnimediaRepositoryRepository.saveAndFlush(userMALTitleInfo);
		}
	}
	
	private void handleMoreThanOneMatchedResultInAnimediaSearchList(Set<AnimediaTitleSearchInfo> animediaSearchList,
																	UserMALTitleInfo userMALTitleInfo) {
		Set<AnimediaTitleSearchInfo> matchedResult = animediaSearchList.stream().filter(list -> list.getKeywords().contains(userMALTitleInfo.getTitle())).collect(Collectors.toSet());
		System.out.println("Найдено несколько совпадений для " + userMALTitleInfo.getTitle() + "\nСовпадения:\n" + matchedResult);
	}
	
	/**
	 * Update matched reference (NumberOfEpisodeForWatch, FinalUrl)
	 *
	 * @param watchingTitles        user watching titles
	 * @param references            currently updated title on animedia
	 * @param matchedAnimeFromCache matched user anime from cache
	 */
	public void updateMatchedReferences(Set<UserMALTitleInfo> watchingTitles, AnimediaMALTitleReferences references, Set<AnimediaMALTitleReferences> matchedAnimeFromCache) {
		System.out.println("MATCHED DATA:");
		System.out.println("updateMatchedReferences:");
		for (UserMALTitleInfo userMALTitleInfo : watchingTitles) {
			Integer numWatchedEpisodesOnMAL = userMALTitleInfo.getNumWatchedEpisodes() + 1;
			Integer episodeNumberForWatch;
			Map<String, String> nextEpisodeForWatchFinalUrl;
			AnimediaMALTitleReferences matchedReferences = null;
			if (references.getTitleOnMAL().equals(userMALTitleInfo.getTitle())) {
				matchedReferences = matchedAnimeFromCache.stream()
						.filter(set -> set.getTitleOnMAL().equals(references.getTitleOnMAL()) && set.getDataList().equals(references.getDataList())).findFirst()
						.orElse(null);
			}
			//инкремент на номер следующей непросмотренной серии
			if (matchedReferences != null) {
				episodeNumberForWatch = Integer.parseInt(matchedReferences.getFirstEpisode()) + userMALTitleInfo.getNumWatchedEpisodes();
				nextEpisodeForWatchFinalUrl = printResult(numWatchedEpisodesOnMAL, matchedReferences, episodeNumberForWatch);
				for (Map.Entry<String, String> currentEpisodeFinalUrl : nextEpisodeForWatchFinalUrl.entrySet()) {
					matchedReferences.setNumberOfEpisodeForWatch(currentEpisodeFinalUrl.getKey());
					matchedReferences.setFinalUrl(currentEpisodeFinalUrl.getValue());
				}
			}
		}
	}
	
	private Map<String, String> printResult(Integer numWatchedEpisodesOnMAL, AnimediaMALTitleReferences animediaMALTitleReferences, Integer episodeNumberForWatch) {
		String finalUrl;
		Map<String, String> nextEpisodeForWatchFinalUrl = new HashMap<>();
		// TODO: 28.01.2019 dfd
		if (
//				(numWatchedEpisodesOnMAL <= Integer.parseInt(animediaMALTitleReferences.getCurrentMax())
//				&& numWatchedEpisodesOnMAL >= Integer.parseInt(animediaMALTitleReferences.getFirstEpisode()))
//				||
				episodeNumberForWatch <= Integer.parseInt(animediaMALTitleReferences.getCurrentMax())
				) {
			finalUrl = ONLINE_ANIMEDIA_TV.getDescription() + animediaMALTitleReferences.getUrl() + "/" + animediaMALTitleReferences.getDataList() + "/" + episodeNumberForWatch;
			System.out.println("Найдены новые серии для мультисезонного аниме " + finalUrl);
			nextEpisodeForWatchFinalUrl.put(episodeNumberForWatch.toString(), finalUrl);
		} else {
			finalUrl = ONLINE_ANIMEDIA_TV.getDescription() + animediaMALTitleReferences.getUrl() + "/" + animediaMALTitleReferences.getDataList();
			System.out.println("Нет новых серий для мультисезонного аниме " + finalUrl);
			nextEpisodeForWatchFinalUrl.put("", "");
		}
		return nextEpisodeForWatchFinalUrl;
	}
	
	private Map<String, String> printResult(Integer numWatchedEpisodesOnMAL, List<String> episodesRangeFromMinToMax, String url, String dataList, UserMALTitleInfo userMALTitleInfo) {
		String finalUrl;
		Map<String, String> nextEpisodeForWatchFinalUrl = new HashMap<>();
		if (numWatchedEpisodesOnMAL <= Integer.parseInt(episodesRangeFromMinToMax.get(episodesRangeFromMinToMax.size() - 1))) {
			Integer episodeNumberForWatch = Integer.parseInt(episodesRangeFromMinToMax.get(userMALTitleInfo.getNumWatchedEpisodes()));
			finalUrl = ONLINE_ANIMEDIA_TV.getDescription() + url + "/" + dataList + "/" + episodeNumberForWatch;
			System.out.println("Найдены новые серии для односезонного аниме " + finalUrl);
			nextEpisodeForWatchFinalUrl.put(episodeNumberForWatch.toString(), finalUrl);
		} else {
			finalUrl = ONLINE_ANIMEDIA_TV.getDescription() + url + "/" + dataList;
			System.out.println("Нет новых серий для односезонного аниме " + finalUrl);
			nextEpisodeForWatchFinalUrl.put("", "");
		}
		return nextEpisodeForWatchFinalUrl;
	}
	
	public void differences(Set<AnimediaMALTitleReferences> references, Set<AnimediaMALTitleReferences> matchedAnime) {
		for (AnimediaMALTitleReferences watchedTitlesInfo : references) {
			if (watchedTitlesInfo.getTitleOnMAL().equalsIgnoreCase("none")) {
				continue;
			}
			long separatedSeason = matchedAnime.stream().filter(set -> set.getTitleOnMAL().equals(watchedTitlesInfo.getTitleOnMAL())).count();
			long matchedTitleAndDatalist = matchedAnime.stream().filter(set -> set.getTitleOnMAL().equals(watchedTitlesInfo.getTitleOnMAL())
					&& set.getDataList().equals(watchedTitlesInfo.getDataList())).count();
			if (matchedTitleAndDatalist == 0 && separatedSeason == 0) {
				System.out.println("В matchedAnime нет " + watchedTitlesInfo.toString());
			} else if (matchedTitleAndDatalist > 1) {
				System.out.println("В matchedAnime " + matchedTitleAndDatalist + " совпадений " + watchedTitlesInfo.toString());
			}
		}
	}
}
