package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.data.constants.ServiceSourceType.SITE;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.site.SiteEpisode;
import nasirov.yv.data.properties.AnimediaProps;
import nasirov.yv.http.feign.AnimediaSiteFeignClient;
import nasirov.yv.service.AnimediaServiceI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.animedia-service-source", havingValue = SITE)
public class AnimediaSiteService implements AnimediaServiceI {

	private static final Pattern URL_PATTERN = Pattern.compile("^.+(?<url>anime/.+)$");

	private final AnimediaSiteFeignClient animediaSiteFeignClient;

	private final AnimediaProps animediaProps;

	private int maxSize;

	private int limit;

	@PostConstruct
	public void init() {
		maxSize = animediaProps.getSearchListMaxSize();
		limit = animediaProps.getRequestLimit();
	}

	@Override
	public Set<AnimediaSearchListTitle> getAnimediaSearchList() {
		Set<AnimediaSearchListTitle> animediaSearchList = new LinkedHashSet<>(maxSize);
		for (int offset = 0; offset < maxSize; offset += limit) {
			String rawPageWithTitles = animediaSiteFeignClient.getAnimediaSearchList(offset, limit);
			Elements titles = extractTitlesElements(rawPageWithTitles);
			titles.stream()
					.map(this::buildAnimediaSearchListTitle)
					.map(this::buildWithDataLists)
					.forEach(animediaSearchList::add);
		}
		return animediaSearchList;
	}

	@Override
	public List<String> getDataLists(AnimediaSearchListTitle animediaSearchTitle) {
		String animePageWithDataLists = animediaSiteFeignClient.getAnimePage(animediaSearchTitle.getUrl());
		Elements tabsElements = extractTabsElements(animePageWithDataLists);
		return tabsElements.stream()
				.filter(this::isFilledDataList)
				.map(this::extractDataList)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getEpisodes(String animeId, String dataList) {
		List<SiteEpisode> episodes = animediaSiteFeignClient.getEpisodes(animeId, dataList);
		return episodes.stream()
				.map(SiteEpisode::getEpisodeName)
				.collect(Collectors.toList());
	}

	private Elements extractTitlesElements(String rawPageWithTitles) {
		Document html = Jsoup.parse(rawPageWithTitles);
		return html.select(".ads-list__item");
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle(Element elementWithTitleInfo) {
		return AnimediaSearchListTitle.builder()
				.animeId(extractAnimeId(elementWithTitleInfo))
				.url(cutUrl(extractUrl(elementWithTitleInfo)))
				.build();
	}

	private AnimediaSearchListTitle buildWithDataLists(AnimediaSearchListTitle title) {
		title.setDataLists(this.getDataLists(title));
		return title;
	}

	private String extractAnimeId(Element elementWithAnimeIdValue) {
		return elementWithAnimeIdValue.select(".overlay__header")
				.attr("data-entry");
	}

	private String extractUrl(Element elementWithUrlValue) {
		return elementWithUrlValue.selectFirst(".ads-list__item__thumb.js-postload a")
				.attr("href");
	}

	private String cutUrl(String fullUrl) {
		Matcher matcher = URL_PATTERN.matcher(fullUrl);
		return matcher.find() ? matcher.group("url") : "";
	}

	private Elements extractTabsElements(String animePageWithDataLists) {
		Document html = Jsoup.parse(animePageWithDataLists);
		return html.select(".media__tabs__panel.tab-pane");
	}

	private String extractDataList(Element elementWithDataListValue) {
		return elementWithDataListValue.select(".media__tabs__series__list.carousel-inner")
				.attr("data-list_id");
	}

	private boolean isFilledDataList(Element elementWithDataListEpisodesDescription) {
		return elementWithDataListEpisodesDescription.selectFirst(".media__tabs__series__footer__item.media__tabs__series__footer__item__center")
				.text()
				.matches("^.+из.+$");
	}
}
