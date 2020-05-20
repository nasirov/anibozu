package nasirov.yv.service.impl.fandub.jisedai;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.JisedaiSiteFeignClient;
import nasirov.yv.parser.JisedaiParserI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.JisedaiGitHubResourcesServiceI;
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
public class JisedaiEpisodeUrlService implements EpisodeUrlServiceI {

	private final JisedaiSiteFeignClient jisedaiSiteFeignClient;

	private final JisedaiGitHubResourcesServiceI<JisedaiSiteTitle> jisedaiGitHubResourcesService;

	private final JisedaiParserI jisedaiParser;

	private final UrlsNames urlsNames;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		JisedaiSiteTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
			url = episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getJisedaiUrls()
					.getJisedaiSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		} else {
			log.debug("TITLE [{}] WAS NOT FOUND ON JISEDAI!", watchingTitle.getTitle());
		}
		return url;
	}

	private JisedaiSiteTitle getMatchedTitle(UserMALTitleInfo watchingTitle) {
		return jisedaiGitHubResourcesService.getJisedaiTitles()
				.get(watchingTitle.getAnimeId());
	}

	private List<Integer> extractAvailableEpisodes(JisedaiSiteTitle matchedTitle) {
		String titlePage = jisedaiSiteFeignClient.getTitlePage(matchedTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return document.getElementsByClass("episode")
				.stream()
				.map(this::parseEpisodeNumber)
				.collect(Collectors.toList());
	}

	private Integer parseEpisodeNumber(Element elementWithEpisode) {
		return jisedaiParser.extractEpisodeNumber(elementWithEpisode.text());
	}
}
