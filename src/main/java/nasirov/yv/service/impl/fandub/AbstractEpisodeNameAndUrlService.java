package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;

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
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEpisodeNameAndUrlService implements EpisodeNameAndUrlServiceI {

	protected final FanDubProps fanDubProps;

	protected final CommonProps commonProps;

	protected final HttpRequestServiceI httpRequestService;

	protected final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	protected final FanDubSource fanDubSource;

	@Override
	public final Mono<Pair<String, String>> getEpisodeNameAndUrl(MalTitle watchingTitle, List<CommonTitle> commonTitles) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		String animeUrl = watchingTitle.getAnimeUrl();
		return Mono.just(commonTitles)
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(x -> buildNameAndUrl(nextEpisodeForWatch,
						x,
						fanDubProps.getUrls()
								.get(fanDubSource)))
				.defaultIfEmpty(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL)
				.doOnSubscribe(x -> log.debug("Trying to get episode name and url for [{} - {} episode] by [{}]",
						animeUrl,
						nextEpisodeForWatch,
						fanDubSource))
				.doOnSuccess(x -> log.debug("Got episode name and url [{}] for [{} - {} episode] by [{}]", x, animeUrl, nextEpisodeForWatch, fanDubSource));
	}

	protected abstract Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle);

	protected Mono<Pair<String, String>> buildNameAndUrl(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Flux.fromIterable(matchedTitles)
				.map(CommonTitle::getEpisodes)
				.flatMap(Flux::fromIterable)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.next()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()))
				.switchIfEmpty(buildNameAndUrlInRuntime(nextEpisodeForWatch, matchedTitles, fandubUrl));
	}

	protected Mono<Pair<String, String>> buildNameAndUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
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
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()))
				.defaultIfEmpty(NOT_AVAILABLE_EPISODE_NAME_AND_URL)
				.doOnSubscribe(x -> log.debug("Trying to get episode name and url in runtime..."));
	}

	protected List<CommonTitle> extractRegularTitles(List<CommonTitle> matchedTitles) {
		return matchedTitles.stream()
				.filter(x -> x.getType() == TitleType.REGULAR)
				.collect(Collectors.toList());
	}
}
