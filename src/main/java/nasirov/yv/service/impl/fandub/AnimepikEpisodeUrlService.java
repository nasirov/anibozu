package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
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
public class AnimepikEpisodeUrlService extends AbstractEpisodeUrlService {

	private final AnimepikParserI animepikParser;

	public AnimepikEpisodeUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			AnimepikParserI animepikParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder);
		this.animepikParser = animepikParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.animepik(commonTitle))
				.doOnNext(x -> fillAnimepikEpisodesWithTitleUrl(x, commonTitle.getUrl()))
				.map(animepikParser::extractEpisodes);
	}

	private void fillAnimepikEpisodesWithTitleUrl(List<AnimepikEpisode> animepikEpisodes, String titleUrl) {
		animepikEpisodes.forEach(x -> x.setTitleUrl(titleUrl));
	}
}
