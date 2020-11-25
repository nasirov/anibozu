package nasirov.yv.service.impl.fandub.jisedai;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.jisedai.JisedaiFeignClient;
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
public class JisedaiEpisodeUrlService extends BaseEpisodeUrlService {

	private final JisedaiFeignClient jisedaiFeignClient;

	private final JisedaiParserI jisedaiParser;

	public JisedaiEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			JisedaiFeignClient jisedaiFeignClient,
			JisedaiParserI jisedaiParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps);
		this.jisedaiFeignClient = jisedaiFeignClient;
		this.jisedaiParser = jisedaiParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String url = commonTitle.getUrl();
		String titlePage = jisedaiFeignClient.getTitlePage(url);
		Document document = Jsoup.parse(titlePage);
		return jisedaiParser.extractEpisodes(document);
	}
}
