package nasirov.yv.service.impl.fandub.shiza_project;

import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.ShizaProjectParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.shiza_project.ShizaProjectFeignClient;
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
public class ShizaProjectEpisodeUrlService extends BaseEpisodeUrlService {

	private final ShizaProjectFeignClient shizaProjectFeignClient;

	private final ShizaProjectParserI shizaProjectParser;

	public ShizaProjectEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, ShizaProjectFeignClient shizaProjectFeignClient,
			ShizaProjectParserI shizaProjectParser) {
		super(titlesService, fanDubProps);
		this.shizaProjectFeignClient = shizaProjectFeignClient;
		this.shizaProjectParser = shizaProjectParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String titlePage = shizaProjectFeignClient.getTitlePage(commonTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return shizaProjectParser.extractEpisodes(document);
	}
}
