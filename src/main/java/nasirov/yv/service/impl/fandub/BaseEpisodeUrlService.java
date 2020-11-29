package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.TitleType;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub_titles_service.FandubTitlesServiceFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
public abstract class BaseEpisodeUrlService implements EpisodeUrlServiceI {

	private final FanDubProps fanDubProps;

	private final FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient;

	private final AuthProps authProps;

	private final CommonProps commonProps;

	protected BaseEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			CommonProps commonProps) {
		this.fanDubProps = fanDubProps;
		this.fandubTitlesServiceFeignClient = fandubTitlesServiceFeignClient;
		this.authProps = authProps;
		this.commonProps = commonProps;
	}

	@Override
	public final String getEpisodeUrl(FanDubSource fanDubSource, MalTitle watchingTitle) {
		String result = NOT_FOUND_ON_FANDUB_SITE_URL;
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		log.debug("Trying to build url for [{} - {} episode] by [{}]", watchingTitle.getAnimeUrl(), nextEpisodeForWatch, fanDubSource);
		String fandubUrl = fanDubProps.getUrls()
				.get(fanDubSource);
		List<CommonTitle> matchedTitles = getMatchedTitles(authProps.getFandubTitlesServiceBasicAuth(),
				fanDubSource,
				watchingTitle.getId(),
				nextEpisodeForWatch);
		if (CollectionUtils.isNotEmpty(matchedTitles)) {
			result = buildUrl(nextEpisodeForWatch, matchedTitles, fandubUrl);
		}
		log.debug("Got url [{}] for [{} - {} episode] by [{}]", result, watchingTitle.getAnimeUrl(), nextEpisodeForWatch, fanDubSource);
		return result;
	}

	protected String buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		log.debug("Building url in runtime...");
		CommonTitle commonTitle = Iterables.get(matchedTitles, 0);
		List<FandubEpisode> episodes = getEpisodes(commonTitle);
		return episodes.stream()
				.filter(x -> nextEpisodeForWatch.equals(x.getId()))
				.findFirst()
				.map(x -> fandubUrl + x.getUrl())
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	protected abstract List<FandubEpisode> getEpisodes(CommonTitle commonTitle);

	protected String buildUrl(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return matchedTitles.stream()
				.map(CommonTitle::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> fandubUrl + x.getUrl())
				.orElseGet(() -> buildUrlAlt(nextEpisodeForWatch, matchedTitles, fandubUrl));
	}

	protected String buildUrlAlt(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Optional.of(matchedTitles)
				.filter(x -> commonProps.getEnableBuildUrlInRuntime())
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.map(x -> buildUrlInRuntime(nextEpisodeForWatch, x, fandubUrl))
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	private List<CommonTitle> getMatchedTitles(String basicAuth, FanDubSource fanDubSource, Integer malId, int malEpisodeId) {
		return fandubTitlesServiceFeignClient.getCommonTitles(basicAuth, fanDubSource, malId, malEpisodeId);
	}

	private List<CommonTitle> extractRegularTitles(List<CommonTitle> matchedTitles) {
		return matchedTitles.stream()
				.filter(x -> x.getType() == TitleType.REGULAR)
				.collect(Collectors.toList());
	}
}
