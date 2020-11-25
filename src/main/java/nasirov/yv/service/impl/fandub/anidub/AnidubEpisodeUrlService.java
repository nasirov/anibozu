package nasirov.yv.service.impl.fandub.anidub;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnidubParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anidub.AnidubFeignClient;
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
public class AnidubEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnidubFeignClient anidubFeignClient;

	private final AnidubParserI anidubParser;

	public AnidubEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			CommonProps commonProps, AnidubFeignClient anidubFeignClient, AnidubParserI anidubParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps, commonProps);
		this.anidubFeignClient = anidubFeignClient;
		this.anidubParser = anidubParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String url = commonTitle.getUrl();
		String titlePage = anidubFeignClient.getTitlePage(url);
		Document document = Jsoup.parse(titlePage);
		return anidubParser.extractEpisodes(document);
	}
}
