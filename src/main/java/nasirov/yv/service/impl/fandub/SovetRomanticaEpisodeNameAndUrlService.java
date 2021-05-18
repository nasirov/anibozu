package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class SovetRomanticaEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final SovetRomanticaParserI sovetRomanticaParser;

	public SovetRomanticaEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			SovetRomanticaParserI sovetRomanticaParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.SOVETROMANTICA);
		this.sovetRomanticaParser = sovetRomanticaParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.sovetRomanticaDdosGuard())
				.map(sovetRomanticaParser::extractCookie)
				.map(x -> httpRequestServiceDtoBuilder.sovetRomantica(commonTitle, x.orElse(null)))
				.flatMap(httpRequestService::performHttpRequest)
				.map(Jsoup::parse)
				.map(sovetRomanticaParser::extractEpisodes);
	}
}
