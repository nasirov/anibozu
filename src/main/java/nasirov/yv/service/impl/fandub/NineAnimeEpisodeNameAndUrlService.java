package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.ReactiveNineAnimeServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
public class NineAnimeEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final ReactiveNineAnimeServiceI reactiveNineAnimeService;

	public NineAnimeEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder, ReactiveNineAnimeServiceI reactiveNineAnimeService) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.NINEANIME);
		this.reactiveNineAnimeService = reactiveNineAnimeService;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return reactiveNineAnimeService.getTitleEpisodes(commonTitle.getId());
	}

	@Override
	protected Mono<Pair<String, String>> buildNameAndUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Mono.just(matchedTitles)
				.filter(x -> commonProps.getEnableBuildUrlInRuntime()
						.get(fanDubSource))
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.flatMapMany(Flux::fromIterable)
				.flatMap(this::getEpisodes)
				.flatMapSequential(Flux::fromIterable)
				.filter(x -> StringUtils.equals(nextEpisodeForWatch.toString(), x.getNumber()))
				.next()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()))
				.defaultIfEmpty(NOT_AVAILABLE_EPISODE_NAME_AND_URL)
				.doOnSubscribe(x -> log.debug("Trying to get episode name and url in runtime..."));
	}
}
