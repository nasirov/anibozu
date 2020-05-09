package nasirov.yv.service.impl.fandub.anidub;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.data.constants.ServiceSourceType.SITE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnidubSiteFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.service.AnidubEpisodeUrlServiceI;
import nasirov.yv.service.AnidubGitHubResourcesServiceI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

	private final AnidubGitHubResourcesServiceI<AnidubSiteTitle> anidubGitHubResourcesService;

	private final AnidubParserI anidubParser;

	private final UrlsNames urlsNames;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		AnidubSiteTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
			url = episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnidubUrls()
					.getAnidubSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		} else {
			log.debug("TITLE [{}] WAS NOT FOUND ON ANIDUB!", watchingTitle.getTitle());
		}
		return url;
	}

	private AnidubSiteTitle getMatchedTitle(UserMALTitleInfo watchingTitle) {
		return anidubGitHubResourcesService.getAnidubTitles()
				.get(watchingTitle.getAnimeId());
	}

	private List<Integer> extractAvailableEpisodes(AnidubSiteTitle matchedTitle) {
		String titlePage = anidubSiteFeignClient.getTitlePage(matchedTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return document.getElementsByClass("tabs-sel series-tab")
				.eachText()
				.stream()
				.distinct()
				.map(anidubParser::extractEpisodeNumber)
				.map(Integer::valueOf)
				.collect(Collectors.toList());
	}
}
