package nasirov.yv.service.impl.fandub.jutsu;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JutsuParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.jutsu.JutsuFeignClient;
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
public class JutsuEpisodeUrlService extends BaseEpisodeUrlService {

	private final JutsuFeignClient jutsuFeignClient;

	private final JutsuParserI jutsuParser;

	public JutsuEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			JutsuFeignClient jutsuFeignClient, JutsuParserI jutsuParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps);
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
