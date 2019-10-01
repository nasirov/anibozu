package nasirov.yv.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.exception.animedia.EpisodesRangeNotFoundException;
import nasirov.yv.exception.animedia.FirstEpisodeInSeasonNotFoundException;
import nasirov.yv.exception.animedia.NewEpisodesListNotFoundException;
import nasirov.yv.exception.animedia.OriginalTitleNotFoundException;
import nasirov.yv.exception.animedia.SeasonsAndEpisodesNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Html parser
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class AnimediaHTMLParser {

	private static final int NUMBER_OF_CURRENTLY_UPDATED_TITLES = 10;

	/**
	 * For tab id and episodes number
	 */
	private static final String TAB_AND_EPISODES =
			"\\s*<div id=\"tab(?<numberOfTab>\\d{1,3})\" role=\"tabpanel\" class=\"media__tabs__panel tab-pane\\s?(active)?\">\\R"
					+ "\\s*<div id=\"carousel\\d{1,3}\" data-interval=\"false\" data-wrap=\"false\" class=\"media__tabs__series carousel slide\">\\R"
					+ "\\s*<div class=\"media__tabs__series__list carousel-inner\" data-list_id=\"(?<dataList>\\d{1,3})\"></div>\\R"
					+ "\\s*<div class=\"clearfix\"></div>\\R\\s*<div class=\"media__tabs__series__footer\">\\R"
					+ "\\s*<div class=\"media__tabs__series__footer__item\"><a "
					+ "href=\"#carousel\\d{1,3}\" data-slide=\"prev\" class=\"media__tabs__series__control "
					+ "carousel-control prev\"><i class=\"ai ai-prev\"></i></a></div>\\R\\s*<div class=\"media__tabs__series__footer__item "
					+ "media__tabs__series__footer__item__center\">Серии <span class=\"start-series\"></span>-<span class=\"end-series\"></span> из "
					+ "(?<numberOfEpisodes>(\\d{1,4}|[xXхХ]{1,3}))(.+)?</div>";

	/**
	 * For tab id and season
	 */
	private static final String TAB_AND_SEASONS =
			"<li class=\"media__tabs__nav__item\\s?(active)?\"><a href=\"#tab(?<numberOfTab>\\d{1,3})\" role=\"tab\" data-toggle=\"tab\">(?<season>"
					+ ".+)</a></li>";

	/**
	 * For anime id
	 */
	private static final String DATA_ENTRY_ID = "<ul role=\"tablist\" class=\"media__tabs__nav nav-tabs\" data-entry_id=\"(?<animeId>\\d+)\".*?";

	/**
	 * For first episode in data list
	 */
	private static final String EPISODE_IN_DATA_LIST = "<span>(?<description>Серия\\.|Cерия|Серия|Серии|серия|серии|ОВА|OVA|ONA|ODA"
			+ "|ФИЛЬМ|Фильмы|Сп[е|э]шл|СПЕШЛ|Фильм|Трейлер)?\\s?(?<firstEpisodeInSeason>\\d{1,3})?"
			+ "(-\\d{1,3})?(\\s\\(\\d{1,3}\\))?\\s*?(из)?\\s?(?<maxEpisodes>.{1,3})?</span>";

	/**
	 * For episodes range except trailers in data list
	 */
	private static final String EPISODE_IN_DATA_LIST_EXCEPT_TRAILERS = "<span>\\s?(?<description>Серия\\.|Cерия|Серия|Серии|серия|серии|ОВА|OVA|ONA|ODA"
			+ "|ФИЛЬМ|Фильмы|Сп[е|э]шл|СПЕШЛ|Фильм)?\\s?(?<firstEpisodeInSeason>\\d{1,3})?"
			+ "(-(?<joinedEpisode>\\d{1,3}))?(\\s\\(\\d{1,3}\\))?\\s*?(из)?\\s?(?<maxEpisodes>.{1,3})?</span>";

	/**
	 * For trailer in data list
	 */
	private static final String TRAILER_IN_DATA_LIST = "<span>\\s?(?<description>Трейлер)?\\s?(из)?\\s?(?<maxEpisodes>.{1,3})?</span>";

	/**
	 * For original title in html page
	 */
	private static final String ORIGINAL_TITLE = "<div class=\"media__post__original-title\">(?<originalTitle>[^а-яА-Я\\n]*).+?</div>";

	/**
	 * For currently updated titles
	 */
	private static final String NEW_SERIES_INFO =
			"\\s*<div class=\"widget__new-series__item widget__item\">\\R*\\s*<a href=\"(?<fullUrl>(?<root>/anime/.*?)(?<dataListAndCurrentMax>/(?<dataList>\\d{1,3})/"
					+ "(?<currentMax>\\d{1,3}))?)\" title=\".+\" class=\"widget__new-series__item__thumb\"><img data-src=\".+\" alt=\".+\" title=\""
					+ ".+\"></a>\\R*\\s*<div class=\"widget__new-series__item__info\">\\R*"
					+ "\\s*<a href=\".+\" title=\".+\" class=\"h4 widget__new-series__item__title\">.+</a>";

	/**
	 * For url in data list
	 */
	private static final String URL_IN_DATA_LIST = "href=\"(?<url>/anime/.*?/\\d{1,3}/\\d{1,3})\"";

	private static final String DATALIST_EPISODES_RANGE = "(\\d{1,3}-(\\d{1,3}|[xXхХ]{1,3}))";

	private static final Pattern NEW_SERIES_INFO_PATTERN = Pattern.compile(NEW_SERIES_INFO);

	private static final Pattern ORIGINAL_TITLE_PATTERN = Pattern.compile(ORIGINAL_TITLE);

	private static final Pattern EPISODE_IN_DATA_LIST_PATTERN = Pattern.compile(EPISODE_IN_DATA_LIST);

	private static final Pattern EPISODE_IN_DATA_LIST_EXCEPT_TRAILERS_PATTERN = Pattern.compile(EPISODE_IN_DATA_LIST_EXCEPT_TRAILERS);

	private static final Pattern TRAILER_IN_DATA_LIST_PATTERN = Pattern.compile(TRAILER_IN_DATA_LIST);

	private static final Pattern URL_IN_DATA_LIST_PATTERN = Pattern.compile(URL_IN_DATA_LIST);

	private static final Pattern TAB_AND_SEASONS_PATTERN = Pattern.compile(TAB_AND_SEASONS);

	private static final Pattern TAB_AND_EPISODES_PATTERN = Pattern.compile(TAB_AND_EPISODES);

	private static final Pattern DATA_ENTRY_ID_PATTERN = Pattern.compile(DATA_ENTRY_ID);

	private static final Pattern DATALIST_EPISODES_RANGE_PATTERN = Pattern.compile(DATALIST_EPISODES_RANGE);

	/**
	 * Searches for the title info
	 * anime id - an entry id in the animedia database
	 * data list - sub entry with anime episodes(it may be full season,part of season,ova,etc.)
	 * each tab on page contain one data list
	 * tab - view, data list - model(in terms mvc)
	 *
	 * @param response the animedia response
	 * @return the map <anime id,map<data list, number of episodes>
	 */
	public Map<String, Map<String, String>> getAnimeIdDataListsAndMaxEpisodesMap(HttpResponse response) {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodes = new HashMap<>();
		try {
			animeIdSeasonsAndEpisodes = searchForDataListsAndMaxEpisodes(response.getContent());
		} catch (SeasonsAndEpisodesNotFoundException | OriginalTitleNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return animeIdSeasonsAndEpisodes;
	}

	/**
	 * Searches for the first episode in the data list
	 * request example http://online.animedia.tv/ajax/episodes/9480/3
	 * 9480 - anime id
	 * 3 - data list
	 *
	 * @param response the animedia response
	 * @return the first episode
	 */
	public String getFirstEpisodeInSeason(HttpResponse response) {
		String firstEpisodeNumber = null;
		try {
			firstEpisodeNumber = searchForFirstEpisodeInSeason(response.getContent());
		} catch (FirstEpisodeInSeasonNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return firstEpisodeNumber;
	}

	/**
	 * Searches for the episodes range in a data list
	 * request example http://online.animedia.tv/ajax/episodes/9480/3
	 * 9480 - anime id
	 * 3 - data list
	 *
	 * @param response the animedia response
	 * @return the map<max episode, list<episodes range>>
	 */
	public Map<String, List<String>> getEpisodesRange(HttpResponse response) {
		Map<String, List<String>> episodesRange = new HashMap<>();
		try {
			episodesRange = searchForEpisodesRange(response.getContent());
		} catch (EpisodesRangeNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return episodesRange;
	}

	/**
	 * Searches for a title name
	 *
	 * @param response the animedia response
	 * @return the title name
	 */
	public String getOriginalTitle(HttpResponse response) {
		String originalTitle = null;
		try {
			originalTitle = searchForOriginalTitle(response.getContent());
		} catch (OriginalTitleNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return originalTitle;
	}

	/**
	 * Searches for the currently updated titles on animedia
	 *
	 * @param response the animedia response
	 * @return list of the currently updated titles
	 */
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitlesList(HttpResponse response) {
		List<AnimediaMALTitleReferences> newSeriesList = new ArrayList<>();
		try {
			newSeriesList = searchForCurrentlyUpdatedTitles(response.getContent());
		} catch (NewEpisodesListNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return newSeriesList;
	}

	private List<AnimediaMALTitleReferences> searchForCurrentlyUpdatedTitles(String content) throws NewEpisodesListNotFoundException {
		Matcher matcher = NEW_SERIES_INFO_PATTERN.matcher(content);
		List<AnimediaMALTitleReferences> newSeriesList = new ArrayList<>();
		while (matcher.find()) {
			String dataListAndCurrentMax = matcher.group("dataListAndCurrentMax");
			String dataList;
			String currentMax;
			if (dataListAndCurrentMax != null) {
				dataList = matcher.group("dataList");
				currentMax = matcher.group("currentMax");
			} else {
				dataList = "";
				currentMax = "";
			}
			newSeriesList.add(AnimediaMALTitleReferences.builder().url(matcher.group("root")).dataList(dataList)
					.currentMax(currentMax).build());
		}
		if (newSeriesList.size() != NUMBER_OF_CURRENTLY_UPDATED_TITLES) {
			throw new NewEpisodesListNotFoundException("New episodes not found!");
		}
		return newSeriesList;
	}

	private String searchForOriginalTitle(String content) throws OriginalTitleNotFoundException {
		Matcher matcher = ORIGINAL_TITLE_PATTERN.matcher(content);
		if (matcher.find()) {
			return matcher.group("originalTitle");
		}
		throw new OriginalTitleNotFoundException("Original title not found!");
	}

	private String searchForFirstEpisodeInSeason(String content) throws FirstEpisodeInSeasonNotFoundException {
		Matcher matcher = EPISODE_IN_DATA_LIST_PATTERN.matcher(content);
		if (matcher.find()) {
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			if (firstEpisodeInSeason == null && description != null) {
				return BaseConstants.FIRST_EPISODE;
			}
			return firstEpisodeInSeason;
		}
		throw new FirstEpisodeInSeasonNotFoundException("First episode not found!");
	}

	private Map<String, List<String>> searchForEpisodesRange(String content) throws EpisodesRangeNotFoundException {
		Matcher matcher = EPISODE_IN_DATA_LIST_EXCEPT_TRAILERS_PATTERN.matcher(content);
		List<String> episodes = new LinkedList<>();
		Map<String, List<String>> episodesRange = new HashMap<>();
		String maxEpisodes = null;
		while (matcher.find()) {
			if (maxEpisodes == null) {
				maxEpisodes = matcher.group("maxEpisodes");
			}
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			String joinedEpisode = matcher.group("joinedEpisode");
			if (firstEpisodeInSeason != null) {
				if (joinedEpisode != null) {
					episodes.add(firstEpisodeInSeason + "-" + joinedEpisode);
				} else {
					checkEpisodeAndAdd(episodes, firstEpisodeInSeason);
				}
			} else if (description != null) {
				checkEpisodeAndAdd(episodes, BaseConstants.FIRST_EPISODE);
			}
		}
		if (isTrailer(content)) {
			maxEpisodes = BaseConstants.ZERO_EPISODE;
			episodes.add(BaseConstants.ZERO_EPISODE);
		} else if (episodes.isEmpty() || maxEpisodes == null) {
			throw new EpisodesRangeNotFoundException("Episodes range is not found for " + getUrl(content));
		}
		episodesRange.put(maxEpisodes, episodes);
		return episodesRange;
	}

	private void checkEpisodeAndAdd(List<String> episodes, String episode) {
		if (episodes.contains(episode)) {
			Integer lasAdded = Integer.parseInt(episodes.get(episodes.size() - 1));
			lasAdded++;
			episodes.add(String.valueOf(lasAdded));
		} else {
			episodes.add(episode);
		}
	}

	private boolean isTrailer(String content) {
		Matcher matcher = TRAILER_IN_DATA_LIST_PATTERN.matcher(content);
		return matcher.find();
	}

	private String getUrl(String content) {
		Matcher matcher = URL_IN_DATA_LIST_PATTERN.matcher(content);
		if (matcher.find()) {
			return matcher.group("url");
		}
		return "url not found";
	}

	/**
	 * Searches for the mappings: a tab on page - a season, a tab on page - an episodes
	 * then merge if tabs are matched
	 *
	 * @param content the html page
	 * @return the map <anime id <data list, episodes>>
	 * @throws SeasonsAndEpisodesNotFoundException, if the mappings are not matched
	 */
	private Map<String, Map<String, String>> searchForDataListsAndMaxEpisodes(String content)
			throws SeasonsAndEpisodesNotFoundException, OriginalTitleNotFoundException {
		Map<String, String> tabsAndSeasons = new HashMap<>();
		Map<String, String> tabsAndEpisodes = new HashMap<>();
		Map<String, String> tabsAndDataList = new HashMap<>();
		Map<String, String> seasonsAndEpisodes = new HashMap<>();
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodes = new HashMap<>();
		Matcher matcher = TAB_AND_SEASONS_PATTERN.matcher(content);
		String numberOfTab;
		while (matcher.find()) {
			numberOfTab = matcher.group("numberOfTab");
			tabsAndSeasons.put(numberOfTab, matcher.group("season"));
		}
		matcher = TAB_AND_EPISODES_PATTERN.matcher(content);
		while (matcher.find()) {
			numberOfTab = matcher.group("numberOfTab");
			tabsAndDataList.put(numberOfTab, matcher.group("dataList"));
			tabsAndEpisodes.put(numberOfTab, matcher.group("numberOfEpisodes"));
		}
		for (Map.Entry<String, String> seasonTab : tabsAndSeasons.entrySet()) {
			for (Map.Entry<String, String> episodeTab : tabsAndEpisodes.entrySet()) {
				if (seasonTab.getKey().equals(episodeTab.getKey())) {
					seasonsAndEpisodes
							.put(tabsAndDataList.get(seasonTab.getKey()), checkSeason(seasonTab.getValue()) ? seasonTab.getValue() : episodeTab.getValue());
				}
			}
		}
		if (seasonsAndEpisodes.isEmpty()) {
			throw new SeasonsAndEpisodesNotFoundException("Seasons And Episodes not found for " + searchForOriginalTitle(content));
		}
		matcher = DATA_ENTRY_ID_PATTERN.matcher(content);
		if (matcher.find()) {
			animeIdSeasonsAndEpisodes.put(matcher.group("animeId"), seasonsAndEpisodes);
		}
		return animeIdSeasonsAndEpisodes;
	}

	/**
	 * Checks the season description for the range
	 * For example, 1-700 or 701-xxx
	 *
	 * @param season the season description
	 * @return true if the description contain the range
	 */
	private boolean checkSeason(String season) {
		Matcher matcher = DATALIST_EPISODES_RANGE_PATTERN.matcher(season);
		return matcher.find();
	}
}
