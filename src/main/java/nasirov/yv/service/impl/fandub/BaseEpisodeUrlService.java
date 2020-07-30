package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.dto.fandub.common.TitleType;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Created by nasirov.yv
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseEpisodeUrlService implements EpisodeUrlServiceI {

	private final TitlesServiceI titlesService;

	private final FanDubProps fanDubProps;

	@Override
	public final String getEpisodeUrl(FanDubSource fanDubSource, MalTitle watchingTitle) {
		String result = NOT_FOUND_ON_FANDUB_SITE_URL;
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		log.debug("Trying to build url for [{} - {} episode] by [{}]", watchingTitle.getAnimeUrl(), nextEpisodeForWatch, fanDubSource);
		String fandubUrl = fanDubProps.getUrls()
				.get(fanDubSource);
		List<CommonTitle> matchedTitles = getMatchedTitles(fanDubSource, watchingTitle);
		if (Objects.nonNull(matchedTitles)) {
			result = buildUrl(nextEpisodeForWatch, matchedTitles, fandubUrl);
		}
		log.debug("Got url [{}] for [{} - {} episode] by [{}]", result, watchingTitle.getAnimeUrl(), nextEpisodeForWatch, fanDubSource);
		return result;
	}

	protected String buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		log.debug("Building url in runtime...");
		CommonTitle commonTitle = Iterables.get(matchedTitles, 0);
		List<FandubEpisode> episodes = getEpisodes(commonTitle);
		boolean isFoundAvailableEpisode = episodes.stream()
				.anyMatch(x -> nextEpisodeForWatch.equals(x.getId()));
		return isFoundAvailableEpisode ? fandubUrl + commonTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
	}

	protected abstract List<FandubEpisode> getEpisodes(CommonTitle commonTitle);

	private String buildUrl(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return matchedTitles.stream()
				.map(CommonTitle::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> fandubUrl + x.getUrl())
				.orElseGet(() -> buildUrlAlt(nextEpisodeForWatch, matchedTitles, fandubUrl));
	}

	private String buildUrlAlt(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Optional.of(matchedTitles)
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.map(x -> buildUrlInRuntime(nextEpisodeForWatch, x, fandubUrl))
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private List<CommonTitle> getMatchedTitles(FanDubSource fanDubSource, MalTitle watchingTitle) {
		Map<Integer, List<CommonTitle>> mappedTitlesByMalId = titlesService.getTitles(fanDubSource);
		return mappedTitlesByMalId.get(watchingTitle.getId());
	}

	private List<CommonTitle> extractRegularTitles(List<CommonTitle> matchedTitles) {
		return matchedTitles.stream()
				.filter(x -> x.getType() == TitleType.REGULAR)
				.collect(Collectors.toList());
	}
}
