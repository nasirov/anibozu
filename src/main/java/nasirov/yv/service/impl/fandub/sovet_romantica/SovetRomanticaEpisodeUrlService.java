package nasirov.yv.service.impl.fandub.sovet_romantica;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.SovetRomanticaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.sovet_romantica.SovetRomanticaFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
public class SovetRomanticaEpisodeUrlService extends BaseEpisodeUrlService {

	private final SovetRomanticaFeignClient sovetRomanticaFeignClient;

	private final SovetRomanticaParserI sovetRomanticaParser;

	public SovetRomanticaEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, SovetRomanticaFeignClient sovetRomanticaFeignClient,
			SovetRomanticaParserI sovetRomanticaParser) {
		super(titlesService, fanDubProps);
		this.sovetRomanticaFeignClient = sovetRomanticaFeignClient;
		this.sovetRomanticaParser = sovetRomanticaParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		log.debug("Building url in runtime...");
		String titlePage = sovetRomanticaFeignClient.getTitlePage(commonTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return sovetRomanticaParser.extractEpisodes(document);
	}
}
