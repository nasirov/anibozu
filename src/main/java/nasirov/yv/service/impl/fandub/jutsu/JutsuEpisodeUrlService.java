package nasirov.yv.service.impl.fandub.jutsu;

import java.util.List;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JutsuParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.jutsu.JutsuFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class JutsuEpisodeUrlService extends BaseEpisodeUrlService {

	private final JutsuFeignClient jutsuFeignClient;

	private final JutsuParserI jutsuParser;

	public JutsuEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, JutsuFeignClient jutsuFeignClient, JutsuParserI jutsuParser) {
		super(titlesService, fanDubProps);
		this.jutsuFeignClient = jutsuFeignClient;
		this.jutsuParser = jutsuParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String titlePage = jutsuFeignClient.getTitlePage(commonTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return jutsuParser.extractEpisodes(document);
	}
}
