package nasirov.yv.parser;

import static nasirov.yv.enums.Constants.FIRST_EPISODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.EpisodesRangeNotFoundException;
import nasirov.yv.exception.FirstEpisodeInSeasonNotFoundException;
import nasirov.yv.exception.NewEpisodesListNotFoundException;
import nasirov.yv.exception.OriginalTitleNotFoundException;
import nasirov.yv.exception.SeasonsAndEpisodesNotFoundException;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import org.springframework.stereotype.Component;

/**
 * Html parser
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class AnimediaHTMLParser {

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
					+ "(?<numberOfEpisodes>(\\d{1,4}|[xX]{1,3}))(.+)?</div>";

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
	 * For episodes in data list
	 */
	private static final String EPISODE_IN_DATA_LIST = "<span>(?<description>Серия\\.|Cерия|Серия|Серии|серия|серии|ОВА|OVA|ONA|ODA"
			+ "|ФИЛЬМ|Фильмы|Сп[е|э]шл|СПЕШЛ|Фильм|Трейлер)?\\s?(?<firstEpisodeInSeason>\\d{1,3})?"
			+ "(-\\d{1,3})?(\\s\\(\\d{1,3}\\))?\\s*?(из)?\\s?(?<maxEpisodes>.{1,3})?</span>";

	/**
	 * For original title
	 */
	private static final String ORIGINAL_TITLE = "<div class=\"media__post__original-title\">(?<originalTitle>[^а-яА-Я\\n]*).+?</div>";

	/**
	 * For currently updated titles
	 */
	private static final String NEW_SERIES_INFO =
			"\\s*<div class=\"widget__new-series__item widget__item\">\\R*\\s*<a href=\"(?<fullUrl>(?<root>/anime/.+)/(?<dataList>\\d{1,3})/"
					+ "(?<currentMax>\\d{1,3}))\" title=\".+\" class=\"widget__new-series__item__thumb\"><img data-src=\".+\" alt=\".+\" title=\""
					+ ".+\"></a>\\R*\\s*<div class=\"widget__new-series__item__info\">\\R*"
					+ "\\s*<a href=\".+\" title=\".+\" class=\"h4 widget__new-series__item__title\">.+</a>";

	private static final String GET_URL = "href=\"(?<url>/anime/.*?/\\d{1,3}/\\d{1,3})\"";

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
	public Map<String, Map<String, String>> getAnimeIdSeasonsAndEpisodesMap(@NotNull HttpResponse response) {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodes = new HashMap<>();
		try {
			animeIdSeasonsAndEpisodes = searchForSeasonsAndEpisodes(response.getContent());
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
	public String getFirstEpisodeInSeason(@NotNull HttpResponse response) {
		String firstEpisodeNumber = null;
		try {
			firstEpisodeNumber = searchForFirstEpisodeInSeason(response.getContent());
		} catch (FirstEpisodeInSeasonNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return firstEpisodeNumber;
	}

	/**
	 * Searches for the episodes range in the data list
	 * request example http://online.animedia.tv/ajax/episodes/9480/3
	 * 9480 - anime id
	 * 3 - data list
	 *
	 * @param response the animedia response
	 * @return the map<data list, list<episodes range>>
	 */
	public Map<String, List<String>> getEpisodesRange(@NotNull HttpResponse response) {
		Map<String, List<String>> firstEpisodeNumber = new HashMap<>();
		try {
			firstEpisodeNumber = searchForEpisodesRange(response.getContent());
		} catch (EpisodesRangeNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return firstEpisodeNumber;
	}

	/**
	 * Searches for a title name
	 *
	 * @param response the animedia response
	 * @return the title name
	 */
	public String getOriginalTitle(@NotNull HttpResponse response) {
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
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitlesList(@NotNull HttpResponse response) {
		List<AnimediaMALTitleReferences> newSeriesList = new ArrayList<>();
		try {
			newSeriesList = searchForCurrentlyUpdatedTitles(response.getContent());
		} catch (NewEpisodesListNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return newSeriesList;
	}

	private List<AnimediaMALTitleReferences> searchForCurrentlyUpdatedTitles(@NotEmpty String content) throws NewEpisodesListNotFoundException {
		Pattern pattern = Pattern.compile(NEW_SERIES_INFO);
		Matcher matcher = pattern.matcher(content);
		List<AnimediaMALTitleReferences> newSeriesList = new ArrayList<>();
		while (matcher.find()) {
			String dataList = matcher.group("dataList");
			String currentMax = matcher.group("currentMax");
			newSeriesList.add(new AnimediaMALTitleReferences(matcher.group("root"),
					dataList != null ? dataList : "",
					"",
					"",
					"",
					"",
					currentMax != null ? currentMax : "",
					"",
					"",
					""));
		}
		if (newSeriesList.isEmpty()) {
			throw new NewEpisodesListNotFoundException("New episodes not found!");
		}
		return newSeriesList;
	}

	private String searchForOriginalTitle(@NotEmpty String content) throws OriginalTitleNotFoundException {
		Pattern pattern = Pattern.compile(ORIGINAL_TITLE);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group("originalTitle");
		}
		throw new OriginalTitleNotFoundException("Original title not found!");
	}

	private String searchForFirstEpisodeInSeason(@NotEmpty String content) throws FirstEpisodeInSeasonNotFoundException {
		Pattern pattern = Pattern.compile(EPISODE_IN_DATA_LIST);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			if (firstEpisodeInSeason == null && description != null) {
				return FIRST_EPISODE.getDescription();
			}
			return firstEpisodeInSeason;
		}
		throw new FirstEpisodeInSeasonNotFoundException("First episode not found!");
	}

	private Map<String, List<String>> searchForEpisodesRange(@NotEmpty String content) throws EpisodesRangeNotFoundException {
		Pattern pattern = Pattern.compile(EPISODE_IN_DATA_LIST);
		Matcher matcher = pattern.matcher(content);
		List<String> episodes = new LinkedList<>();
		Map<String, List<String>> episodesRange = new HashMap<>();
		String maxEpisodes = null;
		while (matcher.find()) {
			if (maxEpisodes == null) {
				maxEpisodes = matcher.group("maxEpisodes");
			}
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			if (firstEpisodeInSeason != null) {
				if (episodes.contains(firstEpisodeInSeason)) {
					Integer lasAdded = Integer.parseInt(episodes.get(episodes.size() - 1));
					lasAdded++;
					episodes.add(String.valueOf(lasAdded));
				} else {
					episodes.add(firstEpisodeInSeason);
				}
			} else if (description != null) {
				episodes.add(isDescription(description) ? FIRST_EPISODE.getDescription() : description);
			}
		}
		if (episodes.isEmpty() || maxEpisodes == null) {
			throw new EpisodesRangeNotFoundException("Episodes range is not found for " + getUrl(content));
		}
		episodesRange.put(maxEpisodes, episodes);
		return episodesRange;
	}

	private String getUrl(@NotEmpty String content) {
		Pattern pattern = Pattern.compile(GET_URL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group("url");
		}
		return "url not found";
	}

	private boolean isDescription(@NotEmpty String description) {
		Pattern pattern = Pattern.compile("\\D+");
		Matcher matcher = pattern.matcher(description);
		return matcher.find();
	}

	/**
	 * Searches for the mappings: a tab on page - a season, a tab on page - an episodes
	 * then merge if tabs are matched
	 *
	 * @param content the html page
	 * @return the map <anime id <data list, episodes>>
	 * @throws SeasonsAndEpisodesNotFoundException, if the mappings are not matched
	 */
	private Map<String, Map<String, String>> searchForSeasonsAndEpisodes(@NotEmpty String content)
			throws SeasonsAndEpisodesNotFoundException, OriginalTitleNotFoundException {
		Map<String, String> tabsAndSeasons = new HashMap<>();
		Map<String, String> tabsAndEpisodes = new HashMap<>();
		Map<String, String> tabsAndDataList = new HashMap<>();
		Map<String, String> seasonsAndEpisodes = new HashMap<>();
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodes = new HashMap<>();
		Pattern pattern = Pattern.compile(TAB_AND_SEASONS);
		Matcher matcher = pattern.matcher(content);
		String numberOfTab;
		while (matcher.find()) {
			numberOfTab = matcher.group("numberOfTab");
			tabsAndSeasons.put(numberOfTab, matcher.group("season"));
		}
		pattern = Pattern.compile(TAB_AND_EPISODES);
		matcher = pattern.matcher(content);
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
		pattern = Pattern.compile(DATA_ENTRY_ID);
		matcher = pattern.matcher(content);
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
	private boolean checkSeason(@NotEmpty String season) {
		Pattern pattern = Pattern.compile("(\\d{1,3}-(\\d{1,3}|[xX]{1,3}))");
		Matcher matcher = pattern.matcher(season);
		return matcher.find();
	}
}
