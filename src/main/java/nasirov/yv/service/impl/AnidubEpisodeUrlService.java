package nasirov.yv.service.impl;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.AnidubTitle;
import nasirov.yv.data.anidub.AnidubTitleEpisode;
import nasirov.yv.data.anidub.AnidubTitleFandubSource;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.GithubResources;
import nasirov.yv.http.feign.AnidubApiFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.service.EpisodeUrlServiceI;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnidubEpisodeUrlService implements EpisodeUrlServiceI {

	private static final String ANIDUB_FANDUB_NAME = "AniDUB";

	private static final String UNAVAILABLE_SOURCE_MARK = "(не работает)";

	private static final String UNSUPPORTED_SOURCE_MARK = "Kodik";

	private static final String VK_COM = "vk.com";

	private static final String OUT_PLADFORM_RU = "out.pladform.ru";

	private final AnidubApiFeignClient anidubApiFeignClient;

	private final GithubResourcesService githubResourcesService;

	private final GithubResources githubResources;

	private final AnidubParserI anidubParser;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		AnidubTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			Integer titleId = matchedTitle.getId();
			Integer anidubFandubId = getAnidubFandubId(titleId);
			List<AnidubTitleFandubSource> anidubEpisodesSources = getAnidubEpisodesSources(titleId, anidubFandubId);
			List<AnidubTitleEpisode> titleEpisodes = getValidEpisodes(titleId, anidubFandubId, anidubEpisodesSources);
			Map<String, String> episodesAndUrls = extractEpisodesAndUrls(titleEpisodes);
			url = anidubParser.fixBrokenUrl(episodesAndUrls.getOrDefault(getNextEpisodeForWatch(watchingTitle),
					FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
		} else {
			log.debug("TITLE [{}] WAS NOT FOUND ON ANIDUB!", watchingTitle.getTitle());
		}
		return url;
	}

	private AnidubTitle getMatchedTitle(UserMALTitleInfo watchingTitle) {
		return githubResourcesService.getResource(githubResources.getAnidubTitles(), AnidubTitle.class)
				.stream()
				.filter(x -> watchingTitle.getAnimeId()
						.equals(x.getTitleIdOnMal()))
				.findFirst()
				.orElse(null);
	}

	private Integer getAnidubFandubId(Integer titleId) {
		return anidubApiFeignClient.getAvailableFandubs(titleId)
				.getTypes()
				.stream()
				.filter(x -> ANIDUB_FANDUB_NAME.equals(x.getName()))
				.findFirst()
				.orElseGet(AnidubTitleFandubSource::new)
				.getId();
	}

	private List<AnidubTitleFandubSource> getAnidubEpisodesSources(Integer titleId, Integer anidubFandubId) {
		List<AnidubTitleFandubSource> result = Collections.emptyList();
		if (nonNull(titleId) && nonNull(anidubFandubId)) {
			result = anidubApiFeignClient.getFandubEpisodesSources(titleId, anidubFandubId)
					.getSources()
					.stream()
					.filter(this::isValidSource)
					.collect(Collectors.toList());
		}
		return result;
	}

	private boolean isValidSource(AnidubTitleFandubSource anidubTitleFandubSource) {
		String sourceName = anidubTitleFandubSource.getName();
		return !(StringUtils.contains(sourceName, UNAVAILABLE_SOURCE_MARK) || StringUtils.contains(sourceName, UNSUPPORTED_SOURCE_MARK));
	}

	private List<AnidubTitleEpisode> getValidEpisodes(Integer titleId, Integer anidubFandubId, List<AnidubTitleFandubSource> anidubEpisodesSources) {
		return anidubEpisodesSources.stream()
				.map(x -> getTitleEpisodes(titleId, anidubFandubId, x.getId()))
				.filter(this::isValidHost)
				.findFirst()
				.orElseGet(Collections::emptyList);
	}

	private List<AnidubTitleEpisode> getTitleEpisodes(Integer titleId, Integer fandubSourceId, Integer sourceId) {
		List<AnidubTitleEpisode> result = Collections.emptyList();
		if (nonNull(titleId) && nonNull(fandubSourceId) && nonNull(sourceId)) {
			result = anidubApiFeignClient.getTitleEpisodes(titleId, fandubSourceId, sourceId)
					.getEpisodes();
		}
		return result;
	}

	private boolean isValidHost(List<AnidubTitleEpisode> episodes) {
		return episodes.stream()
				.noneMatch(x -> StringUtils.contains(x.getUrl(), VK_COM) || StringUtils.contains(x.getUrl(), OUT_PLADFORM_RU));
	}

	private Map<String, String> extractEpisodesAndUrls(List<AnidubTitleEpisode> titleEpisodes) {
		return titleEpisodes.stream()
				.collect(Collectors.toMap(x -> anidubParser.extractEpisodeNumber(x.getName()), AnidubTitleEpisode::getUrl, (oldKey, newKey) -> oldKey));
	}

	private String getNextEpisodeForWatch(UserMALTitleInfo watchingTitle) {
		return String.valueOf(watchingTitle.getNumWatchedEpisodes() + 1);
	}
}
