package nasirov.yv.service.impl.fandub.anidub;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.data.constants.ServiceSourceType.SITE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnidubSiteFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.service.AnidubEpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = SITE)
public class AnidubSiteEpisodeUrlService implements AnidubEpisodeUrlServiceI {

	private final AnidubSiteFeignClient anidubSiteFeignClient;

	private final TitlesServiceI<AnidubSiteTitle> anidubSiteTitleService;

	private final AnidubParserI anidubParser;

	private final UrlsNames urlsNames;

	@Override
	public String getEpisodeUrl(MalTitle watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		AnidubSiteTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
			url = episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnidubUrls()
					.getAnidubSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		} else {
			log.debug("TITLE [{}] WAS NOT FOUND ON ANIDUB!", watchingTitle.getName());
		}
		return url;
	}

	private AnidubSiteTitle getMatchedTitle(MalTitle watchingTitle) {
		return Optional.ofNullable(anidubSiteTitleService.getTitles()
				.get(watchingTitle.getId()))
				.orElseGet(Collections::emptyList)
				.stream()
				.findFirst()
				.orElse(null);
	}

	private List<Integer> extractAvailableEpisodes(AnidubSiteTitle matchedTitle) {
		String titlePage = anidubSiteFeignClient.getTitlePage(matchedTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		Optional<Element> elementWithEpisodes = extractElementWithEpisodes(document);
		return elementWithEpisodes.map(this::extractEpisodes)
				.orElseGet(Collections::emptyList);
	}

	private Optional<Element> extractElementWithEpisodes(Document document) {
		return Optional.ofNullable(document.getElementsByClass("tabs-sel series-tab")
				.first());
	}

	private List<Integer> extractEpisodes(Element elementWithEpisodes) {
		return elementWithEpisodes.select("span")
				.stream()
				.map(this::parseEpisodeNumber)
				.collect(Collectors.toList());
	}

	private Integer parseEpisodeNumber(Element elementWithEpisode) {
		return anidubParser.extractEpisodeNumber(elementWithEpisode.text());
	}
}
