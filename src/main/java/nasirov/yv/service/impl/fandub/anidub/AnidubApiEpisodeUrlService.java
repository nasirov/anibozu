package nasirov.yv.service.impl.fandub.anidub;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.data.constants.ServiceSourceType.API;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.anidub.api.AnidubTitleEpisode;
import nasirov.yv.data.anidub.api.AnidubTitleFandubSource;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.http.feign.AnidubApiFeignClient;
import nasirov.yv.parser.AnidubParserI;
import nasirov.yv.service.AnidubEpisodeUrlServiceI;
import nasirov.yv.service.AnidubGitHubResourcesServiceI;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = API)
public class AnidubApiEpisodeUrlService implements AnidubEpisodeUrlServiceI {

	private static final String ANIDUB_FANDUB_NAME = "anidub";

	private static final String UNAVAILABLE_SOURCE_MARK = "(не работает)";

	private static final String UNSUPPORTED_SOURCE_MARK = "Kodik";

	private static final String VK_COM = "vk.com";

	private static final String OUT_PLADFORM_RU = "out.pladform.ru";

	private final AnidubApiFeignClient anidubApiFeignClient;

	private final AnidubGitHubResourcesServiceI<AnidubApiTitle> anidubGitHubResourcesService;

	private final AnidubParserI anidubParser;

	@Override
	public String getEpisodeUrl(UserMALTitleInfo watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		AnidubApiTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			Integer titleId = matchedTitle.getId();
			Integer anidubFandubId = getAnidubFandubId(titleId);
			List<AnidubTitleFandubSource> anidubEpisodesSources = getAnidubEpisodesSources(titleId, anidubFandubId);
			List<AnidubTitleEpisode> titleEpisodes = getValidEpisodes(titleId, anidubFandubId, anidubEpisodesSources);
			Map<String, String> episodesAndUrls = extractEpisodesAndUrls(titleEpisodes);
			url = anidubParser.fixBrokenUrl(episodesAndUrls.getOrDefault(getNextEpisodeForWatch(watchingTitle).toString(),
					FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE));
		} else {
			log.debug("TITLE [{}] WAS NOT FOUND ON ANIDUB!", watchingTitle.getTitle());
		}
		return url;
	}

	private AnidubApiTitle getMatchedTitle(UserMALTitleInfo watchingTitle) {
		return anidubGitHubResourcesService.getAnidubTitles()
				.get(watchingTitle.getAnimeId());
	}

	private Integer getAnidubFandubId(Integer titleId) {
		return anidubApiFeignClient.getAvailableFandubs(titleId)
				.getTypes()
				.stream()
				.filter(x -> ANIDUB_FANDUB_NAME.equals(StringUtils.lowerCase(x.getName())))
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
}
