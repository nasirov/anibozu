package nasirov.yv.service.impl.fandub.nine_anime;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.NineAnimeParserI;
import nasirov.yv.fandub.service.spring.boot.starter.service.NineAnimeServiceI;
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
public class NineAnimeEpisodeUrlService extends BaseEpisodeUrlService {

	private final NineAnimeServiceI nineAnimeService;

	private final NineAnimeParserI nineAnimeParser;

	public NineAnimeEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, NineAnimeServiceI nineAnimeService,
			NineAnimeParserI nineAnimeParser) {
		super(titlesService, fanDubProps);
		this.nineAnimeService = nineAnimeService;
		this.nineAnimeParser = nineAnimeParser;
	}

	@Override
	protected String buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		log.debug("Building url in runtime...");
		return matchedTitles.stream()
				.map(this::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getId()))
				.findFirst()
				.map(x -> fandubUrl + x.getUrl())
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String dataId = nineAnimeParser.extractDataId(commonTitle.getUrl());
		String htmlWithTitleEpisodes = nineAnimeService.getTitleEpisodes(dataId);
		Document document = Jsoup.parse(htmlWithTitleEpisodes);
		return nineAnimeParser.extractEpisodes(document);
	}
}
