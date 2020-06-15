package nasirov.yv.service.impl.fandub.jisedai;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.List;
import java.util.stream.Collectors;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.JisedaiSiteFeignClient;
import nasirov.yv.parser.JisedaiParserI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.common.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class JisedaiEpisodeUrlService extends BaseEpisodeUrlService<JisedaiSiteTitle> {

	private final JisedaiSiteFeignClient jisedaiSiteFeignClient;

	private final JisedaiParserI jisedaiParser;

	private final UrlsNames urlsNames;

	public JisedaiEpisodeUrlService(JisedaiSiteFeignClient jisedaiSiteFeignClient, TitlesServiceI<JisedaiSiteTitle> jisedaiSiteTitleService,
			JisedaiParserI jisedaiParser, UrlsNames urlsNames) {
		super(jisedaiSiteTitleService);
		this.jisedaiSiteFeignClient = jisedaiSiteFeignClient;
		this.jisedaiParser = jisedaiParser;
		this.urlsNames = urlsNames;
	}

	@Override
	protected String buildUrl(MalTitle watchingTitle, List<JisedaiSiteTitle> matchedTitles) {
		JisedaiSiteTitle matchedTitle = matchedTitles.get(0);
		List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
		return episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getJisedaiUrls()
				.getJisedaiSiteUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
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
