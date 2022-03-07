package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class JisedaiEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final JisedaiParserI jisedaiParser;

	public JisedaiEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			JisedaiParserI jisedaiParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.JISEDAI);
		this.jisedaiParser = jisedaiParser;
	}

	@Override
	protected Mono<List<CommonEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.jisedai(commonTitle))
				.map(jisedaiParser::extractEpisodes)
				.doOnNext(x -> fillFandubEpisodesWithTitleUrl(x, commonTitle.getUrl()));
	}

	private void fillFandubEpisodesWithTitleUrl(List<CommonEpisode> commonEpisodes, String titleUrl) {
		commonEpisodes.forEach(x -> x.setUrl(titleUrl));
	}
}
