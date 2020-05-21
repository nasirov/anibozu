package nasirov.yv.service.impl.fandub.nine_anime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;
import static org.springframework.web.util.UriUtils.encode;

import com.google.common.primitives.Ints;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.NineAnimeFeignClient;
import nasirov.yv.service.EpisodeUrlServiceI;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NineAnimeEpisodeUrlService implements EpisodeUrlServiceI {

	private static final Pattern DATA_ID_PATTERN = Pattern.compile("^/watch/.+\\.(?<dataId>.+)$");

	private static final Pattern EPISODE_NUMBER_PATTERN = Pattern.compile("^.*?(?<episodeNumber>\\d+).*?$");

	private static final String DUB_SUFFIX = " (dub)";

	private final NineAnimeFeignClient nineAnimeFeignClient;

	private final UrlsNames urlsNames;

	@Override
	public String getEpisodeUrl(MalTitle watchingTitle) {
		return buildEpisodeUrl(watchingTitle);
	}

	private String buildEpisodeUrl(MalTitle watchingTitle) {
		Optional<Element> searchResultWithTitleUrl = getSearchResultWithTitleUrl(watchingTitle.getName());
		return searchResultWithTitleUrl.map(x -> urlForTitle(watchingTitle, x))
				.orElseGet(() -> urlForNotFoundTitle(watchingTitle));
	}

	private String urlForTitle(MalTitle watchingTitle, Element elementWithTitleUrl) {
		String titleUrl = extractTitleUrl(elementWithTitleUrl);
		String dataId = extractDataId(titleUrl);
		String titleEpisodesInfoHtml = getTitleEpisodesInfoHtml(dataId);
		int episode = getNextEpisodeForWatch(watchingTitle);
		return extractEpisodeUrl(titleEpisodesInfoHtml, episode);
	}

	private String urlForNotFoundTitle(MalTitle watchingTitle) {
		log.debug("TITLE [{}] WAS NOT FOUND ON 9Anime!", watchingTitle);
		return NOT_FOUND_ON_FANDUB_SITE_URL;
	}

	private Optional<Element> getSearchResultWithTitleUrl(String titleName) {
		String htmlWithTitleInfo = searchTitleByNameAndGetResultHtml(titleName);
		Document document = Jsoup.parse(htmlWithTitleInfo);
		return document.getElementsByClass("name")
				.stream()
				.filter(x -> isTargetTitle(titleName, x))
				.max(comparing(x -> x.text()
						.length()));
	}

	private String searchTitleByNameAndGetResultHtml(String titleName) {
		return nineAnimeFeignClient.searchTitleByName(encodeTitleName(titleName))
				.getHtml();
	}

	private String encodeTitleName(String titleName) {
		return titleName.contains(";") ? encode(titleName, UTF_8.name()) : titleName;
	}

	private boolean isTargetTitle(String titleName, Element element) {
		String titleNameFromSearchResult = element.text()
				.toLowerCase();
		return titleNameFromSearchResult.equals(titleName + DUB_SUFFIX) || titleNameFromSearchResult.equals(titleName);
	}

	private String extractTitleUrl(Element elementWithTitleUrl) {
		return elementWithTitleUrl.attr("href");
	}

	private String extractDataId(String titleUrl) {
		Matcher matcher = DATA_ID_PATTERN.matcher(titleUrl);
		return matcher.find() ? matcher.group("dataId") : StringUtils.EMPTY;
	}

	private String getTitleEpisodesInfoHtml(String dataId) {
		return nineAnimeFeignClient.getTitleEpisodesInfo(dataId)
				.getHtml();
	}

	private String extractEpisodeUrl(String titleEpisodesInfoHtml, int nextEpisodeForWatch) {
		Document document = Jsoup.parse(titleEpisodesInfoHtml);
		return document.select(".episodes.range > li > a")
				.stream()
				.filter(x -> isNewEpisodeAvailable(nextEpisodeForWatch, x))
				.map(x -> urlsNames.getNineAnimeUrls()
						.getNineAnimeTo() + x.attr("href"))
				.findFirst()
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}
	private boolean isNewEpisodeAvailable(int nextEpisodeForWatch, Element element) {
		String episodeNumber = element.text();
		Integer parsedEpisodeNumber = Ints.tryParse(episodeNumber);
		if (isNull(parsedEpisodeNumber)) {
			parsedEpisodeNumber = extractEpisodeInExtraWay(episodeNumber);
		}
		return parsedEpisodeNumber == nextEpisodeForWatch;
	}

	private int extractEpisodeInExtraWay(String episodeNumber) {
		Integer result = extractEpisodeViaRegexp(episodeNumber);
		return nonNull(result) ? result : extractEpisodeViaStubConstant(episodeNumber);
	}

	private Integer extractEpisodeViaRegexp(String episodeNumber) {
		Matcher matcher = EPISODE_NUMBER_PATTERN.matcher(episodeNumber);
		Integer parsedEpisodeNumber = null;
		if (matcher.find()) {
			String extractedEpisodeViaRegexp = matcher.group("episodeNumber");
			parsedEpisodeNumber = Integer.parseInt(extractedEpisodeViaRegexp);
			log.debug("EPISODE_NUMBER_PATTERN extracted episode via regexp [{}] parsed [{}] from [{}]",
					extractedEpisodeViaRegexp,
					parsedEpisodeNumber,
					episodeNumber);
		}
		return parsedEpisodeNumber;
	}

	private int extractEpisodeViaStubConstant(String episodeNumber) {
		int result = 0;
		if ("Full".equals(episodeNumber)) {
			result = 1;
		} else {
			log.error("UNKNOWN EPISODE STUB CONSTANT [{}]!", episodeNumber);
		}
		return result;
	}
}
