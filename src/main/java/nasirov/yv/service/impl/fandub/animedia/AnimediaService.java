package nasirov.yv.service.impl.fandub.animedia;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.fandub.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.properties.AnimediaProps;
import nasirov.yv.fandub.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animedia.AnimediaFeignClient;
import nasirov.yv.service.AnimediaServiceI;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaService implements AnimediaServiceI {

	private static final Pattern URL_PATTERN = Pattern.compile("^.+(?<url>anime/.+)$");

	private static final Pattern FILLED_DATA_LIST_PATTERN = Pattern.compile("^.+из.+$");

	private final AnimediaFeignClient animediaFeignClient;

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
			String rawPageWithTitles = animediaFeignClient.getTitles(offset, limit);
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
		String animePageWithDataLists = animediaFeignClient.getTitlePage(animediaSearchTitle.getUrl());
		Elements tabsElements = extractTabsElements(animePageWithDataLists);
		return tabsElements.stream()
				.filter(this::isFilledDataList)
				.map(this::extractDataList)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getEpisodes(String animeId, String dataList) {
		List<AnimediaEpisode> episodes = animediaFeignClient.getTitleEpisodesByPlaylist(Integer.parseInt(animeId), Integer.parseInt(dataList));
		return episodes.stream()
				.map(AnimediaEpisode::getEpisodeName)
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
		return Optional.ofNullable(elementWithUrlValue.selectFirst(".ads-list__item__thumb.js-postload a"))
				.map(x -> x.attr("href"))
				.orElse(StringUtils.EMPTY);
	}

	private String cutUrl(String fullUrl) {
		Matcher matcher = URL_PATTERN.matcher(fullUrl);
		return matcher.find() ? matcher.group("url") : StringUtils.EMPTY;
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
		return Optional.ofNullable(elementWithDataListEpisodesDescription.selectFirst(
				".media__tabs__series__footer__item.media__tabs__series__footer__item__center"))
				.map(x -> FILLED_DATA_LIST_PATTERN.matcher(x.text())
						.find())
				.orElse(false);
	}
}
