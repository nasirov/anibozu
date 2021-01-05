package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.selenium_service.SeleniumServiceRequestDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.NineAnimeParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
public class NineAnimeEpisodeUrlService extends AbstractEpisodeUrlService {

	private final NineAnimeParserI nineAnimeParser;

	public NineAnimeEpisodeUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			NineAnimeParserI nineAnimeParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder);
		this.nineAnimeParser = nineAnimeParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.seleniumService(SeleniumServiceRequestDto.builder()
				.url(fanDubProps.getUrls()
						.get(FanDubSource.NINEANIME) + "watch/" + commonTitle.getDataId())
				.timeoutInSec(15)
				.cssSelector("ul.episodes >li")
				.build()))
				.map(Jsoup::parse)
				.map(nineAnimeParser::extractEpisodes);
	}

	@Override
	protected Mono<String> buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return Mono.just(matchedTitles)
				.filter(x -> commonProps.getEnableBuildUrlInRuntime())
				.map(this::extractRegularTitles)
				.filter(CollectionUtils::isNotEmpty)
				.flatMapMany(Flux::fromIterable)
				.flatMap(this::getEpisodes)
				.flatMapSequential(Flux::fromIterable)
				.filter(x -> StringUtils.equals(nextEpisodeForWatch.toString(), x.getNumber()))
				.next()
				.map(x -> fandubUrl + x.getUrl())
				.defaultIfEmpty(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)
				.doOnSubscribe(x -> log.debug("Building url in runtime..."));
	}
}
