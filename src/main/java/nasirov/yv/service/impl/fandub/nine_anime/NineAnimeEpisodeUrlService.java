package nasirov.yv.service.impl.fandub.nine_anime;

import java.util.List;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.NineAnimeParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.nine_anime.NineAnimeFeignClient;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class NineAnimeEpisodeUrlService extends BaseEpisodeUrlService {

	private final NineAnimeFeignClient nineAnimeFeignClient;

	private final NineAnimeParserI nineAnimeParser;

	public NineAnimeEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, NineAnimeFeignClient nineAnimeFeignClient,
			NineAnimeParserI nineAnimeParser) {
		super(titlesService, fanDubProps);
		this.nineAnimeFeignClient = nineAnimeFeignClient;
		this.nineAnimeParser = nineAnimeParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String dataId = nineAnimeParser.extractDataId(commonTitle.getUrl());
		String htmlWithTitleEpisodes = nineAnimeFeignClient.getTitleEpisodes(dataId);
		Document document = Jsoup.parse(htmlWithTitleEpisodes);
		return nineAnimeParser.extractEpisodes(document);
	}
}
