package nasirov.yv.service;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.enums.AnimeTypeOnAnimedia;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.AnimediaTitlesSearchParser;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.Anime;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.sun.research.ws.wadl.HTTPMethods.GET;

/**
 * Created by Хикка on 21.01.2019.
 */
@Service
@Slf4j
public class AnimediaService {
	private static final String ANNOUNCEMENT = "<a href=\"/announcements\" title=\"Аниме онлайн смотреть\">Анонсы</a>";
	
	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;
	
	@Value("${cache.currentlyUpdatedTitles.name}")
	private String currentlyUpdatedTitlesCacheName;
	
	@Value("${cache.sortedAnimediaSearchList.name}")
	private String sortedAnimediaSearchListCacheName;
	
	@Value("${urls.online.animedia.tv}")
	private String animediaOnlineTv;
	
	@Value("${urls.online.animedia.anime.list}")
	private String animediaAnimeList;
	
	@Value("${urls.online.animedia.anime.episodes.list}")
	private String animediaEpisodesList;
	
	@Value("classpath:${resources.multiSeasonsAnimeUrls.name}")
	private Resource resourceMultiSeasonsAnimeUrls;
	
	@Value("classpath:${resources.singleSeasonsAnimeUrls.name}")
	private Resource resourceSingleSeasonsAnimeUrls;
	
	@Value("classpath:${resources.announcements.name}")
	private Resource resourceAnnouncementsUrls;
	
	private HttpCaller httpCaller;
	
	private RequestParametersBuilder requestParametersBuilder;
	
	private AnimediaTitlesSearchParser animediaTitlesSearchParser;
	
	private AnimediaHTMLParser animediaHTMLParser;
	
	private URLBuilder urlBuilder;
	
	private RoutinesIO routinesIO;
	
	private CacheManager cacheManager;
	
	@Autowired
	public AnimediaService(HttpCaller httpCaller,
						   @Qualifier(value = "animediaRequestParametersBuilder") RequestParametersBuilder requestParametersBuilder,
						   AnimediaTitlesSearchParser animediaTitlesSearchParser,
						   AnimediaHTMLParser animediaHTMLParser,
						   URLBuilder urlBuilder,
						   RoutinesIO routinesIO,
						   CacheManager cacheManager) {
		this.httpCaller = httpCaller;
		this.requestParametersBuilder = requestParametersBuilder;
		this.animediaTitlesSearchParser = animediaTitlesSearchParser;
		this.animediaHTMLParser = animediaHTMLParser;
		this.urlBuilder = urlBuilder;
		this.routinesIO = routinesIO;
		this.cacheManager = cacheManager;
	}
	
	/**
	 * Search for animedia search list
	 *
	 * @return list of title search info on animedia
	 */
	@Cacheable(value = "animediaSearchListCache")
	public Set<AnimediaTitleSearchInfo> getAnimediaSearchList() {
		HttpResponse animediaResponse = httpCaller.call(animediaAnimeList, GET, requestParametersBuilder.build());
		//инфа для поиска на animedia
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaTitlesSearchParser.parse(animediaResponse, LinkedHashSet.class);
		animediaSearchList.forEach(set -> {
			set.setUrl(set.getUrl().replaceAll("http://online\\.animedia\\.tv/", "")
					.replace("[", "%5B").replace("]", "%5D"));
			set.setPosterUrl("http:" + set.getPosterUrl().replace("h=70&q=50", "h=350&q=100"));
		});
		return animediaSearchList;
	}
	
	/**
	 * Search for currently updated titles on animedia
	 *
	 * @return list of currently updated titles
	 */
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles() {
		Cache currentlyUpdatedTitlesCache = cacheManager.getCache(currentlyUpdatedTitlesCacheName);
		HttpResponse animediaResponse = httpCaller.call(animediaOnlineTv, GET, requestParametersBuilder.build());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaHTMLParser.getCurrentlyUpdatedTitlesList(animediaResponse);
		currentlyUpdatedTitlesCache.putIfAbsent(currentlyUpdatedTitlesCacheName, currentlyUpdatedTitles);
		return currentlyUpdatedTitles;
	}
	
	/**
	 * Sort anime search info ofr single season,multi season, announcements
	 *
	 * @param animediaSearchListInput anime info for search on animedia
	 * @return list[0] - singleSeason anime, list[1] - multiSeason anime,list[2] - announcements
	 */
	public List<Set<Anime>> getSortedForSeasonAnime(@NotNull Set<AnimediaTitleSearchInfo> animediaSearchListInput) {
		Map<String, Map<String, String>> animediaRequestParameters = requestParametersBuilder.build();
		//печатаем все урлы анимедии с 1 серией для каждого дата листа
		int multiSeasonCount = 1;
		int singleSeasonCount = 1;
		int announcementCount = 1;
		Set<Anime> multi = new LinkedHashSet<>();
		Set<Anime> single = new LinkedHashSet<>();
		Set<Anime> announcement = new LinkedHashSet<>();
		List<Set<Anime>> allSeasons = new ArrayList<>();
		for (AnimediaTitleSearchInfo animediaSearchList : animediaSearchListInput) {
			String rootUrl = animediaSearchList.getUrl();
			String url = animediaOnlineTv + rootUrl;
			//получаем html с аниме
			HttpResponse response = httpCaller.call(url
					, GET, animediaRequestParameters);
			String content = response.getContent();
			//анонс просто дабавляем в сет
			if (!content.contains(ANNOUNCEMENT)) {
				//парсим html и получаем аниме id, дата сеты и эпизоды на них
				Map<String, Map<String, String>> seasonsAndEpisodes = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(response);
				if (seasonsAndEpisodes != null) {
					for (Map.Entry<String, Map<String, String>> entry : seasonsAndEpisodes.entrySet()) {
						int dataListCount = 1;
						for (Map.Entry<String, String> seasons : entry.getValue().entrySet()) {
							String targetUrl;
							//если несколько дата сетов, то это многосезонное аниме и по каждому дата сету - вкладке нужно сделать дополнительный запрос
							//дата сет - вкладка не обязательно отдельный сезон
							//иногда один сезон размазывают по нескольким дата сетам - вкладкам
							if (entry.getValue().size() > 1) {
								HttpResponse resp = httpCaller.call(animediaEpisodesList + entry.getKey() + "/" + seasons.getKey(), GET, animediaRequestParameters);
								String count = String.valueOf(multiSeasonCount) + "." + dataListCount;
								targetUrl = urlBuilder.build(url, seasons.getKey(), animediaHTMLParser.getFirstEpisodeInSeason(resp), null);
								multi.add(new Anime(count, targetUrl, rootUrl));
								dataListCount++;
							} else {
								//если дата сет один-это односезонное аниме
								targetUrl = urlBuilder.build(url, seasons.getKey(), null, seasons.getValue());
								single.add(new Anime(String.valueOf(singleSeasonCount), targetUrl, rootUrl));
								singleSeasonCount++;
							}
						}
						if (entry.getValue().size() > 1) {
							multiSeasonCount++;
						}
					}
				}
			} else {
				announcement.add(new Anime(String.valueOf(announcementCount), url, rootUrl));
				announcementCount++;
			}
		}
		allSeasons.add(single);
		allSeasons.add(multi);
		allSeasons.add(announcement);
		// routinesIO.marshalToResources(resourceSingleSeasonsAnimeUrls, single);
		//  routinesIO.marshalToResources(resourceMultiSeasonsAnimeUrls, multi);
		//  routinesIO.marshalToResources(resourceAnnouncementsUrls, announcement);
		Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
		sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), single);
		sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.MULTISEASONS.getDescription(), multi);
		sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), announcement);
		return allSeasons;
	}
	
	public List<Set<Anime>> getAnime(@NotNull Set<AnimediaTitleSearchInfo> animediaSearchList) {
		Set<Anime> singleSeasonAnime;
		Set<Anime> multiSeasonsAnime;
		Set<Anime> announcements;
		List<Set<Anime>> allSeasons = new ArrayList<>();
		if (resourceAnnouncementsUrls.exists() && resourceMultiSeasonsAnimeUrls.exists() && resourceSingleSeasonsAnimeUrls.exists()) {
			log.debug("Loading sorted anime from resources...");
			singleSeasonAnime = routinesIO.unmarshalFromResource(resourceSingleSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
			multiSeasonsAnime = routinesIO.unmarshalFromResource(resourceMultiSeasonsAnimeUrls, Anime.class, LinkedHashSet.class);
			announcements = routinesIO.unmarshalFromResource(resourceAnnouncementsUrls, Anime.class, LinkedHashSet.class);
			allSeasons.add(singleSeasonAnime);
			allSeasons.add(multiSeasonsAnime);
			allSeasons.add(announcements);
			Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
			sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), singleSeasonAnime);
			sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.MULTISEASONS.getDescription(), multiSeasonsAnime);
			sortedAnimediaSearchListCache.put(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), announcements);
		} else {
			log.debug("Sorted anime not found in resources. Creating...");
			allSeasons = getSortedForSeasonAnime(animediaSearchList);
		}
		return allSeasons;
	}
	
	/**
	 * Search for new titles from animedia search list in containers from resources
	 *
	 * @param singleSeasonAnime  single season anime from resources
	 * @param multiSeasonsAnime  multi seasons anime from resources
	 * @param announcements      announcements anime from resources
	 * @param animediaSearchList animedia search list
	 * @return set of not found titles from animedia search list
	 */
	public Set<AnimediaTitleSearchInfo> checkAnime(@NotNull Set<Anime> singleSeasonAnime,
												   @NotNull Set<Anime> multiSeasonsAnime,
												   @NotNull Set<Anime> announcements,
												   @NotNull Set<AnimediaTitleSearchInfo> animediaSearchList) {
		Set<AnimediaTitleSearchInfo> notFound = new LinkedHashSet<>();
		for (AnimediaTitleSearchInfo animediaTitleSearchInfo : animediaSearchList) {
			long singleCount = singleSeasonAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long multiCount = multiSeasonsAnime.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			long announcementCount = announcements.stream().filter(set -> set.getRootUrl().equals(animediaTitleSearchInfo.getUrl())).count();
			if (singleCount == 0 && multiCount == 0 && announcementCount == 0) {
				log.error("Not found in any sorted anime lists\n{}", animediaTitleSearchInfo);
				notFound.add(animediaTitleSearchInfo);
			}
		}
		return notFound;
	}
	
	/**
	 * Compare cached currently updated titles and fresh
	 * refresh cache with difference
	 *
	 * @param fresh     fresh updated titles
	 * @param fromCache updated titles from cache
	 * @return list of differences between fresh and cached
	 */
	public List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles(@NotNull List<AnimediaMALTitleReferences> fresh, @NotNull List<AnimediaMALTitleReferences> fromCache) {
		List<AnimediaMALTitleReferences> list = new ArrayList<>();
		if (fresh != null && fromCache != null) {
			AnimediaMALTitleReferences animediaMALTitleReferencesFromCache = fromCache.get(0);
			if (fresh.size() != fromCache.size()
					|| !fresh.get(0).equals(animediaMALTitleReferencesFromCache)) {
				for (int i = 0; i < fresh.size(); i++) {
					AnimediaMALTitleReferences temp = fresh.get(i);
					if (temp.equals(animediaMALTitleReferencesFromCache)) {
						break;
					}
					list.add(temp);
				}
				cacheManager.getCache(currentlyUpdatedTitlesCacheName).put(currentlyUpdatedTitlesCacheName, list);
			}
		}
		return list;
	}
}
