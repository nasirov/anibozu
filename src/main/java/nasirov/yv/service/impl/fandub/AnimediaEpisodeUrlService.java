package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimediaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
public class AnimediaEpisodeUrlService extends AbstractEpisodeUrlService {

	private final AnimediaParserI animediaParser;

	public AnimediaEpisodeUrlService(FanDubProps fanDubProps, CommonProps commonProps, AnimediaParserI animediaParser,
			HttpRequestServiceI httpRequestService, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.ANIMEDIA);
		this.animediaParser = animediaParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.animedia(commonTitle))
				.doOnNext(x -> fillAnimediaEpisodesWithTitleUrl(x, commonTitle.getUrl()))
				.map(animediaParser::extractEpisodes);
	}

	@Override
	protected Mono<String> buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Mono.just(matchedTitles)
				.filter(x -> commonProps.getEnableBuildUrlInRuntime()
						.get(fanDubSource))
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.map(x -> Collections.max(x, Comparator.comparing(CommonTitle::getDataList)))
				.flatMap(this::getEpisodes)
				.flatMapMany(Flux::fromIterable)
				.filter(x -> StringUtils.equals(nextEpisodeForWatch.toString(), x.getNumber()))
				.next()
				.map(x -> fandubUrl + x.getUrl())
				.defaultIfEmpty(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.doOnSubscribe(x -> log.debug("Building url in runtime..."));
	}

	private void fillAnimediaEpisodesWithTitleUrl(List<AnimediaEpisode> animediaEpisodes, String titleUrl) {
		animediaEpisodes.forEach(x -> x.setTitleUrl(titleUrl));
	}
}
