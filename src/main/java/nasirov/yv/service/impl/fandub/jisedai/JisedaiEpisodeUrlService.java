package nasirov.yv.service.impl.fandub.jisedai;

import java.util.List;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.jisedai.JisedaiFeignClient;
import nasirov.yv.service.TitlesServiceI;
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

	public JisedaiEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, JisedaiFeignClient jisedaiFeignClient,
			JisedaiParserI jisedaiParser) {
		super(titlesService, fanDubProps);
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
