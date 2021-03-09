package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaDdosGuardParserI;
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
public class SovetRomanticaEpisodeUrlService extends AbstractEpisodeUrlService {

	private final SovetRomanticaParserI sovetRomanticaParser;

	private final SovetRomanticaDdosGuardParserI sovetRomanticaDdosGuardParser;

	public SovetRomanticaEpisodeUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			SovetRomanticaParserI sovetRomanticaParser, HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder,
			SovetRomanticaDdosGuardParserI sovetRomanticaDdosGuardParser) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.SOVETROMANTICA);
		this.sovetRomanticaParser = sovetRomanticaParser;
		this.sovetRomanticaDdosGuardParser = sovetRomanticaDdosGuardParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.sovetRomanticaDdosGuard())
				.map(sovetRomanticaDdosGuardParser::extractDdosGuardCookie)
				.map(x -> httpRequestServiceDtoBuilder.sovetRomantica(commonTitle, x.orElse(null)))
				.flatMap(httpRequestService::performHttpRequest)
				.map(Jsoup::parse)
				.map(sovetRomanticaParser::extractEpisodes);
	}
}
