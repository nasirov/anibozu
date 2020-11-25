package nasirov.yv.service.impl.fandub.sovet_romantica;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.sovet_romantica.SovetRomanticaFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub_titles_service.FandubTitlesServiceFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class SovetRomanticaEpisodeUrlService extends BaseEpisodeUrlService {

	private final SovetRomanticaFeignClient sovetRomanticaFeignClient;

	private final SovetRomanticaParserI sovetRomanticaParser;

	public SovetRomanticaEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			SovetRomanticaFeignClient sovetRomanticaFeignClient,
			SovetRomanticaParserI sovetRomanticaParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps);
		this.sovetRomanticaFeignClient = sovetRomanticaFeignClient;
		this.sovetRomanticaParser = sovetRomanticaParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String titlePage = sovetRomanticaFeignClient.getTitlePage(commonTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return sovetRomanticaParser.extractEpisodes(document);
	}
}
