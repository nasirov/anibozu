package nasirov.yv.service.impl.fandub.jisedai;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.JisedaiSiteFeignClient;
import nasirov.yv.parser.JisedaiParserI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
public class JisedaiEpisodeUrlService implements EpisodeUrlServiceI {

	private final JisedaiSiteFeignClient jisedaiSiteFeignClient;

	private final TitlesServiceI<JisedaiSiteTitle> jisedaiSiteTitleService;

	private final JisedaiParserI jisedaiParser;

	private final UrlsNames urlsNames;

	@Override
	public String getEpisodeUrl(MalTitle watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		JisedaiSiteTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
			url = episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getJisedaiUrls()
					.getJisedaiSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		}
		return url;
	}

	private JisedaiSiteTitle getMatchedTitle(MalTitle watchingTitle) {
		return Optional.ofNullable(jisedaiSiteTitleService.getTitles()
				.get(watchingTitle.getId()))
				.orElseGet(Collections::emptyList)
				.stream()
				.findFirst()
				.orElse(null);
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
