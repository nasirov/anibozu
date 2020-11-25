package nasirov.yv.service.impl.fandub.anilibria;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnilibriaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.anilibria.AnilibriaFeignClient;
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
public class AnilibriaEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnilibriaFeignClient anilibriaFeignClient;

	private final AnilibriaParserI anilibriaParser;

	public AnilibriaEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			CommonProps commonProps,
			AnilibriaFeignClient anilibriaFeignClient,
			AnilibriaParserI anilibriaParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps, commonProps);
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
