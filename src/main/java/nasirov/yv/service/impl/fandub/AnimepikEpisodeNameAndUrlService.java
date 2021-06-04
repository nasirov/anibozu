package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class AnimepikEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final AnimepikParserI animepikParser;

	public AnimepikEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			AnimepikParserI animepikParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.ANIMEPIK);
		this.animepikParser = animepikParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.animepik(commonTitle))
				.doOnNext(x -> fillAnimepikEpisodesWithTitleUrl(x, commonTitle.getUrl()))
				.map(x -> animepikParser.extractEpisodes(x.getEpisodes()));
	}

	private void fillAnimepikEpisodesWithTitleUrl(AnimepikTitleEpisodes animepikTitleEpisodes, String titleUrl) {
		animepikTitleEpisodes.getEpisodes()
				.forEach(x -> x.setTitleUrl(titleUrl));
	}
}
