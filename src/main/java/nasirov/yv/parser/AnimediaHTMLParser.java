package nasirov.yv.parser;

import nasirov.yv.exception.FirstEpisodeInSeasonNotFoundException;
import nasirov.yv.exception.OriginalTitleNotFoundException;
import nasirov.yv.exception.SeasonsAndEpisodesNotFoundException;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nasirov.yv.enums.Constants.FIRST_EPISODE;

/**
 * Парсит html
 * Created by Хикка on 23.12.2018.
 */
@Component
public class AnimediaHTMLParser {
	private static final Logger logger = LoggerFactory.getLogger(AnimediaHTMLParser.class);
	
	/**
	 * Регулярное выражения для поиска номера вкладки на странице и соответствующего ей количества серий
	 */
	private static final String TAB_AND_EPISODES = "\\s*<div id=\"tab(?<numberOfTab>\\d{1,3})\" role=\"tabpanel\" class=\"media__tabs__panel tab-pane\\s?(active)?\">\n" +
			"\\s*<div id=\"carousel\\d{1,3}\" data-interval=\"false\" data-wrap=\"false\" class=\"media__tabs__series carousel slide\">\n" +
			"\\s*<div class=\"media__tabs__series__list carousel-inner\" data-list_id=\"(?<dataList>\\d{1,3})\"></div>\n" +
			"\\s*<div class=\"clearfix\"></div>\n" +
			"\\s*<div class=\"media__tabs__series__footer\">\n" +
			"\\s*<div class=\"media__tabs__series__footer__item\"><a href=\"#carousel\\d{1,3}\" data-slide=\"prev\" class=\"media__tabs__series__control carousel-control prev\"><i class=\"ai ai-prev\"></i></a></div>\n" +
			"\\s*<div class=\"media__tabs__series__footer__item media__tabs__series__footer__item__center\">Серии <span class=\"start-series\"></span>-<span class=\"end-series\"></span> из (?<numberOfEpisodes>(\\d{1,4}|[xX]{1,3}))(.+)?</div>";
	
	/**
	 * Регулярное выражения для поиска номера вкладки на странице и соответствующего ей сезона
	 */
	private static final String TAB_AND_SEASONS = "<li class=\"media__tabs__nav__item\\s?(active)?\"><a href=\"#tab(?<numberOfTab>\\d{1,3})\" role=\"tab\" data-toggle=\"tab\">(?<season>.+)</a></li>";
	
	/**
	 * Регулярное выражения для поиска id аниме
	 */
	private static final String DATA_ENTRY_ID = "<ul role=\"tablist\" class=\"media__tabs__nav nav-tabs\" data-entry_id=\"(?<animeId>\\d+)\".*?";
	
	/**
	 * Регулярное выражения для поиска серий в data list
	 */
	private static final String FIRST_EPISODE_IN_SEASON = "<span>(?<description>Серия\\.|Cерия|Серия|Серии|серия|серии|ОВА|OVA|ONA|ODA|ФИЛЬМ|Фильмы|Сп[е|э]шл|СПЕШЛ|Фильм)?\\s?(?<firstEpisodeInSeason>\\d{1,3})?(-\\d{1,3})?(\\s\\(\\d{1,3}\\))? из (?<maxEpisodes>.{1,3})</span>";
	
	private static final String ORIGINAL_TITLE = "<div class=\"media__post__original-title\">(?<originalTitle>[^а-яА-Я\\n]*).+?</div>";
//    private static final String NEW_SERIES_INFO = "<div class=\"widget__new-series__item widget__item\">\n" +
//            "<a href=\"(?<fullUrl>(?<root>/anime/.+)/(?<dataList>\\d{1,3})/(?<currentMax>\\d{1,3}))\" title=\"Cмотреть онлайн аниме (.+) Серия \\d{1,3}\" class=\"widget__new-series__item__thumb\"><img src=\"//static\\.animedia\\.tv/screens/.+\\.jpg.+\" alt=\"Cмотреть онлайн аниме .+ Серия \\d{1,3}\" title=\"Серия \\d{1,3} аниме .* онлайн\"></a>\n" +
//            "<div class=\"widget__new-series__item__info\">\n" +
//            "<a href=\"/anime/.+/\\d{1,3}/\\d{1,3}\" title=\"Cмотреть онлайн аниме .* Серия \\d{1,3}\" class=\"h4 widget__new-series__item__title\">(.+)</a>\n" +
//            "<div class=\"widget__new-series__item__status\">\n" +
//            ".+\\n.+\n" +
//            "</div>\n" +
//            "<div class=\"date_newseries\">.+</div>\n" +
//            "</div>\n" +
//            "</div>";
	
	private static final String NEW_SERIES_INFO = "<div class=\"widget__new-series__item widget__item\">\n" +
			"<a href=\\\"(?<fullUrl>(?<root>\\/anime\\/.+)\\/(?<dataList>\\d{1,3})\\/(?<currentMax>\\d{1,3}))\" title=\".+\" class=\"widget__new-series__item__thumb\"><img src=\".+\" alt=\".+\" title=\".+\"><\\/a>\n" +
			"<div class=\"widget__new-series__item__info\">\n" +
			"<a href=\".+\" title=\".+\" class=\"h4 widget__new-series__item__title\">.+<\\/a>";
	
	/**
	 * Ищет соответствия data list и серий
	 *
	 * @param response ответ от сайта
	 * @return map <anime id,map<data list, number of episodes>
	 */
	public Map<String, Map<String, String>> getAnimeIdSeasonsAndEpisodesMap(HttpResponse response) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodes = new HashMap<>();
		try {
			animeIdSeasonsAndEpisodes = searchForSeasonsAndEpisodes(response.getContent());
		} catch (SeasonsAndEpisodesNotFoundException | OriginalTitleNotFoundException e) {
			System.out.println(e.getMessage());
			logger.warn(e.getMessage());
		}
		logger.debug("End Parsing");
		return animeIdSeasonsAndEpisodes;
	}
	
	/**
	 * Ишет первый эпизод в сезоне
	 *
	 * @param response html
	 * @return первый эпизод
	 */
	public String getFirstEpisodeInSeason(HttpResponse response) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		String firstEpisodeNumber = null;
		try {
			firstEpisodeNumber = searchForFirstEpisodeInSeason(response.getContent());
		} catch (FirstEpisodeInSeasonNotFoundException e) {
			System.out.println("First episode in season not found!");
			logger.error("First episode in season not found!");
		}
		logger.debug("End Parsing");
		return firstEpisodeNumber;
	}
	
	/**
	 * Ишет первый эпизод в data list
	 *
	 * @param response html
	 * @return первый эпизод
	 */
	public Map<String, List<String>> getEpisodesRange(HttpResponse response) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		Map<String, List<String>> firstEpisodeNumber = null;
		try {
			firstEpisodeNumber = searchForEpisodesRange(response.getContent());
		} catch (FirstEpisodeInSeasonNotFoundException e) {
			System.out.println("First episode in season not found!");
			logger.error("First episode in season not found!");
		}
		logger.debug("End Parsing");
		return firstEpisodeNumber;
	}
	
	/**
	 * Ищет оригинальное название
	 *
	 * @param response html
	 * @return оригинальное название
	 */
	public String getOriginalTitle(HttpResponse response) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		String originalTitle = null;
		try {
			originalTitle = searchForOriginalTitle(response.getContent());
		} catch (OriginalTitleNotFoundException e) {
			System.out.println("Original title not found!");
			logger.error("Original title not found!");
		}
		logger.debug("End Parsing");
		return originalTitle;
	}
	
	public List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitlesList(HttpResponse response) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		List<AnimediaMALTitleReferences> newSeriesList = null;
		try {
			newSeriesList = searchForCurrentlyUpdatedTitles(response.getContent());
		} catch (Exception e) {
			System.out.println("New Series list not found!");
			logger.error("New Series list not found!");
		}
		logger.debug("End Parsing");
		return newSeriesList;
	}
	
	private List<AnimediaMALTitleReferences> searchForCurrentlyUpdatedTitles(String content) {
		Pattern pattern = Pattern.compile(NEW_SERIES_INFO);
		Matcher matcher = pattern.matcher(content);
		List<AnimediaMALTitleReferences> newSeriesList = new ArrayList<>();
		while (matcher.find()) {
			String dataList = matcher.group("dataList");
			String currentMax = matcher.group("currentMax");
			newSeriesList.add(new AnimediaMALTitleReferences(matcher.group("root"),
					dataList != null ? dataList : "", "", "", "", "",
					currentMax != null ? currentMax : "", "", "", ""));
		}
		if (newSeriesList.size() == 0) {
			throw new RuntimeException("New Series List is empty");
		}
		return newSeriesList;
	}
	
	private String searchForOriginalTitle(String content) throws OriginalTitleNotFoundException {
		Pattern pattern = Pattern.compile(ORIGINAL_TITLE);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group("originalTitle");
		}
		throw new OriginalTitleNotFoundException("Original title not found!");
	}
	
	private String searchForFirstEpisodeInSeason(String content) throws FirstEpisodeInSeasonNotFoundException {
		Pattern pattern = Pattern.compile(FIRST_EPISODE_IN_SEASON);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			String description = matcher.group("description");
			String firstEpisodeInSeason = matcher.group("firstEpisodeInSeason");
			if (firstEpisodeInSeason == null && description != null) {
				return FIRST_EPISODE.getDescription();
			}
			return firstEpisodeInSeason;
		}
		throw new FirstEpisodeInSeasonNotFoundException("First episode in season not found!");
	}
	
	private Map<String, List<String>> searchForEpisodesRange(String content) throws FirstEpisodeInSeasonNotFoundException {
		Pattern pattern = Pattern.compile(FIRST_EPISODE_IN_SEASON);
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
				episodes.add(checkDescription(description) ? FIRST_EPISODE.getDescription() : description);
			}
		}
		if (episodes.isEmpty() || maxEpisodes == null) {
			throw new FirstEpisodeInSeasonNotFoundException("First episode in season not found!");
		}
		episodesRange.put(maxEpisodes, episodes);
		return episodesRange;
	}
	
	private boolean checkDescription(String description) {
		Pattern pattern = Pattern.compile("\\D+");
		Matcher matcher = pattern.matcher(description);
		return matcher.find();
	}
	
	/**
	 * Ищет соответствия вкладок-сезонов, вкладок-серий, затем мержит по вкладкам, если есть совпадения
	 *
	 * @param content html страница сайта
	 * @return map <anime id <data list, episodes>>
	 * @throws SeasonsAndEpisodesNotFoundException, если не найдено соответсвий сезонов и серий
	 */
	private Map<String, Map<String, String>> searchForSeasonsAndEpisodes(String content) throws SeasonsAndEpisodesNotFoundException, OriginalTitleNotFoundException {
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
					seasonsAndEpisodes.put(tabsAndDataList.get(seasonTab.getKey()), checkSeason(seasonTab.getValue()) ? seasonTab.getValue() : episodeTab.getValue());
				}
			}
		}
		if (seasonsAndEpisodes.isEmpty()) {
			throw new SeasonsAndEpisodesNotFoundException("Seasons And Episodes not found!" + searchForOriginalTitle(content));
		}
		pattern = Pattern.compile(DATA_ENTRY_ID);
		matcher = pattern.matcher(content);
		if (matcher.find()) {
			animeIdSeasonsAndEpisodes.put(matcher.group("animeId"), seasonsAndEpisodes);
		}
		return animeIdSeasonsAndEpisodes;
	}
	
	/**
	 * Проверяет строку описания сезона на диапазон
	 * Например, 1-700 или 701-xxx
	 *
	 * @param season описание сезона
	 * @return true, если описание содержит диапазон
	 */
	private boolean checkSeason(String season) {
		Pattern pattern = Pattern.compile("(\\d{1,3}-(\\d{1,3}|[xX]{1,3}))");
		Matcher matcher = pattern.matcher(season);
		return matcher.find();
	}
}
