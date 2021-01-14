package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.TitleType;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEpisodeUrlService implements EpisodeUrlServiceI {

	protected final FanDubProps fanDubProps;

	protected final CommonProps commonProps;

	protected final HttpRequestServiceI httpRequestService;

	protected final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	protected final FanDubSource fanDubSource;

	@Override
	public final Mono<String> getEpisodeUrl(MalTitle watchingTitle) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		String animeUrl = watchingTitle.getAnimeUrl();
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.fandubTitlesService(fanDubSource,
				watchingTitle.getId(), nextEpisodeForWatch))
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(x -> buildUrl(nextEpisodeForWatch,
						x,
						fanDubProps.getUrls()
								.get(fanDubSource)))
				.defaultIfEmpty(NOT_FOUND_ON_FANDUB_SITE_URL)
				.doOnSubscribe(x -> log.debug("Trying to build url for [{} - {} episode] by [{}]", animeUrl, nextEpisodeForWatch, fanDubSource))
				.doOnSuccess(x -> log.debug("Got url [{}] for [{} - {} episode] by [{}]", x, animeUrl, nextEpisodeForWatch, fanDubSource));
	}

	protected abstract Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle);

	protected Mono<String> buildUrl(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Flux.fromIterable(matchedTitles)
				.map(CommonTitle::getEpisodes)
				.flatMap(Flux::fromIterable)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.next()
				.map(x -> fandubUrl + x.getUrl())
				.switchIfEmpty(buildUrlInRuntime(nextEpisodeForWatch, matchedTitles, fandubUrl));
	}

	protected Mono<String> buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Mono.just(matchedTitles)
				.filter(x -> commonProps.getEnableBuildUrlInRuntime()
						.get(fanDubSource))
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.flatMapMany(Flux::fromIterable)
				.next()
				.flatMap(this::getEpisodes)
				.flatMapMany(Flux::fromIterable)
				.filter(x -> nextEpisodeForWatch.equals(x.getId()))
				.next()
				.map(x -> fandubUrl + x.getUrl())
				.defaultIfEmpty(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.doOnSubscribe(x -> log.debug("Building url in runtime..."));
	}

	protected List<CommonTitle> extractRegularTitles(List<CommonTitle> matchedTitles) {
		return matchedTitles.stream()
				.filter(x -> x.getType() == TitleType.REGULAR)
				.collect(Collectors.toList());
	}
}
