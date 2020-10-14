package nasirov.yv.service.impl.fandub.anilibria;

import java.util.List;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnilibriaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anilibria.AnilibriaFeignClient;
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
public class AnilibriaEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnilibriaFeignClient anilibriaFeignClient;

	private final AnilibriaParserI anilibriaParser;

	public AnilibriaEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, AnilibriaFeignClient anilibriaFeignClient,
			AnilibriaParserI anilibriaParser) {
		super(titlesService, fanDubProps);
		this.anilibriaFeignClient = anilibriaFeignClient;
		this.anilibriaParser = anilibriaParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String url = commonTitle.getUrl();
		String titlePage = anilibriaFeignClient.getTitlePage(url);
		Document document = Jsoup.parse(titlePage);
		return anilibriaParser.extractEpisodes(document);
	}
}
