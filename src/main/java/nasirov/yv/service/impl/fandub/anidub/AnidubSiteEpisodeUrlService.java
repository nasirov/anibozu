package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.ServiceSourceType.SITE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnidubSiteFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.service.AnidubEpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.common.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = SITE)
public class AnidubSiteEpisodeUrlService extends BaseEpisodeUrlService<AnidubSiteTitle> implements AnidubEpisodeUrlServiceI {

	private final AnidubSiteFeignClient anidubSiteFeignClient;

	private final AnidubParserI anidubParser;

	private final UrlsNames urlsNames;

	public AnidubSiteEpisodeUrlService(AnidubSiteFeignClient anidubSiteFeignClient, TitlesServiceI<AnidubSiteTitle> anidubSiteTitleService,
			AnidubParserI anidubParser, UrlsNames urlsNames) {
		super(anidubSiteTitleService);
		this.anidubSiteFeignClient = anidubSiteFeignClient;
		this.anidubParser = anidubParser;
		this.urlsNames = urlsNames;
	}

	@Override
	protected String buildUrl(MalTitle watchingTitle, List<AnidubSiteTitle> matchedTitles) {
		AnidubSiteTitle matchedTitle = matchedTitles.get(0);
		List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
		return episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnidubUrls()
				.getAnidubSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
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
